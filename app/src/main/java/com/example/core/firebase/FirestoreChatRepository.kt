package com.example.core.firebase

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.database.AppDatabase
import com.example.core.database.ConversationEntity
import com.example.core.database.MessageEntity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreChatRepository(
    private val db: FirebaseFirestore,
    private val appDatabase: AppDatabase
) {
    companion object {
        private const val TAG = "FirestoreChatRepo"

        fun create(context: Context, appDatabase: AppDatabase): FirestoreChatRepository {
            val databaseId = context.getString(R.string.firestore_database_id)
            val firestore = FirebaseFirestore.getInstance(databaseId)
            return FirestoreChatRepository(firestore, appDatabase)
        }

        fun generateConversationId(uid1: String, uid2: String): String {
            return if (uid1 < uid2) "${uid1}_$uid2" else "${uid2}_$uid1"
        }
    }

    private val conversationsCollection = db.collection("conversations")
    private val scope = CoroutineScope(Dispatchers.IO)

    fun observeMessages(conversationId: String): Flow<List<CloudMessage>> {
        return conversationsCollection.document(conversationId)
            .collection("messages")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(CloudMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.createdAt }
            }
    }

    suspend fun sendMessage(
        senderProfile: UserProfile,
        recipientProfile: UserProfile,
        text: String
    ): CloudMessage {
        val convId = generateConversationId(senderProfile.uid, recipientProfile.uid)
        val messageId = UUID.randomUUID().toString()

        val messageData = mapOf(
            "id" to messageId,
            "conversationId" to convId,
            "senderUid" to senderProfile.uid,
            "recipientUid" to recipientProfile.uid,
            "text" to text,
            "createdAt" to FieldValue.serverTimestamp(),
            "deliveryState" to "sent",
            "transportType" to "internet"
        )

        // 1. Write message in Firestore subcollection
        conversationsCollection.document(convId)
            .collection("messages").document(messageId)
            .set(messageData).await()

        // 2. Update parent conversation metadata with deterministically sorted participants
        val sortedParticipants = listOf(senderProfile.uid, recipientProfile.uid).sorted()
        val convData = mapOf(
            "id" to convId,
            "participants" to sortedParticipants,
            "lastMessageText" to text,
            "lastMessageTimestamp" to FieldValue.serverTimestamp(),
            "lastSenderUid" to senderProfile.uid
        )
        conversationsCollection.document(convId).set(convData).await()

        // 3. Cache in Room database
        val localMessage = MessageEntity(
            messageId = messageId,
            conversationId = recipientProfile.uid,
            senderDeviceId = senderProfile.uid,
            recipientDeviceId = recipientProfile.uid,
            content = text,
            timestamp = System.currentTimeMillis(),
            deliveryState = "DELIVERED",
            hopCount = 0,
            routeNodesJson = "[]",
            isOutgoing = true,
            transportType = "INTERNET"
        )
        val localConv = ConversationEntity(
            conversationId = recipientProfile.uid,
            contactName = recipientProfile.displayName,
            lastMessageText = text,
            lastMessageTimestamp = System.currentTimeMillis(),
            unreadCount = 0,
            lastTransportType = "INTERNET"
        )
        appDatabase.messageDao().insert(localMessage)
        appDatabase.conversationDao().insertOrUpdate(localConv)

        return CloudMessage(
            id = messageId,
            conversationId = convId,
            senderUid = senderProfile.uid,
            recipientUid = recipientProfile.uid,
            text = text,
            deliveryState = "sent",
            transportType = "internet"
        )
    }

    suspend fun markMessagesAsRead(conversationId: String, currentUid: String) {
        try {
            val query = conversationsCollection.document(conversationId)
                .collection("messages")
                .whereEqualTo("recipientUid", currentUid)
                .whereNotEqualTo("deliveryState", "read")
                .get().await()

            for (doc in query.documents) {
                doc.reference.update("deliveryState", "read")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error marking messages as read: ${e.message}")
        }
    }

    fun syncIncomingMessagesToRoom(conversationId: String, peerUid: String, peerName: String): ListenerRegistration {
        return conversationsCollection.document(conversationId)
            .collection("messages")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Snapshot error for $conversationId", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.toObjects(CloudMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    scope.launch {
                        for (msg in messages) {
                            val entity = MessageEntity(
                                messageId = msg.id.ifEmpty { UUID.randomUUID().toString() },
                                conversationId = peerUid,
                                senderDeviceId = msg.senderUid,
                                recipientDeviceId = msg.recipientUid,
                                content = msg.text,
                                timestamp = msg.createdAt?.toDate()?.time ?: System.currentTimeMillis(),
                                deliveryState = if (msg.deliveryState == "read") "READ" else "DELIVERED",
                                hopCount = 0,
                                isOutgoing = (msg.senderUid != peerUid),
                                transportType = "INTERNET"
                            )
                            appDatabase.messageDao().insert(entity)
                        }
                    }
                }
            }
    }
}
