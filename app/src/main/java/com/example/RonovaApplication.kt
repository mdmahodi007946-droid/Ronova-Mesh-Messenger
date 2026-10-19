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

    override fun onCreate() {
        super.onCreate()
        // Initialize crypto identity and prepare Bluetooth listeners
        val id = cryptoManager.deviceId
    }
}
