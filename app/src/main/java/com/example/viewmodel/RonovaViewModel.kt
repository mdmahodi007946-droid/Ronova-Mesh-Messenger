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
import com.example.core.database.ApprovedContactEntity
import com.example.core.database.ConversationEntity
import com.example.core.database.DeviceEntity
import com.example.core.database.MessageEntity
import com.example.core.database.PeerEntity
import com.example.core.database.RelayQueueEntity
import com.example.core.locale.LocaleHelper
import com.example.core.network.RoutePreference
import com.example.core.network.TransportRoute
import com.example.core.protocol.MeshPacket
import com.example.core.protocol.MeshPacketType
import com.example.data.RetentionPolicy
import com.example.service.BluetoothMeshService
import android.app.Activity
import android.content.Context
import com.example.core.firebase.CloudMessage
import com.example.core.firebase.FriendRequest
import com.example.core.firebase.Friendship
import com.example.core.firebase.UserProfile
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.util.UUID

sealed interface DirectoryUiState {
    object Loading : DirectoryUiState
    data class Success(val users: List<UserProfile>, val isSearch: Boolean) : DirectoryUiState
    data class Empty(val isSearch: Boolean) : DirectoryUiState
    data class Error(val message: String) : DirectoryUiState
}

class RonovaViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as RonovaApplication
    val cryptoManager = app.cryptoManager
    val meshManager = app.bluetoothMeshManager
    val audioCallManager = app.audioCallManager
    val repository = app.repository
    val internetTransportManager = app.internetTransportManager
    val firebaseAuthManager = app.firebaseAuthManager
    val firestoreUserRepository = app.firestoreUserRepository
    val firestoreFriendRepository = app.firestoreFriendRepository
    val firestoreChatRepository = app.firestoreChatRepository

    val currentUser: StateFlow<FirebaseUser?> = firebaseAuthManager.currentUserState

    private val _myProfile = MutableStateFlow<UserProfile?>(null)
    val myProfile: StateFlow<UserProfile?> = _myProfile.asStateFlow()

    private val _friends = MutableStateFlow<List<UserProfile>>(emptyList())
    val friends: StateFlow<List<UserProfile>> = _friends.asStateFlow()

    private val _receivedRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val receivedRequests: StateFlow<List<FriendRequest>> = _receivedRequests.asStateFlow()

    private val _sentRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val sentRequests: StateFlow<List<FriendRequest>> = _sentRequests.asStateFlow()

    private val _directoryUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val directoryUsers: StateFlow<List<UserProfile>> = _directoryUsers.asStateFlow()

    private val _directoryUiState = MutableStateFlow<DirectoryUiState>(DirectoryUiState.Loading)
    val directoryUiState: StateFlow<DirectoryUiState> = _directoryUiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _directoryRefreshTrigger = MutableStateFlow(0)

    // Core Identity
    val myDeviceId: String = cryptoManager.deviceId
    val myPublicKeyBase64: String = cryptoManager.publicKeyBase64

    // Bluetooth Mesh State
    val bluetoothStatus: StateFlow<BluetoothStatus> = meshManager.bluetoothStatus
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = meshManager.discoveredDevices
    val deviceRssiMap: StateFlow<Map<String, Int>> = meshManager.deviceRssiMap
    val isDiscoverable: StateFlow<Boolean> = meshManager.isDiscoverable
    val diagnostics: StateFlow<MeshDiagnostics> = meshManager.diagnostics
    val currentCall: StateFlow<ActiveCallInfo> = audioCallManager.currentCall

    // Internet & Connectivity State (PRD v2.1 Section 2.3)
    val isInternetAvailable: StateFlow<Boolean> = internetTransportManager.isInternetAvailable
    val isWifi: StateFlow<Boolean> = internetTransportManager.isWifi
    val isCellular: StateFlow<Boolean> = internetTransportManager.isCellular

    private val _routePreference = MutableStateFlow(repository.getRoutePreference())
    val routePreference: StateFlow<RoutePreference> = _routePreference.asStateFlow()

    fun setRoutePreference(pref: RoutePreference) {
        _routePreference.value = pref
        repository.setRoutePreference(pref)
    }

    // Profile Lock & Authorization State (PRD v2.1 Section 6 & 7)
    private val _isProfileLocked = MutableStateFlow(cryptoManager.isProfileLocked())
    val isProfileLocked: StateFlow<Boolean> = _isProfileLocked.asStateFlow()

    private val _authCredential = MutableStateFlow(cryptoManager.getOrGenerateAuthorizationCredential())
    val authCredential: StateFlow<String> = _authCredential.asStateFlow()

    fun setProfileLocked(locked: Boolean) {
        _isProfileLocked.value = locked
        cryptoManager.setProfileLocked(locked)
        viewModelScope.launch {
            repository.logSecurityEvent(
                "PROFILE_LOCK_CHANGED",
                "Profile Lock set to: ${if (locked) "LOCKED" else "UNLOCKED"}"
            )
        }
    }

    fun rotateAuthCredential(): String {
        val newCode = cryptoManager.rotateAuthorizationCredential()
        _authCredential.value = newCode
        viewModelScope.launch {
            repository.logSecurityEvent("AUTH_CREDENTIAL_ROTATED", "Rotated authorization credential")
        }
        return newCode
    }

    // Discoverability & Privacy Preferences (PRD v2.1 Section 5 & 7)
    private val _radarVisibility = MutableStateFlow(repository.getRadarVisibility())
    val radarVisibility: StateFlow<Boolean> = _radarVisibility.asStateFlow()

    fun setRadarVisibility(visible: Boolean) {
        _radarVisibility.value = visible
        repository.setRadarVisibility(visible)
    }

    private val _showDeviceName = MutableStateFlow(repository.getShowDeviceName())
    val showDeviceName: StateFlow<Boolean> = _showDeviceName.asStateFlow()

    fun setShowDeviceName(show: Boolean) {
        _showDeviceName.value = show
        repository.setShowDeviceName(show)
    }

    private val _allowIncomingCalls = MutableStateFlow(repository.getAllowIncomingCalls())
    val allowIncomingCalls: StateFlow<Boolean> = _allowIncomingCalls.asStateFlow()

    fun setAllowIncomingCalls(allow: Boolean) {
        _allowIncomingCalls.value = allow
        repository.setAllowIncomingCalls(allow)
    }

    private val _customNickname = MutableStateFlow(repository.getMeshDisplayName())
    val customNickname: StateFlow<String> = _customNickname.asStateFlow()

    fun setCustomNickname(name: String) {
        _customNickname.value = name
        repository.setMeshDisplayName(name)
    }

    // Language Preference (PRD v2.1 Section 4)
    private val _languagePreference = MutableStateFlow(LocaleHelper.getPersistedLanguage(app))
    val languagePreference: StateFlow<String> = _languagePreference.asStateFlow()

    fun setLanguagePreference(lang: String) {
        _languagePreference.value = lang
        LocaleHelper.setPersistedLanguage(app, lang)
    }

    // Database Flows
    val conversations: StateFlow<List<ConversationEntity>> = repository.conversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trustedDevices: StateFlow<List<DeviceEntity>> = repository.trustedDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockedDevices: StateFlow<List<DeviceEntity>> = repository.blockedDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDevices: StateFlow<List<DeviceEntity>> = repository.allDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvedContacts: StateFlow<List<ApprovedContactEntity>> = repository.approvedContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingMessages: StateFlow<List<MessageEntity>> = repository.pendingMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingRelayPackets: StateFlow<List<RelayQueueEntity>> = repository.pendingRelayPackets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingQueueCount: StateFlow<Int> = repository.pendingQueueCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

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

    private val _isForegroundServiceRunning = MutableStateFlow(BluetoothMeshService.isRunning)
    val isForegroundServiceRunning: StateFlow<Boolean> = _isForegroundServiceRunning.asStateFlow()

    private val prefs = app.getSharedPreferences("ronova_preferences", Application.MODE_PRIVATE)

    private val _callAlertsEnabled = MutableStateFlow(prefs.getBoolean("pref_call_alerts", true))
    val callAlertsEnabled: StateFlow<Boolean> = _callAlertsEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(prefs.getBoolean("pref_vibration", true))
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _messageSoundEnabled = MutableStateFlow(prefs.getBoolean("pref_message_sound", true))
    val messageSoundEnabled: StateFlow<Boolean> = _messageSoundEnabled.asStateFlow()

    fun setCallAlertsEnabled(enabled: Boolean) {
        _callAlertsEnabled.value = enabled
        prefs.edit().putBoolean("pref_call_alerts", enabled).apply()
    }

    fun setVibrationEnabled(enabled: Boolean) {
        _vibrationEnabled.value = enabled
        prefs.edit().putBoolean("pref_vibration", enabled).apply()
    }

    fun setMessageSoundEnabled(enabled: Boolean) {
        _messageSoundEnabled.value = enabled
        prefs.edit().putBoolean("pref_message_sound", enabled).apply()
    }

    val setupRepository = app.setupRepository
    val setupPreferences = setupRepository.setupPreferencesFlow
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            com.example.data.SetupPreferences(
                com.example.data.SetupState.NOT_STARTED,
                1,
                com.example.data.SetupRepository.CURRENT_SCHEMA_VERSION,
                0L
            )
        )

    fun saveSetupStep(step: Int) {
        viewModelScope.launch {
            setupRepository.saveStepProgress(step)
        }
    }

    fun completeSetup() {
        viewModelScope.launch {
            setupRepository.markCompleted()
        }
    }

    fun resetSetupWizard() {
        viewModelScope.launch {
            setupRepository.resetSetup()
            repository.setSetupCompleted(false)
        }
    }

    fun isBluetoothEnabled(): Boolean {
        return com.example.core.permission.PermissionManager.isBluetoothEnabled(app)
    }

    fun hasBluetoothPermissions(): Boolean {
        return com.example.core.permission.PermissionManager.hasBluetoothPermissions(app)
    }

    suspend fun getStartDestination(): com.example.data.StartDestination {
        return setupRepository.computeStartDestination()
    }

    init {
        // Collect incoming Bluetooth packets
        viewModelScope.launch {
            meshManager.incomingPackets.collect { packet ->
                handleIncomingPacket(packet, isInternet = false)
            }
        }

        // Collect incoming Internet packets (PRD v2.1 Section 2.3)
        viewModelScope.launch {
            internetTransportManager.incomingInternetPackets.collect { packet ->
                handleIncomingPacket(packet, isInternet = true)
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

        // Firebase Auth and Realtime State Lifecycle
        var userScopeJob: Job? = null
        viewModelScope.launch {
            currentUser.collect { user ->
                userScopeJob?.cancel()
                if (user != null) {
                    userScopeJob = launch {
                        try {
                            val profile = firestoreUserRepository.createOrInitProfile(user)
                            _myProfile.value = profile
                        } catch (e: Exception) {
                            android.util.Log.w("RonovaVM", "Failed to init profile: ${e.message}")
                        }

                        launch {
                            try {
                                firestoreUserRepository.observeProfile(user.uid).collect { updated ->
                                    if (updated != null) _myProfile.value = updated
                                }
                            } catch (e: Exception) {
                                android.util.Log.w("RonovaVM", "Profile listener error", e)
                            }
                        }

                        launch {
                            try {
                                firestoreFriendRepository.observeReceivedRequests(user.uid).collect { reqs ->
                                    _receivedRequests.value = reqs
                                }
                            } catch (e: Exception) {
                                android.util.Log.w("RonovaVM", "Received requests error", e)
                            }
                        }

                        launch {
                            try {
                                firestoreFriendRepository.observeSentRequests(user.uid).collect { reqs ->
                                    _sentRequests.value = reqs
                                }
                            } catch (e: Exception) {
                                android.util.Log.w("RonovaVM", "Sent requests error", e)
                            }
                        }

                        @OptIn(ExperimentalCoroutinesApi::class)
                        launch {
                            try {
                                firestoreFriendRepository.observeFriendships(user.uid)
                                    .flatMapLatest { friendships ->
                                        val friendUids = friendships.map {
                                            if (it.userAUid == user.uid) it.userBUid else it.userAUid
                                        }
                                        if (friendUids.isNotEmpty()) {
                                            firestoreUserRepository.observeUsers(friendUids)
                                        } else {
                                            flowOf(emptyList())
                                        }
                                    }
                                    .collect { friendProfiles ->
                                        _friends.value = friendProfiles
                                    }
                            } catch (e: Exception) {
                                android.util.Log.w("RonovaVM", "Friendships error", e)
                            }
                        }

                        @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
                        launch {
                            try {
                                combine(searchQuery, _directoryRefreshTrigger) { q: String, _: Int -> q }
                                    .debounce(200L)
                                    .flatMapLatest { query ->
                                        _directoryUiState.value = DirectoryUiState.Loading
                                        firestoreUserRepository.searchPublicUsers(query, user.uid)
                                            .map { users -> Result.success(query to users) }
                                            .catch { e ->
                                                android.util.Log.w("RonovaVM", "Directory search error", e)
                                                emit(Result.failure(e))
                                            }
                                    }
                                    .collect { result ->
                                        result.fold(
                                            onSuccess = { (query, users) ->
                                                _directoryUsers.value = users
                                                val isSearch = query.trim().isNotEmpty()
                                                if (users.isEmpty()) {
                                                    _directoryUiState.value = DirectoryUiState.Empty(isSearch)
                                                } else {
                                                    _directoryUiState.value = DirectoryUiState.Success(users, isSearch)
                                                }
                                            },
                                            onFailure = { e ->
                                                _directoryUiState.value = DirectoryUiState.Error(
                                                    e.localizedMessage ?: "Failed to connect to member directory. Please check network."
                                                )
                                            }
                                        )
                                    }
                            } catch (e: Exception) {
                                android.util.Log.w("RonovaVM", "Directory search fatal error", e)
                                _directoryUiState.value = DirectoryUiState.Error(e.localizedMessage ?: "Failed to load directory")
                            }
                        }
                    }
                } else {
                    _myProfile.value = null
                    _friends.value = emptyList()
                    _receivedRequests.value = emptyList()
                    _sentRequests.value = emptyList()
                    _directoryUsers.value = emptyList()
                    _directoryUiState.value = DirectoryUiState.Loading
                }
            }
        }
    }

    private suspend fun handleIncomingPacket(packet: MeshPacket, isInternet: Boolean = false) {
        when (packet.type) {
            MeshPacketType.TEXT_MESSAGE -> {
                // 1. Check if device is blocked
                val senderDevice = repository.getDeviceById(packet.originDeviceId)
                if (senderDevice?.trustStatus == "BLOCKED") {
                    repository.logSecurityEvent(
                        "BLOCKED_MESSAGE_DROPPED",
                        "Dropped message from blocked device ${packet.originDeviceId}",
                        packet.originDeviceId
                    )
                    return
                }

                // 2. Profile Lock verification (PRD v2.1 Section 6.2)
                if (_isProfileLocked.value) {
                    val isApproved = repository.isContactApproved(packet.originDeviceId)
                    if (!isApproved) {
                        repository.logSecurityEvent(
                            "UNAUTHORIZED_MESSAGE_REJECTED",
                            "Rejected message from unapproved contact ${packet.originDeviceId} due to Profile Lock",
                            packet.originDeviceId
                        )
                        return
                    }
                }

                var decryptedText = packet.payload
                try {
                    if (packet.senderPublicKeyBase64.isNotEmpty()) {
                        val senderKey = cryptoManager.decodePublicKey(packet.senderPublicKeyBase64)
                        decryptedText = cryptoManager.decryptFromPeer(packet.payload, senderKey)
                    }
                } catch (e: Exception) {
                    // Raw payload fallback
                }

                val transport = if (isInternet) "INTERNET" else "BLUETOOTH"

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
                    encryptedPayload = packet.payload,
                    transportType = transport
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
                        put("transport", transport)
                    }.toString()
                )
                if (isInternet) {
                    internetTransportManager.dispatchInternetPacket(ack)
                } else {
                    meshManager.sendPacket(ack)
                }
            }
            MeshPacketType.ACK_DELIVERED -> {
                try {
                    val obj = JSONObject(packet.payload)
                    val ackMessageId = obj.optString("ackMessageId", "")
                    val hops = obj.optInt("hops", packet.currentHop)
                    val transport = obj.optString("transport", if (isInternet) "INTERNET" else "BLUETOOTH")
                    if (ackMessageId.isNotEmpty()) {
                        repository.updateMessageDeliveryState(
                            messageId = ackMessageId,
                            state = "DELIVERED",
                            hopCount = hops,
                            routeJson = packet.toJsonObject().optJSONArray("rp")?.toString() ?: "[]"
                        )
                    }
                } catch (e: Exception) {
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

    /**
     * Sends message with automatic route selection (PRD v2.1 Section 2.4):
     * 1. Direct Bluetooth if in direct range.
     * 2. Bluetooth Mesh Relay if route exists.
     * 3. Internet Route if destination is remote & network is connected.
     * 4. Offline Queue if no route available.
     */
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
                } catch (_: Exception) {}
            }

            // Determine route based on Section 2.4
            val directPeer = connectedPeers.value.any { it.deviceId == recipientDeviceId && it.isConnected }
            val route = internetTransportManager.selectRoute(
                isDirectBluetoothConnected = directPeer,
                isBluetoothMeshReachable = directPeer, // or known hop route
                preference = _routePreference.value
            )

            val transportTypeStr = if (route == TransportRoute.INTERNET_REMOTE) "INTERNET" else "BLUETOOTH"

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
                encryptedPayload = payload,
                transportType = transportTypeStr
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

            when (route) {
                TransportRoute.DIRECT_BLUETOOTH, TransportRoute.MESH_RELAY -> {
                    val sent = meshManager.sendPacket(packet)
                    if (sent) {
                        repository.updateMessageDeliveryState(messageId, "RELAYED", 1)
                    } else {
                        // Enqueue in relay queue
                        enqueueToStoreAndForward(messageId, recipientDeviceId, packet)
                    }
                }
                TransportRoute.INTERNET_REMOTE -> {
                    val sent = internetTransportManager.dispatchInternetPacket(packet)
                    if (sent) {
                        repository.updateMessageDeliveryState(messageId, "SENT", 0)
                    } else {
                        enqueueToStoreAndForward(messageId, recipientDeviceId, packet)
                    }
                }
                TransportRoute.OFFLINE_QUEUE -> {
                    enqueueToStoreAndForward(messageId, recipientDeviceId, packet)
                }
            }
        }
    }

    private suspend fun enqueueToStoreAndForward(messageId: String, recipientDeviceId: String, packet: MeshPacket) {
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

    fun approveContact(contactId: String, nickname: String, code: String) {
        viewModelScope.launch {
            repository.saveApprovedContact(contactId, nickname, code)
        }
    }

    fun revokeApprovedContact(contactId: String) {
        viewModelScope.launch {
            repository.revokeApprovedContact(contactId)
        }
    }

    fun clearAllApprovedContacts() {
        viewModelScope.launch {
            repository.clearAllApprovedContacts()
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
            val sent = if (message.transportType == "INTERNET") {
                internetTransportManager.dispatchInternetPacket(packet)
            } else {
                meshManager.sendPacket(packet)
            }
            if (sent) {
                repository.updateMessageDeliveryState(message.messageId, "RELAYED", 1)
            }
        }
    }

    fun resendPendingPackets() {
        viewModelScope.launch(Dispatchers.IO) {
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

    // Firebase Auth & Cloud Actions (PRD v2.2)
    fun signInWithGoogle(
        activity: Activity,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit = {}
    ) {
        firebaseAuthManager.signInWithGoogle(activity, viewModelScope, onSuccess, onError, onCancelled)
    }

    fun attemptAutoSignIn(
        context: Context,
        onSuccess: (FirebaseUser) -> Unit,
        onUnauthenticated: () -> Unit
    ) {
        firebaseAuthManager.attemptAutoSignIn(context, viewModelScope, onSuccess, onUnauthenticated)
    }

    fun signOut(context: Context, onComplete: () -> Unit = {}) {
        val uid = currentUser.value?.uid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    firestoreUserRepository.setPresence(uid, false)
                } catch (_: Exception) {}
            }
        }
        _myProfile.value = null
        _friends.value = emptyList()
        _receivedRequests.value = emptyList()
        _sentRequests.value = emptyList()
        _directoryUsers.value = emptyList()
        firebaseAuthManager.signOut(context, viewModelScope, onComplete)
    }

    suspend fun getUserProfile(uid: String): UserProfile? {
        return friends.value.find { it.uid == uid }
            ?: directoryUsers.value.find { it.uid == uid }
            ?: firestoreUserRepository.getProfileOnce(uid)
    }

    fun updateUserProfile(
        displayName: String,
        bio: String,
        profileVisibility: String,
        presencePrivacy: String,
        photoUrl: String? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val uid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            try {
                firestoreUserRepository.updateProfile(uid, displayName, bio, profileVisibility, presencePrivacy, photoUrl)
                _myProfile.value = _myProfile.value?.copy(
                    displayName = displayName,
                    bio = bio,
                    profileVisibility = profileVisibility,
                    presencePrivacy = presencePrivacy,
                    photoUrl = photoUrl ?: _myProfile.value?.photoUrl ?: ""
                )
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to update profile")
            }
        }
    }

    fun isProfileInitCompleted(): Boolean {
        val uid = currentUser.value?.uid ?: return false
        return prefs.getBoolean("pref_profile_init_completed_$uid", false)
    }

    fun markProfileInitCompleted() {
        val uid = currentUser.value?.uid ?: return
        prefs.edit().putBoolean("pref_profile_init_completed_$uid", true).apply()
        completeSetup()
    }

    fun setDirectorySearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearDirectorySearchQuery() {
        _searchQuery.value = ""
    }

    fun retryDirectorySearch() {
        _directoryUiState.value = DirectoryUiState.Loading
        _directoryRefreshTrigger.value = _directoryRefreshTrigger.value + 1
    }

    fun sendFriendRequest(target: UserProfile, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val sender = myProfile.value
        if (sender == null) {
            onError("You must be logged in to send friend requests")
            return
        }
        viewModelScope.launch {
            try {
                firestoreFriendRepository.sendFriendRequest(sender, target)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to send request")
            }
        }
    }

    fun acceptFriendRequest(request: FriendRequest, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                firestoreFriendRepository.acceptFriendRequest(request)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to accept request")
            }
        }
    }

    fun rejectFriendRequest(requestId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                firestoreFriendRepository.rejectFriendRequest(requestId)
            } catch (_: Exception) {}
            onComplete()
        }
    }

    fun cancelFriendRequest(requestId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                firestoreFriendRepository.cancelFriendRequest(requestId)
            } catch (_: Exception) {}
            onComplete()
        }
    }

    fun removeFriend(targetUid: String, onComplete: () -> Unit = {}) {
        val myUid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            try {
                firestoreFriendRepository.removeFriendship(myUid, targetUid)
            } catch (_: Exception) {}
            onComplete()
        }
    }

    fun blockUser(targetUid: String, onComplete: () -> Unit = {}) {
        val myUid = currentUser.value?.uid ?: return
        viewModelScope.launch {
            try {
                firestoreFriendRepository.removeFriendship(myUid, targetUid)
                repository.updateDeviceTrust(targetUid, "BLOCKED")
            } catch (_: Exception) {}
            onComplete()
        }
    }

    fun sendHybridMessage(
        peerDeviceIdOrUid: String,
        peerDisplayName: String,
        text: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val sender = myProfile.value
        val isFriend = friends.value.any { it.uid == peerDeviceIdOrUid }
        val targetProfile = friends.value.find { it.uid == peerDeviceIdOrUid }

        if (sender != null && isFriend && targetProfile != null && isInternetAvailable.value) {
            viewModelScope.launch {
                try {
                    firestoreChatRepository.sendMessage(sender, targetProfile, text)
                    onComplete(true)
                } catch (e: Exception) {
                    // Fallback to local Bluetooth / mesh queue
                    sendMessage(peerDeviceIdOrUid, text)
                    onComplete(false)
                }
            }
        } else {
            // Local Bluetooth Mesh or direct Bluetooth
            sendMessage(peerDeviceIdOrUid, text)
            onComplete(true)
        }
    }
}

