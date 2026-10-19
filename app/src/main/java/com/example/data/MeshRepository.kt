package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.core.database.AppDatabase
import com.example.core.database.CallHistoryEntity
import com.example.core.database.ConversationEntity
import com.example.core.database.DeviceEntity
import com.example.core.database.MessageEntity
import com.example.core.database.PeerEntity
import com.example.core.database.RelayQueueEntity
import com.example.core.database.SecurityEventEntity
import kotlinx.coroutines.flow.Flow
import java.io.File

enum class RetentionPolicy(val code: String, val title: String) {
    AUTO_DELETE("auto_delete", "Auto-Delete (> 30 Days)"),
    AUTO_ARCHIVE("auto_archive", "Auto-Archive (> 30 Days)"),
    KEEP_ALL("keep_all", "Keep All (No Auto-Cleanup)");

    companion object {
        fun fromCode(code: String): RetentionPolicy {
            return entries.firstOrNull { it.code == code } ?: AUTO_DELETE
        }
    }
}

class MeshRepository(
    private val database: AppDatabase,
    private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ronova_storage_prefs", Context.MODE_PRIVATE)

    companion object {
        const val THIRTY_DAYS_MS = 30L * 24L * 60L * 60L * 1000L
        private const val KEY_RETENTION_POLICY = "key_retention_policy"
    }

    val conversations: Flow<List<ConversationEntity>> = database.conversationDao().getAllConversations()
    val allDevices: Flow<List<DeviceEntity>> = database.deviceDao().getAllDevices()
    val trustedDevices: Flow<List<DeviceEntity>> = database.deviceDao().getTrustedDevices()
    val blockedDevices: Flow<List<DeviceEntity>> = database.deviceDao().getBlockedDevices()
    val pendingMessages: Flow<List<MessageEntity>> = database.messageDao().getPendingMessages()
    val pendingRelayPackets: Flow<List<RelayQueueEntity>> = database.relayQueueDao().getPendingRelayPackets()
    val pendingQueueCount: Flow<Int> = database.relayQueueDao().getPendingQueueCount()
    val connectedPeers: Flow<List<PeerEntity>> = database.peerDao().getAllPeers()
    val callHistory: Flow<List<CallHistoryEntity>> = database.callHistoryDao().getAllCalls()
    val securityEvents: Flow<List<SecurityEventEntity>> = database.securityEventDao().getRecentEvents()
    val archivedMessages: Flow<List<MessageEntity>> = database.messageDao().getAllArchivedMessages()
    val archivedMessagesCount: Flow<Int> = database.messageDao().getArchivedMessagesCount()

    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>> {
        return database.messageDao().getMessagesForConversation(conversationId)
    }

    fun getMessageByIdFlow(messageId: String): Flow<MessageEntity?> {
        return database.messageDao().getMessageByIdFlow(messageId)
    }

    suspend fun getMessageById(messageId: String): MessageEntity? {
        return database.messageDao().getMessageById(messageId)
    }

    fun getDeviceByIdFlow(deviceId: String): Flow<DeviceEntity?> {
        return database.deviceDao().getDeviceByIdFlow(deviceId)
    }

    suspend fun getDeviceById(deviceId: String): DeviceEntity? {
        return database.deviceDao().getDeviceById(deviceId)
    }

    suspend fun saveDevice(device: DeviceEntity) {
        database.deviceDao().insertOrUpdate(device)
    }

    suspend fun updateDeviceTrust(deviceId: String, status: String) {
        database.deviceDao().updateTrustStatus(deviceId, status)
        logSecurityEvent("TRUST_STATUS_CHANGE", "Device $deviceId set to $status", deviceId)
    }

    suspend fun updateDeviceName(deviceId: String, name: String) {
        database.deviceDao().updateDisplayName(deviceId, name)
        // Also update conversation contact name if exists
        val conv = database.conversationDao().getConversation(deviceId)
        if (conv != null) {
            database.conversationDao().insertOrUpdate(conv.copy(contactName = name))
        }
    }

    suspend fun deleteDevice(deviceId: String) {
        database.deviceDao().deleteDevice(deviceId)
    }

    suspend fun saveConversation(conversation: ConversationEntity) {
        database.conversationDao().insertOrUpdate(conversation)
    }

    suspend fun clearUnread(conversationId: String) {
        database.conversationDao().clearUnread(conversationId)
    }

    suspend fun deleteConversation(conversationId: String) {
        database.conversationDao().deleteConversation(conversationId)
        database.messageDao().clearConversationMessages(conversationId)
    }

    suspend fun saveMessage(message: MessageEntity) {
        database.messageDao().insertMessage(message)
        // Update conversation preview
        val contactName = database.deviceDao().getDeviceById(message.conversationId)?.displayName
            ?: message.conversationId
        val conv = database.conversationDao().getConversation(message.conversationId)
            ?: ConversationEntity(
                conversationId = message.conversationId,
                contactName = contactName,
                lastMessageText = message.content,
                lastMessageTimestamp = message.timestamp,
                unreadCount = if (!message.isOutgoing) 1 else 0
            )

        val updatedConv = conv.copy(
            lastMessageText = message.content,
            lastMessageTimestamp = message.timestamp,
            unreadCount = if (!message.isOutgoing) conv.unreadCount + 1 else conv.unreadCount
        )
        database.conversationDao().insertOrUpdate(updatedConv)
    }

    suspend fun updateMessageDeliveryState(
        messageId: String,
        state: String,
        hopCount: Int = 0,
        routeJson: String = "[]"
    ) {
        database.messageDao().updateDeliveryState(messageId, state, hopCount, routeJson)
    }

    suspend fun deleteMessage(messageId: String) {
        database.messageDao().deleteMessage(messageId)
    }

    suspend fun enqueueRelayPacket(packet: RelayQueueEntity) {
        database.relayQueueDao().enqueue(packet)
    }

    suspend fun markRelayPacketForwarded(packetId: String) {
        database.relayQueueDao().markForwarded(packetId)
    }

    suspend fun pruneRelayQueue() {
        database.relayQueueDao().pruneExpired(System.currentTimeMillis())
    }

    suspend fun clearRelayQueue() {
        database.relayQueueDao().clearQueue()
    }

    suspend fun savePeer(peer: PeerEntity) {
        database.peerDao().insertOrUpdate(peer)
    }

    suspend fun setPeerConnected(address: String, connected: Boolean) {
        database.peerDao().setConnected(address, connected)
    }

    suspend fun disconnectAllPeers() {
        database.peerDao().disconnectAll()
    }

    suspend fun saveCall(call: CallHistoryEntity) {
        database.callHistoryDao().insertCall(call)
    }

    suspend fun deleteCall(callId: String) {
        database.callHistoryDao().deleteCall(callId)
    }

    suspend fun clearCallHistory() {
        database.callHistoryDao().clearAll()
    }

    suspend fun logSecurityEvent(type: String, description: String, deviceId: String = "") {
        database.securityEventDao().logEvent(
            SecurityEventEntity(
                eventType = type,
                description = description,
                timestamp = System.currentTimeMillis(),
                deviceId = deviceId
            )
        )
    }

    suspend fun clearSecurityEvents() {
        database.securityEventDao().clearEvents()
    }

    fun getDatabaseSizeBytes(): Long {
        val dbFile = context.getDatabasePath("ronova_mesh.db")
        return if (dbFile.exists()) dbFile.length() else 0L
    }

    fun getRetentionPolicy(): RetentionPolicy {
        val code = prefs.getString(KEY_RETENTION_POLICY, RetentionPolicy.AUTO_DELETE.code)
            ?: RetentionPolicy.AUTO_DELETE.code
        return RetentionPolicy.fromCode(code)
    }

    fun setRetentionPolicy(policy: RetentionPolicy) {
        prefs.edit().putString(KEY_RETENTION_POLICY, policy.code).apply()
    }

    suspend fun getMessagesOlderThan30DaysCount(): Int {
        val cutoff = System.currentTimeMillis() - THIRTY_DAYS_MS
        return database.messageDao().getEligibleForRetentionCount(cutoff)
    }

    suspend fun deleteMessagesOlderThan30Days(): Int {
        val cutoff = System.currentTimeMillis() - THIRTY_DAYS_MS
        val deletedCount = database.messageDao().deleteMessagesOlderThan(cutoff)
        if (deletedCount > 0) {
            logSecurityEvent("STORAGE_AUTO_CLEAN", "Auto-deleted $deletedCount messages older than 30 days")
        }
        return deletedCount
    }

    suspend fun archiveMessagesOlderThan30Days(): Int {
        val cutoff = System.currentTimeMillis() - THIRTY_DAYS_MS
        val archivedCount = database.messageDao().archiveMessagesOlderThan(cutoff)
        if (archivedCount > 0) {
            logSecurityEvent("STORAGE_AUTO_ARCHIVE", "Archived $archivedCount messages older than 30 days")
        }
        return archivedCount
    }

    suspend fun runRetentionPolicyCleanup(): Int {
        return when (getRetentionPolicy()) {
            RetentionPolicy.AUTO_DELETE -> deleteMessagesOlderThan30Days()
            RetentionPolicy.AUTO_ARCHIVE -> archiveMessagesOlderThan30Days()
            RetentionPolicy.KEEP_ALL -> 0
        }
    }

    suspend fun unarchiveMessage(messageId: String) {
        database.messageDao().unarchiveMessage(messageId)
    }

    suspend fun unarchiveAllMessages(): Int {
        return database.messageDao().unarchiveAllMessages()
    }

    suspend fun deleteAllArchivedMessages(): Int {
        return database.messageDao().deleteAllArchivedMessages()
    }
}
