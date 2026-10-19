package com.example.core.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Base64
import com.example.core.bluetooth.BluetoothMeshManager
import com.example.core.database.CallHistoryEntity
import com.example.core.protocol.MeshPacket
import com.example.core.protocol.MeshPacketType
import com.example.data.MeshRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

enum class CallState {
    IDLE,
    OUTGOING_RINGING,
    INCOMING_RINGING,
    CONNECTING,
    ACTIVE,
    ENDED,
    FAILED
}

data class ActiveCallInfo(
    val callId: String = "",
    val peerDeviceId: String = "",
    val peerName: String = "",
    val isOutgoing: Boolean = true,
    val state: CallState = CallState.IDLE,
    val durationSeconds: Long = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val currentAudioAmplitude: Float = 0f,
    val packetsSent: Long = 0,
    val packetsReceived: Long = 0,
    val estimatedLatencyMs: Long = 45,
    val packetLossPercent: Int = 0,
    val hasMicPermission: Boolean = true
)

class AudioCallManager(
    private val context: Context,
    private val cryptoManager: com.example.core.crypto.CryptoManager,
    private val meshManager: BluetoothMeshManager,
    private val repository: MeshRepository,
    private val scope: CoroutineScope
) {
    companion object {
        const val SAMPLE_RATE = 16000
        const val CHANNEL_CONFIG_IN = AudioFormat.CHANNEL_IN_MONO
        const val CHANNEL_CONFIG_OUT = AudioFormat.CHANNEL_OUT_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        const val FRAME_SIZE_SAMPLES = 320 // 20ms frames at 16kHz
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _currentCall = MutableStateFlow(ActiveCallInfo())
    val currentCall: StateFlow<ActiveCallInfo> = _currentCall.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var captureJob: Job? = null
    private var durationJob: Job? = null

    private var callStartTime: Long = 0
    private var sequenceNumber: Long = 0

    init {
        // Listen for incoming packets related to calling
        scope.launch {
            meshManager.incomingPackets.collect { packet ->
                handleIncomingCallPacket(packet)
            }
        }
    }

    private suspend fun handleIncomingCallPacket(packet: MeshPacket) {
        when (packet.type) {
            MeshPacketType.CALL_REQUEST -> {
                if (_currentCall.value.state == CallState.IDLE) {
                    val peerDevice = repository.getDeviceById(packet.originDeviceId)
                    val callerName = peerDevice?.displayName ?: packet.originDeviceId
                    _currentCall.value = ActiveCallInfo(
                        callId = packet.packetId,
                        peerDeviceId = packet.originDeviceId,
                        peerName = callerName,
                        isOutgoing = false,
                        state = CallState.INCOMING_RINGING
                    )
                } else {
                    // Send busy reject
                    val reject = MeshPacket(
                        type = MeshPacketType.CALL_REJECT,
                        originDeviceId = cryptoManager.deviceId,
                        destinationDeviceId = packet.originDeviceId,
                        payload = "BUSY"
                    )
                    meshManager.sendPacket(reject)
                }
            }
            MeshPacketType.CALL_ACCEPT -> {
                if (_currentCall.value.state == CallState.OUTGOING_RINGING &&
                    _currentCall.value.peerDeviceId == packet.originDeviceId
                ) {
                    startActiveAudioStream()
                }
            }
            MeshPacketType.CALL_REJECT -> {
                if (_currentCall.value.peerDeviceId == packet.originDeviceId) {
                    endCall(CallState.FAILED, "Call rejected or busy")
                }
            }
            MeshPacketType.CALL_HANGUP -> {
                if (_currentCall.value.peerDeviceId == packet.originDeviceId) {
                    endCall(CallState.ENDED, "Remote peer ended call")
                }
            }
            MeshPacketType.AUDIO_PACKET -> {
                if (_currentCall.value.state == CallState.ACTIVE &&
                    _currentCall.value.peerDeviceId == packet.originDeviceId
                ) {
                    playIncomingAudioPacket(packet.payload)
                }
            }
            else -> {}
        }
    }

    fun initiateCall(targetDeviceId: String, targetName: String) {
        if (_currentCall.value.state != CallState.IDLE) return

        val callId = UUID.randomUUID().toString()
        _currentCall.value = ActiveCallInfo(
            callId = callId,
            peerDeviceId = targetDeviceId,
            peerName = targetName,
            isOutgoing = true,
            state = CallState.OUTGOING_RINGING
        )

        scope.launch {
            val offerPacket = MeshPacket(
                packetId = callId,
                type = MeshPacketType.CALL_REQUEST,
                originDeviceId = cryptoManager.deviceId,
                destinationDeviceId = targetDeviceId,
                payload = "RONOVA_VOICE_OFFER"
            )
            meshManager.sendPacket(offerPacket)

            // Timeout after 30 seconds if unanswered
            delay(30000L)
            if (_currentCall.value.state == CallState.OUTGOING_RINGING) {
                endCall(CallState.FAILED, "No answer")
            }
        }
    }

    fun acceptIncomingCall() {
        val call = _currentCall.value
        if (call.state != CallState.INCOMING_RINGING) return

        scope.launch {
            val acceptPacket = MeshPacket(
                packetId = call.callId,
                type = MeshPacketType.CALL_ACCEPT,
                originDeviceId = cryptoManager.deviceId,
                destinationDeviceId = call.peerDeviceId,
                payload = "RONOVA_VOICE_ACCEPT"
            )
            meshManager.sendPacket(acceptPacket)
            startActiveAudioStream()
        }
    }

    fun rejectIncomingCall() {
        val call = _currentCall.value
        if (call.state != CallState.INCOMING_RINGING) return

        scope.launch {
            val rejectPacket = MeshPacket(
                packetId = call.callId,
                type = MeshPacketType.CALL_REJECT,
                originDeviceId = cryptoManager.deviceId,
                destinationDeviceId = call.peerDeviceId,
                payload = "DECLINED"
            )
            meshManager.sendPacket(rejectPacket)
            endCall(CallState.ENDED, "Declined")
        }
    }

    fun hangupCall() {
        val call = _currentCall.value
        if (call.state == CallState.IDLE) return

        scope.launch {
            val hangupPacket = MeshPacket(
                packetId = call.callId,
                type = MeshPacketType.CALL_HANGUP,
                originDeviceId = cryptoManager.deviceId,
                destinationDeviceId = call.peerDeviceId,
                payload = "HANGUP"
            )
            meshManager.sendPacket(hangupPacket)
            endCall(CallState.ENDED, "Call ended")
        }
    }

    fun toggleMute() {
        val current = _currentCall.value
        _currentCall.value = current.copy(isMuted = !current.isMuted)
    }

    fun toggleSpeaker() {
        val current = _currentCall.value
        val newSpeaker = !current.isSpeakerOn
        audioManager.isSpeakerphoneOn = newSpeaker
        _currentCall.value = current.copy(isSpeakerOn = newSpeaker)
    }

    @SuppressLint("MissingPermission")
    private fun startActiveAudioStream() {
        callStartTime = System.currentTimeMillis()
        sequenceNumber = 0
        val micGranted = com.example.core.permission.PermissionManager.hasMicrophonePermission(context)
        _currentCall.value = _currentCall.value.copy(
            state = CallState.ACTIVE,
            durationSeconds = 0,
            hasMicPermission = micGranted
        )

        // Notify foreground service that call is active
        try {
            com.example.service.BluetoothMeshService.startService(context, isCallActive = true)
        } catch (e: Exception) {}

        // Setup AudioTrack for playback
        val minBufferSizeOut = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG_OUT, AUDIO_FORMAT)
        val bufferSizeOut = maxOf(minBufferSizeOut, FRAME_SIZE_SAMPLES * 2 * 4)

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AUDIO_FORMAT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(CHANNEL_CONFIG_OUT)
                        .build()
                )
                .setBufferSizeInBytes(bufferSizeOut)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            audioTrack?.play()
        } catch (e: Exception) {
            // Error initializing playback
        }

        // Setup AudioRecord for capture only if permission granted
        val minBufferSizeIn = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG_IN, AUDIO_FORMAT)
        val bufferSizeIn = maxOf(minBufferSizeIn, FRAME_SIZE_SAMPLES * 2 * 4)

        if (micGranted) {
            try {
                val record = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG_IN,
                    AUDIO_FORMAT,
                    bufferSizeIn
                )
                if (record.state == AudioRecord.STATE_INITIALIZED) {
                    record.startRecording()
                    audioRecord = record
                } else {
                    record.release()
                    audioRecord = null
                }
            } catch (e: Exception) {
                audioRecord = null
            }
        } else {
            audioRecord = null
        }

        // Duration timer
        durationJob = scope.launch {
            while (isActive && _currentCall.value.state == CallState.ACTIVE) {
                delay(1000L)
                val duration = (System.currentTimeMillis() - callStartTime) / 1000L
                _currentCall.value = _currentCall.value.copy(durationSeconds = duration)
            }
        }

        // Microphone capture coroutine
        captureJob = scope.launch(Dispatchers.IO) {
            val pcmBuffer = ShortArray(FRAME_SIZE_SAMPLES)

            while (isActive && _currentCall.value.state == CallState.ACTIVE) {
                val record = audioRecord ?: break
                val readSamples = record.read(pcmBuffer, 0, FRAME_SIZE_SAMPLES)
                if (readSamples > 0) {
                    var maxAmp = 0
                    for (i in 0 until readSamples) {
                        val abs = Math.abs(pcmBuffer[i].toInt())
                        if (abs > maxAmp) maxAmp = abs
                    }
                    val normalizedAmp = (maxAmp / 32768f).coerceIn(0f, 1f)

                    if (!_currentCall.value.isMuted) {
                        // ADPCM encode
                        val adpcmBytes = encodePcmToAdpcm(pcmBuffer, readSamples)
                        val payload = Base64.encodeToString(adpcmBytes, Base64.NO_WRAP)

                        val packet = MeshPacket(
                            type = MeshPacketType.AUDIO_PACKET,
                            originDeviceId = cryptoManager.deviceId,
                            destinationDeviceId = _currentCall.value.peerDeviceId,
                            payload = payload
                        )
                        meshManager.sendPacket(packet)

                        _currentCall.value = _currentCall.value.copy(
                            currentAudioAmplitude = normalizedAmp,
                            packetsSent = _currentCall.value.packetsSent + 1
                        )
                    } else {
                        _currentCall.value = _currentCall.value.copy(currentAudioAmplitude = 0f)
                    }
                }
            }
        }
    }

    private fun playIncomingAudioPacket(base64Payload: String) {
        try {
            val adpcmBytes = Base64.decode(base64Payload, Base64.DEFAULT)
            val pcmSamples = decodeAdpcmToPcm(adpcmBytes)
            audioTrack?.write(pcmSamples, 0, pcmSamples.size)

            _currentCall.value = _currentCall.value.copy(
                packetsReceived = _currentCall.value.packetsReceived + 1
            )
        } catch (e: Exception) {
            // Decode error
        }
    }

    private fun endCall(endState: CallState, reason: String) {
        captureJob?.cancel()
        durationJob?.cancel()

        try {
            com.example.service.BluetoothMeshService.startService(context, isCallActive = false)
        } catch (e: Exception) {}

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {}
        audioRecord = null

        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {}
        audioTrack = null

        val call = _currentCall.value
        val duration = if (callStartTime > 0) (System.currentTimeMillis() - callStartTime) / 1000L else 0L

        if (call.callId.isNotEmpty()) {
            scope.launch {
                repository.saveCall(
                    CallHistoryEntity(
                        callId = call.callId,
                        peerDeviceId = call.peerDeviceId,
                        peerName = call.peerName,
                        startTime = callStartTime.takeIf { it > 0 } ?: System.currentTimeMillis(),
                        endTime = System.currentTimeMillis(),
                        durationSeconds = duration,
                        callType = if (call.isOutgoing) "OUTGOING" else "INCOMING",
                        callResult = if (duration > 0) "COMPLETED" else "CANCELLED"
                    )
                )
            }
        }

        _currentCall.value = call.copy(
            state = endState,
            currentAudioAmplitude = 0f
        )

        // Reset to IDLE after 2 seconds
        scope.launch {
            delay(2000L)
            _currentCall.value = ActiveCallInfo()
            callStartTime = 0
        }
    }

    // Lightweight IMA-ADPCM encoder (4:1 compression ratio for Bluetooth RFCOMM)
    private var prevSample = 0
    private var stepIndex = 0

    private val stepTable = intArrayOf(
        7, 8, 9, 10, 11, 12, 13, 14, 16, 17,
        19, 21, 23, 25, 28, 31, 34, 37, 41, 45,
        50, 55, 60, 66, 73, 80, 88, 97, 107, 118,
        130, 143, 157, 173, 190, 209, 230, 253, 279, 307,
        337, 371, 408, 449, 494, 544, 598, 658, 724, 796,
        876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066,
        2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358,
        5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899,
        15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767
    )

    private val indexTable = intArrayOf(
        -1, -1, -1, -1, 2, 4, 6, 8,
        -1, -1, -1, -1, 2, 4, 6, 8
    )

    private fun encodePcmToAdpcm(pcm: ShortArray, length: Int): ByteArray {
        val outBytes = ByteArray((length + 1) / 2)
        var outIdx = 0
        var bufferNibble = 0
        var isLowNibble = true

        for (i in 0 until length) {
            val sample = pcm[i].toInt()
            val step = stepTable[stepIndex]
            var diff = sample - prevSample
            var sign = 0

            if (diff < 0) {
                sign = 8
                diff = -diff
            }

            var delta = 0
            var vpdiff = (step shr 3)
            if (diff >= step) {
                delta = delta or 4
                diff -= step
                vpdiff += step
            }
            if (diff >= (step shr 1)) {
                delta = delta or 2
                diff -= (step shr 1)
                vpdiff += (step shr 1)
            }
            if (diff >= (step shr 2)) {
                delta = delta or 1
                vpdiff += (step shr 2)
            }

            delta = delta or sign
            prevSample = if (sign != 0) (prevSample - vpdiff).coerceIn(-32768, 32767)
            else (prevSample + vpdiff).coerceIn(-32768, 32767)

            stepIndex = (stepIndex + indexTable[delta]).coerceIn(0, 88)

            if (isLowNibble) {
                bufferNibble = delta and 0x0F
                isLowNibble = false
            } else {
                outBytes[outIdx++] = ((delta shl 4) or bufferNibble).toByte()
                isLowNibble = true
            }
        }
        return outBytes
    }

    private var decPrevSample = 0
    private var decStepIndex = 0

    private fun decodeAdpcmToPcm(adpcm: ByteArray): ShortArray {
        val pcm = ShortArray(adpcm.size * 2)
        var pcmIdx = 0

        for (b in adpcm) {
            val byteVal = b.toInt() and 0xFF
            val delta0 = byteVal and 0x0F
            val delta1 = (byteVal shr 4) and 0x0F

            for (delta in intArrayOf(delta0, delta1)) {
                val step = stepTable[decStepIndex]
                var vpdiff = step shr 3
                if ((delta and 4) != 0) vpdiff += step
                if ((delta and 2) != 0) vpdiff += (step shr 1)
                if ((delta and 1) != 0) vpdiff += (step shr 2)

                decPrevSample = if ((delta and 8) != 0) {
                    (decPrevSample - vpdiff).coerceIn(-32768, 32767)
                } else {
                    (decPrevSample + vpdiff).coerceIn(-32768, 32767)
                }

                decStepIndex = (decStepIndex + indexTable[delta]).coerceIn(0, 88)
                if (pcmIdx < pcm.size) {
                    pcm[pcmIdx++] = decPrevSample.toShort()
                }
            }
        }
        return pcm
    }
}
