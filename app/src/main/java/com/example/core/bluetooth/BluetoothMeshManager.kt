package com.example.core.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.core.crypto.CryptoManager
import com.example.core.database.DeviceEntity
import com.example.core.database.PeerEntity
import com.example.core.protocol.MeshPacket
import com.example.core.protocol.MeshPacketType
import com.example.data.MeshRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.Collections
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

enum class BluetoothStatus {
    UNSUPPORTED,
    DISABLED,
    PERMISSION_REQUIRED,
    READY,
    DISCOVERING,
    CONNECTING,
    CONNECTED,
    ERROR
}

data class MeshDiagnostics(
    val packetsSent: Long = 0,
    val packetsReceived: Long = 0,
    val packetsRelayed: Long = 0,
    val packetsDropped: Long = 0,
    val activeConnections: Int = 0,
    val lastError: String? = null,
    val lastActivityTimestamp: Long = System.currentTimeMillis()
)

class BluetoothMeshManager(
    private val context: Context,
    private val cryptoManager: CryptoManager,
    private val repository: MeshRepository,
    private val scope: CoroutineScope
) {
    companion object {
        val RONOVA_SERVICE_UUID: UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0803450c9a66")
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val MAX_DUPLICATE_CACHE = 1000
    }

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bluetoothAdapter: BluetoothAdapter? =
        bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()

    private val _bluetoothStatus = MutableStateFlow(BluetoothStatus.READY)
    val bluetoothStatus: StateFlow<BluetoothStatus> = _bluetoothStatus.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoveredDevices.asStateFlow()

    private val _deviceRssiMap = MutableStateFlow<Map<String, Int>>(emptyMap())
    val deviceRssiMap: StateFlow<Map<String, Int>> = _deviceRssiMap.asStateFlow()

    private val _isDiscoverable = MutableStateFlow(true)
    val isDiscoverable: StateFlow<Boolean> = _isDiscoverable.asStateFlow()

    private val _diagnostics = MutableStateFlow(MeshDiagnostics())
    val diagnostics: StateFlow<MeshDiagnostics> = _diagnostics.asStateFlow()

    private val _incomingPackets = MutableSharedFlow<MeshPacket>(extraBufferCapacity = 64)
    val incomingPackets: SharedFlow<MeshPacket> = _incomingPackets.asSharedFlow()

    // Active RFCOMM connections: DeviceId -> ConnectedSocketInfo
    data class ConnectedPeer(
        val socket: BluetoothSocket,
        val outputStream: DataOutputStream,
        val device: BluetoothDevice,
        var peerDeviceId: String = "",
        var peerPublicKeyBase64: String = ""
    )

    private val activeSockets = ConcurrentHashMap<String, ConnectedPeer>() // key is deviceId or bluetoothAddress
    private val processedPacketIds = Collections.synchronizedSet(LinkedHashSet<String>())

    private var serverJob: Job? = null
    private var isReceiverRegistered = false
    var isRelayEnabled: Boolean = true

    private val discoveryReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(c: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(
                                BluetoothDevice.EXTRA_DEVICE,
                                BluetoothDevice::class.java
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }

                    device?.let { dev ->
                        val current = _discoveredDevices.value.toMutableList()
                        val rssiVal = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toInt()
                        if (rssiVal != Short.MIN_VALUE.toInt()) {
                            _deviceRssiMap.value = _deviceRssiMap.value + (dev.address to rssiVal)
                        }
                        if (current.none { it.address == dev.address }) {
                            current.add(dev)
                            _discoveredDevices.value = current

                            scope.launch {
                                repository.savePeer(
                                    PeerEntity(
                                        bluetoothAddress = dev.address,
                                        deviceId = "",
                                        deviceName = dev.name ?: "Unknown Bluetooth Device",
                                        rssi = if (rssiVal != Short.MIN_VALUE.toInt()) rssiVal else 0,
                                        isConnected = false,
                                        lastSeen = System.currentTimeMillis()
                                    )
                                )
                            }
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _bluetoothStatus.value = BluetoothStatus.DISCOVERING
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    if (activeSockets.isNotEmpty()) {
                        _bluetoothStatus.value = BluetoothStatus.CONNECTED
                    } else {
                        _bluetoothStatus.value = BluetoothStatus.READY
                    }
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    if (state == BluetoothAdapter.STATE_OFF) {
                        _bluetoothStatus.value = BluetoothStatus.DISABLED
                        _isDiscoverable.value = false
                        disconnectAll()
                    } else if (state == BluetoothAdapter.STATE_ON) {
                        _bluetoothStatus.value = BluetoothStatus.READY
                        _isDiscoverable.value = true
                        startServer()
                    }
                }
                BluetoothAdapter.ACTION_SCAN_MODE_CHANGED -> {
                    val mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.SCAN_MODE_NONE)
                    _isDiscoverable.value = (mode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) || (serverJob?.isActive == true)
                }
            }
        }
    }

    init {
        checkStatus()
        registerReceiver()
        checkDiscoverability()
    }

    fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun checkStatus(): BluetoothStatus {
        if (bluetoothAdapter == null) {
            _bluetoothStatus.value = BluetoothStatus.UNSUPPORTED
            return BluetoothStatus.UNSUPPORTED
        }
        if (!hasRequiredPermissions()) {
            _bluetoothStatus.value = BluetoothStatus.PERMISSION_REQUIRED
            return BluetoothStatus.PERMISSION_REQUIRED
        }
        if (!bluetoothAdapter.isEnabled) {
            _bluetoothStatus.value = BluetoothStatus.DISABLED
            return BluetoothStatus.DISABLED
        }
        if (activeSockets.isNotEmpty()) {
            _bluetoothStatus.value = BluetoothStatus.CONNECTED
            return BluetoothStatus.CONNECTED
        }
        _bluetoothStatus.value = BluetoothStatus.READY
        return BluetoothStatus.READY
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        if (!hasRequiredPermissions() || bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            checkStatus()
            return
        }

        registerReceiver()
        _discoveredDevices.value = emptyList()

        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        bluetoothAdapter.startDiscovery()
        _bluetoothStatus.value = BluetoothStatus.DISCOVERING
    }

    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        if (hasRequiredPermissions() && bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter.cancelDiscovery()
        }
        if (activeSockets.isNotEmpty()) {
            _bluetoothStatus.value = BluetoothStatus.CONNECTED
        } else {
            _bluetoothStatus.value = BluetoothStatus.READY
        }
    }

    @SuppressLint("MissingPermission")
    fun getBondedDevices(): List<BluetoothDevice> {
        return if (hasRequiredPermissions() && bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
            bluetoothAdapter.bondedDevices?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun clearDiscoveredDevices() {
        _discoveredDevices.value = emptyList()
    }

    @SuppressLint("MissingPermission")
    fun checkDiscoverability(): Boolean {
        val adapter = bluetoothAdapter
        val isDisc = if (hasRequiredPermissions() && adapter != null && adapter.isEnabled) {
            (adapter.scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) || (serverJob?.isActive == true)
        } else {
            false
        }
        _isDiscoverable.value = isDisc
        return isDisc
    }

    fun setDiscoverable(discoverable: Boolean) {
        _isDiscoverable.value = discoverable
        if (discoverable) {
            startServer()
        }
    }

    @SuppressLint("MissingPermission")
    fun startServer() {
        if (!hasRequiredPermissions() || bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            return
        }
        if (serverJob?.isActive == true) return

        serverJob = scope.launch(Dispatchers.IO) {
            var serverSocket: BluetoothServerSocket? = null
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                    "RonovaMesh",
                    RONOVA_SERVICE_UUID
                )
            } catch (e: Exception) {
                try {
                    serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                        "RonovaMeshInsecure",
                        SPP_UUID
                    )
                } catch (e2: Exception) {
                    recordError("Failed to start RFCOMM server: ${e2.message}")
                    return@launch
                }
            }

            while (isActive && serverSocket != null) {
                try {
                    val socket = serverSocket.accept()
                    if (socket != null) {
                        handleConnectedSocket(socket)
                    }
                } catch (e: IOException) {
                    if (!isActive) break
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        scope.launch(Dispatchers.IO) {
            _bluetoothStatus.value = BluetoothStatus.CONNECTING
            stopDiscovery()

            var socket: BluetoothSocket? = null
            try {
                socket = device.createRfcommSocketToServiceRecord(RONOVA_SERVICE_UUID)
                socket.connect()
            } catch (e: Exception) {
                try {
                    socket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID)
                    socket.connect()
                } catch (e2: Exception) {
                    recordError("Connection failed to ${device.name ?: device.address}: ${e2.message}")
                    if (activeSockets.isEmpty()) {
                        _bluetoothStatus.value = BluetoothStatus.READY
                    }
                    return@launch
                }
            }

            socket?.let { handleConnectedSocket(it) }
        }
    }

    private fun handleConnectedSocket(socket: BluetoothSocket) {
        scope.launch(Dispatchers.IO) {
            val device = socket.remoteDevice
            val out = DataOutputStream(socket.outputStream)
            val input = DataInputStream(socket.inputStream)

            val peer = ConnectedPeer(
                socket = socket,
                outputStream = out,
                device = device
            )
            val socketKey = device.address
            activeSockets[socketKey] = peer

            updateDiagnostics { copy(activeConnections = activeSockets.size) }
            _bluetoothStatus.value = BluetoothStatus.CONNECTED

            repository.savePeer(
                PeerEntity(
                    bluetoothAddress = device.address,
                    deviceId = "",
                    deviceName = device.name ?: "Peer ${device.address.takeLast(5)}",
                    isConnected = true,
                    lastSeen = System.currentTimeMillis()
                )
            )

            // Send initial Beacon packet
            sendBeacon(peer)

            // Start reading incoming frames
            try {
                while (isActive && socket.isConnected) {
                    val length = input.readInt()
                    if (length in 1..1048576) { // Max 1MB packet limit
                        val buffer = ByteArray(length)
                        input.readFully(buffer)
                        val packet = MeshPacket.fromBytes(buffer)
                        onPacketReceived(packet, peer)
                    } else if (length < 0) {
                        break
                    }
                }
            } catch (e: Exception) {
                // Socket disconnected
            } finally {
                cleanupSocket(socketKey, peer)
            }
        }
    }

    private suspend fun sendBeacon(peer: ConnectedPeer) {
        val beacon = MeshPacket(
            type = MeshPacketType.BEACON,
            originDeviceId = cryptoManager.deviceId,
            destinationDeviceId = "*",
            senderPublicKeyBase64 = cryptoManager.publicKeyBase64,
            payload = "RONOVA_PEER"
        )
        sendPacketDirect(peer, beacon)
    }

    private suspend fun onPacketReceived(packet: MeshPacket, sourcePeer: ConnectedPeer) {
        updateDiagnostics { copy(packetsReceived = packetsReceived + 1) }

        // Loop detection
        if (processedPacketIds.contains(packet.packetId)) {
            return
        }
        trackProcessedPacket(packet.packetId)

        // Handle beacon for handshake and peer registration
        if (packet.type == MeshPacketType.BEACON || packet.type == MeshPacketType.BEACON_RESP) {
            sourcePeer.peerDeviceId = packet.originDeviceId
            sourcePeer.peerPublicKeyBase64 = packet.senderPublicKeyBase64

            // Re-index socket by peer deviceId
            activeSockets[packet.originDeviceId] = sourcePeer

            repository.saveDevice(
                DeviceEntity(
                    deviceId = packet.originDeviceId,
                    publicKeyBase64 = packet.senderPublicKeyBase64,
                    displayName = sourcePeer.device.name ?: packet.originDeviceId,
                    trustStatus = "UNTRUSTED",
                    lastSeenTimestamp = System.currentTimeMillis(),
                    bluetoothAddress = sourcePeer.device.address,
                    isDirectPeer = true,
                    hopDistance = 1
                )
            )

            repository.savePeer(
                PeerEntity(
                    bluetoothAddress = sourcePeer.device.address,
                    deviceId = packet.originDeviceId,
                    deviceName = sourcePeer.device.name ?: packet.originDeviceId,
                    isConnected = true,
                    lastSeen = System.currentTimeMillis()
                )
            )

            if (packet.type == MeshPacketType.BEACON) {
                val resp = MeshPacket(
                    type = MeshPacketType.BEACON_RESP,
                    originDeviceId = cryptoManager.deviceId,
                    destinationDeviceId = packet.originDeviceId,
                    senderPublicKeyBase64 = cryptoManager.publicKeyBase64,
                    payload = "RONOVA_ACK"
                )
                sendPacketDirect(sourcePeer, resp)
            }
            return
        }

        // Check if destination is THIS device
        if (packet.destinationDeviceId == cryptoManager.deviceId || packet.destinationDeviceId == "*") {
            _incomingPackets.emit(packet)
            return
        }

        // Multi-hop Relay Store-and-Forward
        if (isRelayEnabled && packet.canHopFurther() && !packet.isExpired()) {
            val relayedPacket = packet.withIncrementedHop(cryptoManager.deviceId)
            updateDiagnostics { copy(packetsRelayed = packetsRelayed + 1) }
            relayPacket(relayedPacket)
        } else {
            updateDiagnostics { copy(packetsDropped = packetsDropped + 1) }
        }
    }

    suspend fun relayPacket(packet: MeshPacket) {
        // Check if destination is directly connected
        val targetPeer = activeSockets[packet.destinationDeviceId]
        if (targetPeer != null) {
            sendPacketDirect(targetPeer, packet)
            return
        }

        // Otherwise flood to all other connected peers except those in route path
        var forwarded = false
        activeSockets.values.forEach { peer ->
            if (!packet.routePath.contains(peer.peerDeviceId)) {
                sendPacketDirect(peer, packet)
                forwarded = true
            }
        }

        if (!forwarded) {
            // Queue for store-and-forward when a route appears
            repository.enqueueRelayPacket(
                com.example.core.database.RelayQueueEntity(
                    packetId = packet.packetId,
                    destinationDeviceId = packet.destinationDeviceId,
                    originDeviceId = packet.originDeviceId,
                    rawPacketJson = packet.toJsonObject().toString(),
                    hopCount = packet.currentHop,
                    maxHops = packet.hopLimit,
                    timestamp = packet.timestamp,
                    expiryTimestamp = packet.expiryTimestamp,
                    status = "PENDING"
                )
            )
        }
    }

    suspend fun sendPacket(packet: MeshPacket): Boolean {
        // Attempt direct send first if peer is connected
        val directPeer = activeSockets[packet.destinationDeviceId]
        if (directPeer != null) {
            val sent = sendPacketDirect(directPeer, packet)
            if (sent) return true
        }

        // Multicast / multi-hop relay to connected peers
        var sentAny = false
        activeSockets.values.forEach { peer ->
            val ok = sendPacketDirect(peer, packet)
            if (ok) sentAny = true
        }
        return sentAny
    }

    private suspend fun sendPacketDirect(peer: ConnectedPeer, packet: MeshPacket): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val bytes = packet.toBytes()
                synchronized(peer.outputStream) {
                    peer.outputStream.writeInt(bytes.size)
                    peer.outputStream.write(bytes)
                    peer.outputStream.flush()
                }
                updateDiagnostics { copy(packetsSent = packetsSent + 1) }
                true
            } catch (e: Exception) {
                recordError("Failed to send packet ${packet.packetId} to ${peer.device.address}: ${e.message}")
                false
            }
        }
    }

    private fun trackProcessedPacket(packetId: String) {
        if (processedPacketIds.size > MAX_DUPLICATE_CACHE) {
            val it = processedPacketIds.iterator()
            if (it.hasNext()) {
                it.next()
                it.remove()
            }
        }
        processedPacketIds.add(packetId)
    }

    private fun cleanupSocket(key: String, peer: ConnectedPeer) {
        try {
            peer.outputStream.close()
            peer.socket.close()
        } catch (e: Exception) {
            // Ignored
        }
        activeSockets.remove(key)
        if (peer.peerDeviceId.isNotEmpty()) {
            activeSockets.remove(peer.peerDeviceId)
        }
        scope.launch {
            repository.setPeerConnected(peer.device.address, false)
        }
        updateDiagnostics { copy(activeConnections = activeSockets.size) }
        if (activeSockets.isEmpty()) {
            _bluetoothStatus.value = BluetoothStatus.READY
        }
    }

    fun disconnectAll() {
        activeSockets.values.forEach { peer ->
            try {
                peer.outputStream.close()
                peer.socket.close()
            } catch (e: Exception) {}
        }
        activeSockets.clear()
        scope.launch {
            repository.disconnectAllPeers()
        }
        _bluetoothStatus.value = BluetoothStatus.READY
        updateDiagnostics { copy(activeConnections = 0) }
    }

    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
            }
            context.registerReceiver(discoveryReceiver, filter)
            isReceiverRegistered = true
        }
    }

    fun unregisterReceiver() {
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(discoveryReceiver)
            } catch (e: Exception) {}
            isReceiverRegistered = false
        }
    }

    private fun updateDiagnostics(update: MeshDiagnostics.() -> MeshDiagnostics) {
        _diagnostics.value = _diagnostics.value.update().copy(
            lastActivityTimestamp = System.currentTimeMillis()
        )
    }

    private fun recordError(msg: String) {
        updateDiagnostics { copy(lastError = msg) }
        scope.launch {
            repository.logSecurityEvent("BLUETOOTH_ERROR", msg)
        }
    }

    fun getActivePeerList(): List<String> {
        return activeSockets.keys().toList()
    }
}
