package com.example.viewmodel

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.RonovaApplication
import com.example.core.audio.ActiveCallInfo
import com.example.core.audio.CallState
import com.example.core.bluetooth.BluetoothStatus
import com.example.core.bluetooth.MeshDiagnostics
import com.example.core.database.ConversationEntity
import com.example.core.database.DeviceEntity
import com.example.core.database.MessageEntity
import com.example.core.database.PeerEntity
import com.example.core.database.RelayQueueEntity
import com.example.core.protocol.MeshPacket
import com.example.core.protocol.MeshPacketType
import com.example.data.RetentionPolicy
import com.example.service.BluetoothMeshService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID

class RonovaViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as RonovaApplication
    val cryptoManager = app.cryptoManager
    val meshManager = app.bluetoothMeshManager
    val audioCallManager = app.audioCallManager
    val repository = app.repository

    // State flows
    val myDeviceId: String = cryptoManager.deviceId
    val myPublicKeyBase64: String = cryptoManager.publicKeyBase64

    val bluetoothStatus: StateFlow<BluetoothStatus> = meshManager.bluetoothStatus
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = meshManager.discoveredDevices
    val deviceRssiMap: StateFlow<Map<String, Int>> = meshManager.deviceRssiMap
    val isDiscoverable: StateFlow<Boolean> = meshManager.isDiscoverable
    val diagnostics: StateFlow<MeshDiagnostics> = meshManager.diagnostics
    val currentCall: StateFlow<ActiveCallInfo> = audioCallManager.currentCall

    val conversations: StateFlow<List<ConversationEntity>> = repository.conversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trustedDevices: StateFlow<List<DeviceEntity>> = repository.trustedDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockedDevices: StateFlow<List<DeviceEntity>> = repository.blockedDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDevices: StateFlow<List<DeviceEntity>> = repository.allDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingMessages: StateFlow<List<MessageEntity>> = repository.pendingMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingRelayPackets: StateFlow<List<RelayQueueEntity>> = repository.pendingRelayPackets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val connectedPeers: StateFlow<List<PeerEntity>> = repository.connectedPeers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val callHistory = repository.callHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val securityEvents = repository.securityEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedMessages: StateFlow<List<MessageEntity>> = repository.archivedMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedMessagesCount: StateFlow<Int> = repository.archivedMessagesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _retentionPolicy = MutableStateFlow(repository.getRetentionPolicy())
    val retentionPolicy: StateFlow<RetentionPolicy> = _retentionPolicy.asStateFlow()

    private val _olderThan30DaysCount = MutableStateFlow(0)
    val olderThan30DaysCount: StateFlow<Int> = _olderThan30DaysCount.asStateFlow()

    private val _isRelayOptedIn = MutableStateFlow(true)
    val isRelayOptedIn: StateFlow<Boolean> = _isRelayOptedIn.asStateFlow()

    private val _maxHops = MutableStateFlow(5)
    val maxHops: StateFlow<Int> = _maxHops.asStateFlow()

    private val _isForegroundServiceRunning = MutableStateFlow(false)
    val isForegroundServiceRunning: StateFlow<Boolean> = _isForegroundServiceRunning.asStateFlow()

    private val _currentLanguage = MutableStateFlow(com.example.core.locale.LocaleHelper.LANGUAGE_BANGLA)
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _isSetupCompleted = MutableStateFlow(repository.isSetupCompleted())
    val isSetupCompleted: StateFlow<Boolean> = _isSetupCompleted.asStateFlow()

    private val _setupStep = MutableStateFlow(repository.getSetupStep())
    val setupStep: StateFlow<String> = _setupStep.asStateFlow()

    fun completeSetup() {
        repository.setSetupCompleted(true)
        _isSetupCompleted.value = true
        _setupStep.value = "COMPLETED"
    }

    fun updateSetupStep(step: String) {
        repository.setSetupStep(step)
        _setupStep.value = step
    }

    fun resetSetup() {
        repository.resetSetup()
        _isSetupCompleted.value = false
        _setupStep.value = "WELCOME"
    }

    fun setLanguage(languageCode: String) {
        com.example.core.locale.LocaleHelper.setLanguage(app, languageCode)
        _currentLanguage.value = languageCode
    }

    fun hasMicrophonePermission(): Boolean {
        return com.example.core.permission.PermissionManager.hasMicrophonePermission(app)
    }

    fun hasBluetoothPermissions(): Boolean {
        return com.example.core.permission.PermissionManager.hasBluetoothPermissions(app)
    }

    fun isBluetoothEnabled(): Boolean {
        return com.example.core.permission.PermissionManager.isBluetoothEnabled(app)
    }

    init {
        // Collect incoming packets to handle text messages and delivery receipts
        viewModelScope.launch {
            meshManager.incomingPackets.collect { packet ->
                handleIncomingPacket(packet)
            }
        }

        // Auto execute 30-day retention cleanup on startup and refresh count
        viewModelScope.launch(Dispatchers.IO) {
            refreshRetentionStats()
            repository.runRetentionPolicyCleanup()
            refreshRetentionStats()
        }

        // Start listening server
        meshManager.startServer()
    }

    private suspend fun handleIncomingPacket(packet: MeshPacket) {
        when (packet.type) {
            MeshPacketType.TEXT_MESSAGE -> {
                // Check if device is blocked
                val senderDevice = repository.getDeviceById(packet.originDeviceId)
                if (senderDevice?.trustStatus == "BLOCKED") {
                    repository.logSecurityEvent(
                        "BLOCKED_MESSAGE_DROPPED",
                        "Dropped message from blocked device ${packet.originDeviceId}",
                        packet.originDeviceId
                    )
                    return
                }

                var decryptedText = packet.payload
                try {
                    if (packet.senderPublicKeyBase64.isNotEmpty()) {
                        val senderKey = cryptoManager.decodePublicKey(packet.senderPublicKeyBase64)
                        decryptedText = cryptoManager.decryptFromPeer(packet.payload, senderKey)
                    }
                } catch (e: Exception) {
                    // Could be unencrypted or payload decode error
                }

                val msg = MessageEntity(
                    messageId = packet.packetId,
                    conversationId = packet.originDeviceId,
                    senderDeviceId = packet.originDeviceId,
                    recipientDeviceId = myDeviceId,
                    content = decryptedText,
                    timestamp = packet.timestamp,
                    deliveryState = "DELIVERED",
                    hopCount = packet.currentHop,
                    routeNodesJson = packet.toJsonObject().optJSONArray("rp")?.toString() ?: "[]",
                    isOutgoing = false,
                    encryptedPayload = packet.payload
                )
                repository.saveMessage(msg)

                // Send ACK_DELIVERED back to sender
                val ack = MeshPacket(
                    type = MeshPacketType.ACK_DELIVERED,
                    originDeviceId = myDeviceId,
                    destinationDeviceId = packet.originDeviceId,
                    senderPublicKeyBase64 = myPublicKeyBase64,
                    payload = JSONObject().apply {
                        put("ackMessageId", packet.packetId)
                        put("hops", packet.currentHop)
                    }.toString()
                )
                meshManager.sendPacket(ack)
            }
            MeshPacketType.ACK_DELIVERED -> {
                try {
                    val obj = JSONObject(packet.payload)
                    val ackMessageId = obj.optString("ackMessageId", "")
                    val hops = obj.optInt("hops", packet.currentHop)
                    if (ackMessageId.isNotEmpty()) {
                        repository.updateMessageDeliveryState(
                            messageId = ackMessageId,
                            state = "DELIVERED",
                            hopCount = hops,
                            routeJson = packet.toJsonObject().optJSONArray("rp")?.toString() ?: "[]"
                        )
                    }
                } catch (e: Exception) {
                    // Fallback using packetId
                    repository.updateMessageDeliveryState(
                        messageId = packet.packetId,
                        state = "DELIVERED",
                        hopCount = packet.currentHop
                    )
                }
            }
            else -> {}
        }
    }

    fun sendMessage(recipientDeviceId: String, text: String) {
        if (text.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            val messageId = UUID.randomUUID().toString()
            var payload = text
            val recipientDevice = repository.getDeviceById(recipientDeviceId)

            if (recipientDevice != null && recipientDevice.publicKeyBase64.isNotEmpty()) {
                try {
                    val peerKey = cryptoManager.decodePublicKey(recipientDevice.publicKeyBase64)
                    payload = cryptoManager.encryptForPeer(text, peerKey)
                } catch (e: Exception) {
                    // If encryption fails, fallback safely
                }
            }

            val msg = MessageEntity(
                messageId = messageId,
                conversationId = recipientDeviceId,
                senderDeviceId = myDeviceId,
                recipientDeviceId = recipientDeviceId,
                content = text,
                timestamp = System.currentTimeMillis(),
                deliveryState = "QUEUED",
                hopCount = 0,
                isOutgoing = true,
                encryptedPayload = payload
            )
            repository.saveMessage(msg)

            val packet = MeshPacket(
                packetId = messageId,
                type = MeshPacketType.TEXT_MESSAGE,
                originDeviceId = myDeviceId,
                destinationDeviceId = recipientDeviceId,
                senderPublicKeyBase64 = myPublicKeyBase64,
                payload = payload,
                hopLimit = _maxHops.value
            )

            val sentToAny = meshManager.sendPacket(packet)
            if (sentToAny) {
                repository.updateMessageDeliveryState(messageId, "RELAYED", 1)
            } else {
                // Enqueue in relay queue for store-and-forward
                repository.enqueueRelayPacket(
                    RelayQueueEntity(
                        packetId = messageId,
                        destinationDeviceId = recipientDeviceId,
                        originDeviceId = myDeviceId,
                        rawPacketJson = packet.toJsonObject().toString(),
                        hopCount = 0,
                        maxHops = _maxHops.value,
                        timestamp = System.currentTimeMillis(),
                        expiryTimestamp = packet.expiryTimestamp,
                        status = "PENDING"
                    )
                )
            }
        }
    }

    fun retryPendingMessage(message: MessageEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val packet = MeshPacket(
                packetId = message.messageId,
                type = MeshPacketType.TEXT_MESSAGE,
                originDeviceId = myDeviceId,
                destinationDeviceId = message.recipientDeviceId,
                senderPublicKeyBase64 = myPublicKeyBase64,
                payload = message.encryptedPayload.ifEmpty { message.content },
                hopLimit = _maxHops.value
            )
            val sent = meshManager.sendPacket(packet)
            if (sent) {
                repository.updateMessageDeliveryState(message.messageId, "RELAYED", 1)
            }
        }
    }

    fun resendPendingPackets() {
        viewModelScope.launch(Dispatchers.IO) {
            val pending = repository.pendingRelayPackets
            // trigger forward on all connected peers
            meshManager.startServer()
        }
    }

    fun startDiscovery() {
        meshManager.startDiscovery()
    }

    fun stopDiscovery() {
        meshManager.stopDiscovery()
    }

    fun getBondedDevices(): List<BluetoothDevice> {
        return meshManager.getBondedDevices()
    }

    fun clearDiscoveredDevices() {
        meshManager.clearDiscoveredDevices()
    }

    fun checkBluetoothStatus(): BluetoothStatus {
        return meshManager.checkStatus()
    }

    fun checkDiscoverability(): Boolean {
        return meshManager.checkDiscoverability()
    }

    fun setDiscoverable(discoverable: Boolean) {
        meshManager.setDiscoverable(discoverable)
    }

    fun connectDevice(device: BluetoothDevice) {
        meshManager.connectToDevice(device)
    }

    fun disconnectAll() {
        meshManager.disconnectAll()
    }

    fun initiateVoiceCall(peerDeviceId: String, peerName: String) {
        audioCallManager.initiateCall(peerDeviceId, peerName)
    }

    fun acceptVoiceCall() {
        audioCallManager.acceptIncomingCall()
    }

    fun rejectVoiceCall() {
        audioCallManager.rejectIncomingCall()
    }

    fun hangupVoiceCall() {
        audioCallManager.hangupCall()
    }

    fun toggleMute() {
        audioCallManager.toggleMute()
    }

    fun toggleSpeaker() {
        audioCallManager.toggleSpeaker()
    }

    fun setDeviceTrust(deviceId: String, status: String) {
        viewModelScope.launch {
            repository.updateDeviceTrust(deviceId, status)
        }
    }

    fun updateDeviceDisplayName(deviceId: String, name: String) {
        viewModelScope.launch {
            repository.updateDeviceName(deviceId, name)
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            repository.deleteConversation(conversationId)
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun clearRelayQueue() {
        viewModelScope.launch {
            repository.clearRelayQueue()
        }
    }

    fun clearCallHistory() {
        viewModelScope.launch {
            repository.clearCallHistory()
        }
    }

    fun clearSecurityEvents() {
        viewModelScope.launch {
            repository.clearSecurityEvents()
        }
    }

    fun setRelayOptIn(enabled: Boolean) {
        _isRelayOptedIn.value = enabled
        meshManager.isRelayEnabled = enabled
    }

    fun setMaxHops(hops: Int) {
        _maxHops.value = hops.coerceIn(1, 10)
    }

    fun toggleForegroundService(enable: Boolean) {
        _isForegroundServiceRunning.value = enable
        if (enable) {
            BluetoothMeshService.startService(app)
        } else {
            BluetoothMeshService.stopService(app)
        }
    }

    fun getDatabaseSizeBytes(): Long = repository.getDatabaseSizeBytes()

    fun refreshRetentionStats() {
        viewModelScope.launch(Dispatchers.IO) {
            _olderThan30DaysCount.value = repository.getMessagesOlderThan30DaysCount()
        }
    }

    fun setRetentionPolicy(policy: RetentionPolicy) {
        _retentionPolicy.value = policy
        repository.setRetentionPolicy(policy)
        viewModelScope.launch(Dispatchers.IO) {
            if (policy != RetentionPolicy.KEEP_ALL) {
                repository.runRetentionPolicyCleanup()
            }
            refreshRetentionStats()
        }
    }

    fun runRetentionCleanupNow(onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = repository.runRetentionPolicyCleanup()
            refreshRetentionStats()
            withContext(Dispatchers.Main) {
                onComplete(count)
            }
        }
    }

    fun deleteMessagesOlderThan30Days(onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = repository.deleteMessagesOlderThan30Days()
            refreshRetentionStats()
            withContext(Dispatchers.Main) {
                onComplete(count)
            }
        }
    }

    fun archiveMessagesOlderThan30Days(onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = repository.archiveMessagesOlderThan30Days()
            refreshRetentionStats()
            withContext(Dispatchers.Main) {
                onComplete(count)
            }
        }
    }

    fun unarchiveMessage(messageId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.unarchiveMessage(messageId)
            refreshRetentionStats()
        }
    }

    fun unarchiveAllMessages(onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = repository.unarchiveAllMessages()
            refreshRetentionStats()
            withContext(Dispatchers.Main) {
                onComplete(count)
            }
        }
    }

    fun deleteAllArchivedMessages(onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = repository.deleteAllArchivedMessages()
            refreshRetentionStats()
            withContext(Dispatchers.Main) {
                onComplete(count)
            }
        }
    }

    fun resetCryptographicIdentity() {
        viewModelScope.launch {
            cryptoManager.resetIdentity()
            repository.logSecurityEvent("KEYPAIR_REGENERATED", "Generated new cryptographic identity")
        }
    }
}
