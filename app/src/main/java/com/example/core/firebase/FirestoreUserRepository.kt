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
import org.json.JSONObject
import kotlin.random.Random

class FirestoreUserRepository(
    private val db: FirebaseFirestore,
    private val context: Context? = null
) {
    companion object {
        private const val TAG = "FirestoreUserRepo"
        private const val PREFS_NAME = "ronova_user_profiles"

        fun create(context: Context): FirestoreUserRepository {
            val databaseId = context.getString(R.string.firestore_database_id)
            val firestore = FirebaseFirestore.getInstance(databaseId)
            return FirestoreUserRepository(firestore, context.applicationContext)
        }
    }

    private val usersCollection = db.collection("users")

    fun getCachedProfile(uid: String): UserProfile? {
        val ctx = context ?: return null
        val sp = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = sp.getString("profile_$uid", null) ?: return null
        return try {
            val obj = JSONObject(json)
            UserProfile(
                uid = obj.optString("uid", uid),
                ronovaId = obj.optString("ronovaId", ""),
                displayName = obj.optString("displayName", ""),
                photoUrl = obj.optString("photoUrl", ""),
                bio = obj.optString("bio", ""),
                profileVisibility = obj.optString("profileVisibility", "public"),
                presencePrivacy = obj.optString("presencePrivacy", "public"),
                isOnline = obj.optBoolean("isOnline", false),
                accountStatus = obj.optString("accountStatus", "active")
            )
        } catch (e: Exception) {
            null
        }
    }

    fun saveCachedProfile(profile: UserProfile) {
        val ctx = context ?: return
        val sp = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        try {
            val obj = JSONObject().apply {
                put("uid", profile.uid)
                put("ronovaId", profile.ronovaId)
                put("displayName", profile.displayName)
                put("photoUrl", profile.photoUrl)
                put("bio", profile.bio)
                put("profileVisibility", profile.profileVisibility)
                put("presencePrivacy", profile.presencePrivacy)
                put("isOnline", profile.isOnline)
                put("accountStatus", profile.accountStatus)
            }
            sp.edit().putString("profile_${profile.uid}", obj.toString()).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cache profile locally: ${e.message}")
        }
    }

    fun observeProfile(uid: String): Flow<UserProfile?> {
        return usersCollection.document(uid).snapshots().map { snapshot ->
            if (snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                if (profile != null) {
                    saveCachedProfile(profile)
                }
                profile
            } else {
                getCachedProfile(uid)
            }
        }
    }

    suspend fun getProfileOnce(uid: String): UserProfile? {
        return try {
            val snapshot = usersCollection.document(uid).get().await()
            if (snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                if (profile != null) saveCachedProfile(profile)
                profile
            } else {
                getCachedProfile(uid)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get profile for $uid from cloud: ${e.message}, falling back to local cache")
            getCachedProfile(uid)
        }
    }

    suspend fun createOrInitProfile(firebaseUser: FirebaseUser): UserProfile {
        val uid = firebaseUser.uid
        val cached = getCachedProfile(uid)

        // 1. Try to read existing document from Firestore
        var existing: UserProfile? = null
        try {
            val snapshot = usersCollection.document(uid).get().await()
            if (snapshot.exists()) {
                existing = snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Network read error fetching profile for $uid: ${e.message}")
        }

        if (existing != null) {
            // Profile document already exists in Firestore!
            // CRITICAL: Preserve all saved custom fields (displayName, photoUrl, bio, and stable ronovaId).
            // Do NOT overwrite with Google defaults!
            val updated = existing.copy(isOnline = true)
            saveCachedProfile(updated)
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
            return updated
        }

        // 2. If server doc was missing or unreachable, check if we have a valid locally cached profile
        if (cached != null && cached.ronovaId.isNotBlank()) {
            Log.i(TAG, "Reusing cached profile for $uid with stable Ronova ID: ${cached.ronovaId}")
            val profileToRestore = cached.copy(isOnline = true)
            try {
                val newProfileMap = mapOf(
                    "uid" to profileToRestore.uid,
                    "ronovaId" to profileToRestore.ronovaId,
                    "displayName" to profileToRestore.displayName.ifBlank { firebaseUser.displayName ?: "Ronova Member" },
                    "photoUrl" to profileToRestore.photoUrl.ifBlank { firebaseUser.photoUrl?.toString() ?: "" },
                    "bio" to profileToRestore.bio.ifBlank { "Connected on Ronova Mesh" },
                    "profileVisibility" to profileToRestore.profileVisibility,
                    "presencePrivacy" to profileToRestore.presencePrivacy,
                    "isOnline" to true,
                    "presenceUpdatedAt" to FieldValue.serverTimestamp(),
                    "accountStatus" to "active",
                    "createdAt" to FieldValue.serverTimestamp()
                )
                usersCollection.document(uid).set(newProfileMap).await()
            } catch (e: Exception) {
                Log.w(TAG, "Could not sync cached profile to server: ${e.message}")
            }
            saveCachedProfile(profileToRestore)
            return profileToRestore
        }

        // 3. Brand new user: generate one stable, unique Ronova ID
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

        try {
            usersCollection.document(uid).set(newProfileMap).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create new user profile in Firestore: ${e.message}")
        }

        val newProfile = UserProfile(
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
        saveCachedProfile(newProfile)
        return newProfile
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

        // Write directly to Firestore
        usersCollection.document(uid).update(updateMap).await()

        // Update local SharedPreferences cache
        val current = getCachedProfile(uid) ?: getProfileOnce(uid)
        if (current != null) {
            val updated = current.copy(
                displayName = displayName.trim(),
                bio = bio.trim(),
                profileVisibility = profileVisibility,
                presencePrivacy = presencePrivacy,
                photoUrl = photoUrl ?: current.photoUrl
            )
            saveCachedProfile(updated)
        }
    }

    suspend fun setPresence(uid: String, isOnline: Boolean) {
        try {
            usersCollection.document(uid).update(
                mapOf(
                    "isOnline" to isOnline,
                    "presenceUpdatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            val cached = getCachedProfile(uid)
            if (cached != null) {
                saveCachedProfile(cached.copy(isOnline = isOnline))
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update presence for $uid: ${e.message}")
        }
    }

    fun searchPublicUsers(query: String, currentUid: String): Flow<List<UserProfile>> {
        val rawQuery = query.trim()
        val isAuth = FirebaseAuth.getInstance().currentUser != null

        // Non-sensitive diagnostic log per PRD requirements
        Log.i(TAG, "[DirectorySearch] Executing search. Query: '$rawQuery', Authenticated: $isAuth, CurrentUid: $currentUid")

        if (!isAuth) {
            Log.w(TAG, "[DirectorySearch] User is unauthenticated. Returning empty results.")
            return flowOf(emptyList())
        }

        val cleanLower = rawQuery.lowercase()
        val normalizedRonovaId = when {
            rawQuery.startsWith("RNV-", ignoreCase = true) -> "RNV-" + rawQuery.substring(4).trim().uppercase()
            rawQuery.startsWith("RNV", ignoreCase = true) -> "RNV-" + rawQuery.substring(3).trim().uppercase()
            rawQuery.all { it.isDigit() } && rawQuery.length in 5..7 -> "RNV-$rawQuery"
            else -> rawQuery.uppercase().trim()
        }

        // When query is empty: Browse active directory with limit 50
        if (rawQuery.isEmpty()) {
            return usersCollection
                .limit(50)
                .snapshots()
                .map { snapshot ->
                    try {
                        val users = snapshot.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        val visible = users.filter { it.uid != currentUid && it.profileVisibility != "private" }
                        Log.i(TAG, "[DirectorySearch] Browse returned ${visible.size} public users")
                        visible
                    } catch (e: Exception) {
                        Log.e(TAG, "[DirectorySearch] Browse mapping failed: ${e.message}")
                        emptyList()
                    }
                }
        }

        // Targeted search: Query by exact Ronova ID flow and general snapshot flow
        val idSearchFlow = usersCollection
            .whereEqualTo("ronovaId", normalizedRonovaId)
            .limit(10)
            .snapshots()
            .map { snap ->
                snap.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }

        val generalSearchFlow = usersCollection
            .limit(50)
            .snapshots()
            .map { snap ->
                val all = snap.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                all.filter { user ->
                    user.profileVisibility != "private" && (
                        user.ronovaId.equals(normalizedRonovaId, ignoreCase = true) ||
                        user.ronovaId.lowercase().contains(cleanLower) ||
                        user.displayName.lowercase().contains(cleanLower) ||
                        user.bio.lowercase().contains(cleanLower)
                    )
                }
            }

        return combine(idSearchFlow, generalSearchFlow) { idResults, generalResults ->
            val merged = (idResults + generalResults).distinctBy { it.uid }
            Log.i(TAG, "[DirectorySearch] Query '$rawQuery' returned ${merged.size} total results")
            merged
        }
    }

    fun observeUsers(uids: List<String>): Flow<List<UserProfile>> {
        if (uids.isEmpty()) return flowOf(emptyList())
        val distinctUids = uids.distinct().take(30)
        val flows = distinctUids.map { uid ->
            usersCollection.document(uid).snapshots().map { snapshot ->
                if (snapshot.exists()) {
                    val p = snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    if (p != null) saveCachedProfile(p)
                    p
                } else {
                    getCachedProfile(uid)
                }
            }
        }
        return combine(flows) { profiles ->
            profiles.filterNotNull()
        }
    }
}
