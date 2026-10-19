package com.example.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val deviceId: String,
    val publicKeyBase64: String,
    val displayName: String,
    val trustStatus: String = "UNTRUSTED", // "TRUSTED", "UNTRUSTED", "BLOCKED"
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val bluetoothAddress: String = "",
    val isDirectPeer: Boolean = false,
    val hopDistance: Int = 1
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val conversationId: String, // peer deviceId
    val contactName: String,
    val lastMessageText: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isPinned: Boolean = false
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val messageId: String,
    val conversationId: String,
    val senderDeviceId: String,
    val recipientDeviceId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val deliveryState: String = "QUEUED", // "DRAFT", "QUEUED", "SENDING", "RELAYED", "DELIVERED", "FAILED", "EXPIRED"
    val hopCount: Int = 0,
    val routeNodesJson: String = "[]",
    val isOutgoing: Boolean = true,
    val encryptedPayload: String = ""
)

@Entity(tableName = "relay_queue")
data class RelayQueueEntity(
    @PrimaryKey val packetId: String,
    val destinationDeviceId: String,
    val originDeviceId: String,
    val rawPacketJson: String,
    val hopCount: Int,
    val maxHops: Int = 5,
    val timestamp: Long = System.currentTimeMillis(),
    val expiryTimestamp: Long = System.currentTimeMillis() + 86400000L,
    val retryCount: Int = 0,
    val lastRetryTimestamp: Long = System.currentTimeMillis(),
    val status: String = "PENDING" // "PENDING", "FORWARDED", "EXPIRED", "FAILED"
)

@Entity(tableName = "peers")
data class PeerEntity(
    @PrimaryKey val bluetoothAddress: String,
    val deviceId: String,
    val deviceName: String,
    val rssi: Int = 0,
    val isConnected: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis()
)

@Entity(tableName = "call_history")
data class CallHistoryEntity(
    @PrimaryKey val callId: String,
    val peerDeviceId: String,
    val peerName: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0,
    val callType: String = "OUTGOING", // "OUTGOING", "INCOMING", "MISSED"
    val callResult: String = "COMPLETED" // "COMPLETED", "REJECTED", "BUSY", "FAILED", "CANCELLED"
)

@Entity(tableName = "security_events")
data class SecurityEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceId: String = ""
)
