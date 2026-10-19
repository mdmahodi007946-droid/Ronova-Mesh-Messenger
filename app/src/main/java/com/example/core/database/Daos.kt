package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY lastSeenTimestamp DESC")
    fun getAllDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE trustStatus = 'TRUSTED' ORDER BY displayName ASC")
    fun getTrustedDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE trustStatus = 'BLOCKED' ORDER BY displayName ASC")
    fun getBlockedDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getDeviceById(deviceId: String): DeviceEntity?

    @Query("SELECT * FROM devices WHERE deviceId = :deviceId LIMIT 1")
    fun getDeviceByIdFlow(deviceId: String): Flow<DeviceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(device: DeviceEntity)

    @Query("UPDATE devices SET trustStatus = :status WHERE deviceId = :deviceId")
    suspend fun updateTrustStatus(deviceId: String, status: String)

    @Query("UPDATE devices SET displayName = :name WHERE deviceId = :deviceId")
    suspend fun updateDisplayName(deviceId: String, name: String)

    @Query("DELETE FROM devices WHERE deviceId = :deviceId")
    suspend fun deleteDevice(deviceId: String)
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, lastMessageTimestamp DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE conversationId = :id LIMIT 1")
    suspend fun getConversation(id: String): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE conversationId = :id LIMIT 1")
    fun getConversationFlow(id: String): Flow<ConversationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(conversation: ConversationEntity)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE conversationId = :id")
    suspend fun clearUnread(id: String)

    @Query("DELETE FROM conversations WHERE conversationId = :id")
    suspend fun deleteConversation(id: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE messageId = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE messageId = :messageId LIMIT 1")
    fun getMessageByIdFlow(messageId: String): Flow<MessageEntity?>

    @Query("SELECT * FROM messages WHERE deliveryState IN ('QUEUED', 'SENDING') ORDER BY timestamp ASC")
    fun getPendingMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE deliveryState IN ('QUEUED', 'SENDING') ORDER BY timestamp ASC")
    suspend fun getPendingMessagesList(): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("UPDATE messages SET deliveryState = :state, hopCount = :hopCount, routeNodesJson = :routeJson WHERE messageId = :messageId")
    suspend fun updateDeliveryState(messageId: String, state: String, hopCount: Int, routeJson: String)

    @Query("DELETE FROM messages WHERE messageId = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun clearConversationMessages(conversationId: String)
}

@Dao
interface RelayQueueDao {
    @Query("SELECT * FROM relay_queue WHERE status = 'PENDING' AND expiryTimestamp > :currentTime ORDER BY timestamp ASC")
    fun getPendingRelayPackets(currentTime: Long = System.currentTimeMillis()): Flow<List<RelayQueueEntity>>

    @Query("SELECT * FROM relay_queue WHERE status = 'PENDING' AND expiryTimestamp > :currentTime ORDER BY timestamp ASC")
    suspend fun getPendingRelayPacketsList(currentTime: Long = System.currentTimeMillis()): List<RelayQueueEntity>

    @Query("SELECT COUNT(*) FROM relay_queue WHERE status = 'PENDING'")
    fun getPendingQueueCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(packet: RelayQueueEntity)

    @Update
    suspend fun update(packet: RelayQueueEntity)

    @Query("UPDATE relay_queue SET status = 'FORWARDED' WHERE packetId = :packetId")
    suspend fun markForwarded(packetId: String)

    @Query("DELETE FROM relay_queue WHERE expiryTimestamp <= :currentTime OR status != 'PENDING'")
    suspend fun pruneExpired(currentTime: Long = System.currentTimeMillis())

    @Query("DELETE FROM relay_queue")
    suspend fun clearQueue()
}

@Dao
interface PeerDao {
    @Query("SELECT * FROM peers ORDER BY isConnected DESC, lastSeen DESC")
    fun getAllPeers(): Flow<List<PeerEntity>>

    @Query("SELECT * FROM peers WHERE isConnected = 1")
    fun getConnectedPeers(): Flow<List<PeerEntity>>

    @Query("SELECT * FROM peers WHERE isConnected = 1")
    suspend fun getConnectedPeersList(): List<PeerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(peer: PeerEntity)

    @Query("UPDATE peers SET isConnected = :connected WHERE bluetoothAddress = :address")
    suspend fun setConnected(address: String, connected: Boolean)

    @Query("DELETE FROM peers WHERE bluetoothAddress = :address")
    suspend fun removePeer(address: String)

    @Query("UPDATE peers SET isConnected = 0")
    suspend fun disconnectAll()
}

@Dao
interface CallHistoryDao {
    @Query("SELECT * FROM call_history ORDER BY startTime DESC")
    fun getAllCalls(): Flow<List<CallHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallHistoryEntity)

    @Query("DELETE FROM call_history WHERE callId = :callId")
    suspend fun deleteCall(callId: String)

    @Query("DELETE FROM call_history")
    suspend fun clearAll()
}

@Dao
interface SecurityEventDao {
    @Query("SELECT * FROM security_events ORDER BY timestamp DESC LIMIT 100")
    fun getRecentEvents(): Flow<List<SecurityEventEntity>>

    @Insert
    suspend fun logEvent(event: SecurityEventEntity)

    @Query("DELETE FROM security_events")
    suspend fun clearEvents()
}
