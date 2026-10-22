package com.example.core.firebase

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreFriendRepository(
    private val db: FirebaseFirestore
) {
    companion object {
        private const val TAG = "FirestoreFriendRepo"

        fun create(context: Context): FirestoreFriendRepository {
            val databaseId = context.getString(R.string.firestore_database_id)
            val firestore = FirebaseFirestore.getInstance(databaseId)
            return FirestoreFriendRepository(firestore)
        }

        fun generateFriendshipId(uid1: String, uid2: String): String {
            return if (uid1 < uid2) "${uid1}_$uid2" else "${uid2}_$uid1"
        }
    }

    private val requestsCollection = db.collection("friend_requests")
    private val friendshipsCollection = db.collection("friendships")

    fun observeReceivedRequests(myUid: String): Flow<List<FriendRequest>> {
        return requestsCollection
            .whereEqualTo("recipientUid", myUid)
            .whereEqualTo("status", "pending")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(FriendRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt }
            }
    }

    fun observeSentRequests(myUid: String): Flow<List<FriendRequest>> {
        return requestsCollection
            .whereEqualTo("senderUid", myUid)
            .whereEqualTo("status", "pending")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(FriendRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt }
            }
    }

    fun observeFriendships(myUid: String): Flow<List<Friendship>> {
        val flowA = friendshipsCollection
            .whereEqualTo("userAUid", myUid)
            .snapshots()
            .map { it.toObjects(Friendship::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
        val flowB = friendshipsCollection
            .whereEqualTo("userBUid", myUid)
            .snapshots()
            .map { it.toObjects(Friendship::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
        return kotlinx.coroutines.flow.combine(flowA, flowB) { listA, listB ->
            (listA + listB).distinctBy { it.id }
        }
    }

    suspend fun sendFriendRequest(sender: UserProfile, recipient: UserProfile) {
        require(sender.uid != recipient.uid) { "Cannot send friend request to yourself" }
        require(sender.uid.isNotBlank() && recipient.uid.isNotBlank()) { "UIDs must not be blank" }

        // If recipient already sent a pending request to us, auto-accept it immediately
        val reverseRequestId = "${recipient.uid}_${sender.uid}"
        val reverseDoc = requestsCollection.document(reverseRequestId).get().await()
        if (reverseDoc.exists() && reverseDoc.getString("status") == "pending") {
            val revRequest = reverseDoc.toObject(FriendRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            if (revRequest != null) {
                Log.i(TAG, "[FriendRequest] Reciprocal pending request found from ${recipient.uid}. Auto-accepting.")
                acceptFriendRequest(revRequest)
                return
            }
        }

        val requestId = "${sender.uid}_${recipient.uid}"
        val existingDoc = requestsCollection.document(requestId).get().await()

        if (existingDoc.exists()) {
            val status = existingDoc.getString("status")
            if (status == "pending") {
                Log.i(TAG, "[FriendRequest] Request already pending: $requestId")
                return
            }
            // Existing cancelled or rejected request: re-activate as pending
            requestsCollection.document(requestId).update(
                mapOf(
                    "status" to "pending",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Log.i(TAG, "[FriendRequest] Reactivated request to pending: $requestId")
        } else {
            val requestData = mapOf(
                "id" to requestId,
                "senderUid" to sender.uid,
                "senderRonovaId" to sender.ronovaId,
                "senderDisplayName" to sender.displayName,
                "senderPhotoUrl" to sender.photoUrl,
                "recipientUid" to recipient.uid,
                "recipientRonovaId" to recipient.ronovaId,
                "recipientDisplayName" to recipient.displayName,
                "status" to "pending",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            requestsCollection.document(requestId).set(requestData).await()
            Log.i(TAG, "[FriendRequest] Created new pending request: $requestId")
        }
    }

    suspend fun acceptFriendRequest(request: FriendRequest) {
        val requestId = request.id
        // 1. Mark request accepted
        requestsCollection.document(requestId).update(
            mapOf(
                "status" to "accepted",
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        // 2. Create friendship record
        val friendshipId = generateFriendshipId(request.senderUid, request.recipientUid)
        val isSenderA = request.senderUid < request.recipientUid

        val friendshipData = mapOf(
            "id" to friendshipId,
            "userAUid" to if (isSenderA) request.senderUid else request.recipientUid,
            "userBUid" to if (isSenderA) request.recipientUid else request.senderUid,
            "userARonovaId" to if (isSenderA) request.senderRonovaId else request.recipientRonovaId,
            "userBRonovaId" to if (isSenderA) request.recipientRonovaId else request.senderRonovaId,
            "userADisplayName" to if (isSenderA) request.senderDisplayName else request.recipientDisplayName,
            "userBDisplayName" to if (isSenderA) request.recipientDisplayName else request.senderDisplayName,
            "createdAt" to FieldValue.serverTimestamp()
        )

        friendshipsCollection.document(friendshipId).set(friendshipData).await()
        Log.i(TAG, "[FriendRequest] Accepted request and created friendship: $friendshipId")
    }

    suspend fun rejectFriendRequest(requestId: String) {
        requestsCollection.document(requestId).update(
            mapOf(
                "status" to "rejected",
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        Log.i(TAG, "[FriendRequest] Rejected request: $requestId")
    }

    suspend fun cancelFriendRequest(requestId: String) {
        requestsCollection.document(requestId).update(
            mapOf(
                "status" to "cancelled",
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        Log.i(TAG, "[FriendRequest] Cancelled request: $requestId")
    }

    suspend fun removeFriendship(uid1: String, uid2: String) {
        val friendshipId = generateFriendshipId(uid1, uid2)
        try {
            friendshipsCollection.document(friendshipId).delete().await()
            Log.i(TAG, "[FriendRequest] Removed friendship: $friendshipId")
        } catch (e: Exception) {
            Log.e(TAG, "[FriendRequest] Error removing friendship $friendshipId", e)
            throw e
        }
    }
}
