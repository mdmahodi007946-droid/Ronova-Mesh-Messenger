package com.example.core.firebase

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
        return try {
            val snapshot = usersCollection.document(uid).get().await()
            if (snapshot.exists()) {
                snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get profile for $uid: ${e.message}")
            null
        }
    }

    suspend fun createOrInitProfile(firebaseUser: FirebaseUser): UserProfile {
        val uid = firebaseUser.uid
        val existing = getProfileOnce(uid)
        if (existing != null) {
            // Document already exists: preserve all custom fields (displayName, photoUrl, bio, ronovaId).
            // Only update active online presence.
            try {
                usersCollection.document(uid).update(
                    mapOf(
                        "isOnline" to true,
                        "presenceUpdatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (e: Exception) {
                Log.w(TAG, "Presence update error on login: ${e.message}")
            }
            return existing.copy(isOnline = true)
        }

        // Generate stable unique Ronova ID (only for brand new users)
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
        presencePrivacy: String,
        photoUrl: String? = null
    ) {
        val updateMap = mutableMapOf<String, Any>(
            "displayName" to displayName.trim(),
            "bio" to bio.trim(),
            "profileVisibility" to profileVisibility,
            "presencePrivacy" to presencePrivacy,
            "presenceUpdatedAt" to FieldValue.serverTimestamp()
        )
        if (photoUrl != null) {
            updateMap["photoUrl"] = photoUrl
        }
        usersCollection.document(uid).update(updateMap).await()
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
        val rawQuery = query.trim()
        val isAuth = FirebaseAuth.getInstance().currentUser != null

        // Non-sensitive diagnostic log
        Log.i(TAG, "[DirectorySearch] Executing query: '$rawQuery', AuthState: $isAuth")

        if (!isAuth) {
            Log.w(TAG, "[DirectorySearch] Aborted: User is unauthenticated")
            return flowOf(emptyList())
        }

        val cleanLower = rawQuery.lowercase()
        val normalizedRonovaId = when {
            rawQuery.startsWith("RNV-", ignoreCase = true) -> rawQuery.uppercase().replace(" ", "")
            rawQuery.startsWith("RNV", ignoreCase = true) -> "RNV-${rawQuery.substring(3).trim()}"
            rawQuery.all { it.isDigit() } && rawQuery.length in 5..7 -> "RNV-$rawQuery"
            else -> rawQuery.uppercase()
        }

        // Bounded query with limit to avoid fetching entire collection
        return usersCollection
            .whereEqualTo("accountStatus", "active")
            .limit(50)
            .snapshots()
            .map { snapshot ->
                try {
                    val allUsers = snapshot.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    val filtered = allUsers.filter { user ->
                        user.uid != currentUid &&
                        user.profileVisibility != "private" &&
                        (cleanLower.isEmpty() ||
                         user.ronovaId.equals(normalizedRonovaId, ignoreCase = true) ||
                         user.ronovaId.lowercase().contains(cleanLower) ||
                         user.displayName.lowercase().contains(cleanLower) ||
                         user.bio.lowercase().contains(cleanLower))
                    }
                    Log.i(TAG, "[DirectorySearch] Success: Returned ${filtered.size} public users for query '$rawQuery'")
                    filtered
                } catch (e: Exception) {
                    Log.e(TAG, "[DirectorySearch] Mapping failed: ${e.javaClass.simpleName} - ${e.message}")
                    emptyList()
                }
            }
    }

    fun observeUsers(uids: List<String>): Flow<List<UserProfile>> {
        if (uids.isEmpty()) return flowOf(emptyList())
        val distinctUids = uids.distinct().take(30)
        val flows = distinctUids.map { uid ->
            usersCollection.document(uid).snapshots().map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
        }
        return combine(flows) { profiles ->
            profiles.filterNotNull()
        }
    }
}
