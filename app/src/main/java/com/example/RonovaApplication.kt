package com.example

import android.app.Application
import com.example.core.audio.AudioCallManager
import com.example.core.bluetooth.BluetoothMeshManager
import com.example.core.crypto.CryptoManager
import com.example.core.database.AppDatabase
import com.example.data.MeshRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class RonovaApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this) }
    val cryptoManager by lazy { CryptoManager(this) }
    val repository by lazy { MeshRepository(database, this) }
    val bluetoothMeshManager by lazy {
        BluetoothMeshManager(this, cryptoManager, repository, applicationScope)
    }
    val audioCallManager by lazy {
        AudioCallManager(this, cryptoManager, bluetoothMeshManager, repository, applicationScope)
    }
    val setupRepository by lazy { com.example.data.SetupRepository(this) }
    val internetTransportManager by lazy { com.example.core.network.InternetTransportManager(this, applicationScope) }
    val firebaseAuthManager by lazy {
        ensureFirebaseInitialized()
        com.example.core.firebase.FirebaseAuthManager.getInstance()
    }
    val firestoreUserRepository by lazy {
        ensureFirebaseInitialized()
        com.example.core.firebase.FirestoreUserRepository.create(this)
    }
    val firestoreFriendRepository by lazy {
        ensureFirebaseInitialized()
        com.example.core.firebase.FirestoreFriendRepository.create(this)
    }
    val firestoreChatRepository by lazy {
        ensureFirebaseInitialized()
        com.example.core.firebase.FirestoreChatRepository.create(this, database)
    }

    private fun ensureFirebaseInitialized() {
        try {
            if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApplicationId(packageName)
                    .setProjectId("ai-studio-applet-webapp-b6997")
                    .setApiKey("AIzaSyDUMMYKEYINITIALIZATION")
                    .build()
                com.google.firebase.FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Exception) {
            android.util.Log.w("RonovaApplication", "Firebase initialization fallback: ${e.message}")
        }
    }

    override fun onCreate() {
        super.onCreate()
        ensureFirebaseInitialized()
        // Initialize crypto identity and prepare Bluetooth listeners
        val id = cryptoManager.deviceId
    }
}
