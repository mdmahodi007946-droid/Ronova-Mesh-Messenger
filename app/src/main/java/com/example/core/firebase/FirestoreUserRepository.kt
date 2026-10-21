package com.example.core.firebase

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class FirestoreUserRepository(
    private val db: FirebaseFirestore
) {
    companion object {
        private const val TAG = "FirestoreUserRepo"

        fun create(context: Context): FirestoreUserRepository {
            val databaseId = context.getString(R.string.firestore_database_id)
            val firestore = FirebaseFirestore.getInstance(databaseId)
            return FirestoreUserRepository(firestore)
        }
    }

    private val usersCollection = db.collection("users")

    fun observeProfile(uid: String): Flow<UserProfile?> {
        return usersCollection.document(uid).snapshots().map { snapshot ->
            if (snapshot.exists()) {
                snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else {
                null
            }
        }
    }

    suspend fun getProfileOnce(uid: String): UserProfile? {
        val snapshot = usersCollection.document(uid).get().await()
        return if (snapshot.exists()) {
            snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } else {
            null
        }
    }

    suspend fun createOrInitProfile(firebaseUser: FirebaseUser): UserProfile {
        val uid = firebaseUser.uid
        val existing = getProfileOnce(uid)
        if (existing != null) {
            // Update online presence
            usersCollection.document(uid).update(
                mapOf(
                    "isOnline" to true,
                    "presenceUpdatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            return existing.copy(isOnline = true)
        }

        // Generate collision-safe Ronova ID
        val randomNum = Random.nextInt(100000, 999999)
        val ronovaId = "RNV-$randomNum"
        val displayName = if (!firebaseUser.displayName.isNullOrBlank()) {
            firebaseUser.displayName!!
        } else {
            "Ronova Member"
        }
        val photoUrl = firebaseUser.photoUrl?.toString() ?: ""

        val newProfileMap = mapOf(
            "uid" to uid,
            "ronovaId" to ronovaId,
            "displayName" to displayName,
            "photoUrl" to photoUrl,
            "bio" to "Connected on Ronova Mesh",
            "profileVisibility" to "public",
            "presencePrivacy" to "public",
            "isOnline" to true,
            "presenceUpdatedAt" to FieldValue.serverTimestamp(),
            "accountStatus" to "active",
            "createdAt" to FieldValue.serverTimestamp()
        )

        usersCollection.document(uid).set(newProfileMap).await()

        return UserProfile(
            uid = uid,
            ronovaId = ronovaId,
            displayName = displayName,
            photoUrl = photoUrl,
            bio = "Connected on Ronova Mesh",
            profileVisibility = "public",
            presencePrivacy = "public",
            isOnline = true,
            accountStatus = "active"
        )
    }

    suspend fun updateProfile(
        uid: String,
        displayName: String,
        bio: String,
        profileVisibility: String,
        presencePrivacy: String
    ) {
        usersCollection.document(uid).update(
            mapOf(
                "displayName" to displayName.trim(),
                "bio" to bio.trim(),
                "profileVisibility" to profileVisibility,
                "presencePrivacy" to presencePrivacy
            )
        ).await()
    }

    suspend fun setPresence(uid: String, isOnline: Boolean) {
        try {
            usersCollection.document(uid).update(
                mapOf(
                    "isOnline" to isOnline,
                    "presenceUpdatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update presence: ${e.message}")
        }
    }

    fun searchPublicUsers(query: String, currentUid: String): Flow<List<UserProfile>> {
        val cleanQuery = query.trim().lowercase()
        return usersCollection
            .whereEqualTo("accountStatus", "active")
            .snapshots()
            .map { snapshot ->
                val allUsers = snapshot.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                allUsers.filter { user ->
                    user.uid != currentUid &&
                    user.profileVisibility != "private" &&
                    (cleanQuery.isEmpty() ||
                     user.ronovaId.lowercase().contains(cleanQuery) ||
                     user.displayName.lowercase().contains(cleanQuery) ||
                     user.bio.lowercase().contains(cleanQuery))
                }
            }
    }

    fun observeUsers(uids: List<String>): Flow<List<UserProfile>> {
        if (uids.isEmpty()) return flow { emit(emptyList()) }
        return usersCollection.snapshots().map { snapshot ->
            val allUsers = snapshot.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            allUsers.filter { it.uid in uids }
        }
    }
}
