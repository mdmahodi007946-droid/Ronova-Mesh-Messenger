package com.example.core.firebase

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    companion object {
        private const val TAG = "FirebaseAuthManager"
        @Volatile
        private var INSTANCE: FirebaseAuthManager? = null

        fun getInstance(): FirebaseAuthManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseAuthManager().also { INSTANCE = it }
            }
        }
    }

    private val _currentUserState = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUserState: StateFlow<FirebaseUser?> = _currentUserState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _currentUserState.value = firebaseAuth.currentUser
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    fun getAuthStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun getUserId(): String? = auth.currentUser?.uid

    private fun getDefaultWebClientId(context: Context): String {
        return try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) {
                context.getString(resId).trim()
            } else {
                "855661887849-n84hc8vamtmocgtam90u9760gblsckdm.apps.googleusercontent.com"
            }
        } catch (_: Exception) {
            "855661887849-n84hc8vamtmocgtam90u9760gblsckdm.apps.googleusercontent.com"
        }
    }

    fun attemptAutoSignIn(
        context: Context,
        scope: CoroutineScope,
        onAuthSuccess: (FirebaseUser) -> Unit,
        onUnauthenticated: () -> Unit
    ) {
        val current = auth.currentUser
        if (current != null) {
            _currentUserState.value = current
            onAuthSuccess(current)
            return
        }

        val clientId = getDefaultWebClientId(context)
        if (clientId.isEmpty()) {
            Log.d(TAG, "Silent auto-sign-in skipped: default_web_client_id is not configured")
            onUnauthenticated()
            return
        }

        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        _currentUserState.value = user
                        onAuthSuccess(user)
                    } else {
                        onUnauthenticated()
                    }
                } else {
                    onUnauthenticated()
                }
            } catch (e: Exception) {
                Log.d(TAG, "Silent auto-sign-in skipped: ${e.message}")
                onUnauthenticated()
            }
        }
    }

    private suspend fun processCredentialResult(
        credential: androidx.credentials.Credential,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
            if (googleIdToken.isBlank()) {
                onError("Received empty Google ID token from Google Play Services")
                return
            }
            val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = auth.signInWithCredential(authCredential).await()
            val user = authResult.user
            if (user != null) {
                _currentUserState.value = user
                onSuccess(user)
            } else {
                onError("Firebase user is null after sign in")
            }
        } else {
            onError("Unexpected credential type: ${credential::class.java.simpleName}")
        }
    }

    fun signInWithGoogle(
        activity: Activity,
        scope: CoroutineScope,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit = {}
    ) {
        val clientId = getDefaultWebClientId(activity)
        if (clientId.isEmpty()) {
            onError("Google Sign-In configuration is missing. Please ensure google-services.json is configured.")
            return
        }

        val credentialManager = CredentialManager.create(activity)

        scope.launch {
            var primaryException: Exception? = null

            // Primary Attempt: GetSignInWithGoogleOption
            try {
                val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
                val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()
                val result = credentialManager.getCredential(activity, request)
                processCredentialResult(result.credential, onSuccess, onError)
                return@launch
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "GetSignInWithGoogleOption cancelled: ${e.message}", e)
                val msg = e.message ?: ""
                val isExplicitUserDismissal = msg.contains("activity is cancelled by the user", ignoreCase = true) ||
                        msg.contains("User cancelled", ignoreCase = true)
                if (isExplicitUserDismissal) {
                    onCancelled()
                    return@launch
                }
                primaryException = e
            } catch (e: Exception) {
                Log.w(TAG, "GetSignInWithGoogleOption exception: ${e.message}", e)
                primaryException = e
            }

            // Secondary Fallback Attempt: GetGoogleIdOption without authorized accounts filter
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setServerClientId(clientId)
                    .setFilterByAuthorizedAccounts(false)
                    .setAutoSelectEnabled(false)
                    .build()
                val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
                val result = credentialManager.getCredential(activity, request)
                processCredentialResult(result.credential, onSuccess, onError)
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Fallback Google Sign-In cancelled: ${e.message}", e)
                val msg = e.message ?: ""
                val isExplicitUserDismissal = msg.contains("activity is cancelled by the user", ignoreCase = true) ||
                        msg.contains("User cancelled", ignoreCase = true)
                if (isExplicitUserDismissal) {
                    onCancelled()
                } else {
                    onError("Google Sign-In was cancelled or rejected by Play Services ($msg). Please check device Google accounts.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fallback Google Sign-In failed", e)
                val errorMsg = e.localizedMessage ?: primaryException?.localizedMessage ?: "Sign in failed"
                onError(errorMsg)
            }
        }
    }

    fun signOut(
        context: Context,
        scope: CoroutineScope,
        onComplete: () -> Unit
    ) {
        auth.signOut()
        scope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear credential state", e)
            } finally {
                onComplete()
            }
        }
    }
}
