package com.example.core.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val uid: String = "",
    val ronovaId: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val bio: String = "",
    val profileVisibility: String = "public", // "public", "friends_only", "private"
    val presencePrivacy: String = "public",   // "public", "friends_only", "nobody"
    val isOnline: Boolean = true,
    @ServerTimestamp val presenceUpdatedAt: Timestamp? = null,
    val accountStatus: String = "active",    // "active", "suspended"
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FriendRequest(
    @DocumentId val id: String = "",
    val senderUid: String = "",
    val senderRonovaId: String = "",
    val senderDisplayName: String = "",
    val senderPhotoUrl: String = "",
    val recipientUid: String = "",
    val recipientRonovaId: String = "",
    val recipientDisplayName: String = "",
    val status: String = "pending", // "pending", "accepted", "rejected", "cancelled"
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Friendship(
    @DocumentId val id: String = "",
    val userAUid: String = "",
    val userBUid: String = "",
    val userARonovaId: String = "",
    val userBRonovaId: String = "",
    val userADisplayName: String = "",
    val userBDisplayName: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CloudConversation(
    @DocumentId val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessageText: String = "",
    @ServerTimestamp val lastMessageTimestamp: Timestamp? = null,
    val lastSenderUid: String = ""
)

@IgnoreExtraProperties
data class CloudMessage(
    @DocumentId val id: String = "",
    val conversationId: String = "",
    val senderUid: String = "",
    val recipientUid: String = "",
    val text: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    val deliveryState: String = "sent", // "sent", "delivered", "read"
    val transportType: String = "internet" // "internet", "bluetooth_direct", "bluetooth_mesh"
)
