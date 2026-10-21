package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.permission.PermissionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.setupDataStore: DataStore<Preferences> by preferencesDataStore(name = "ronova_setup_prefs")

enum class SetupState {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED
}

sealed class StartDestination {
    object Loading : StartDestination()
    data class Setup(val resumeAtStep: Int) : StartDestination()
    object Home : StartDestination()
    object PermissionRecovery : StartDestination()
}

data class SetupPreferences(
    val state: SetupState,
    val lastStep: Int,
    val schemaVersion: Int,
    val completedAt: Long
)

class SetupRepository(
    private val context: Context,
    private val dataStore: DataStore<Preferences> = context.setupDataStore
) {

    companion object {
        val KEY_SETUP_STATE = stringPreferencesKey("setup_state")
        val KEY_SETUP_LAST_STEP = intPreferencesKey("setup_last_step")
        val KEY_SETUP_SCHEMA_VERSION = intPreferencesKey("setup_schema_version")
        val KEY_SETUP_COMPLETED_AT = longPreferencesKey("setup_completed_at")
        const val CURRENT_SCHEMA_VERSION = 2
    }

    val setupPreferencesFlow: Flow<SetupPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val stateStr = preferences[KEY_SETUP_STATE]
            val state = when (stateStr) {
                SetupState.COMPLETED.name -> SetupState.COMPLETED
                SetupState.IN_PROGRESS.name -> SetupState.IN_PROGRESS
                else -> {
                    // Check v1.0 upgrade: if already has existing keys and permissions, treat as COMPLETED
                    if (isV1UpgradeCompleted()) {
                        SetupState.COMPLETED
                    } else {
                        SetupState.NOT_STARTED
                    }
                }
            }
            val lastStep = preferences[KEY_SETUP_LAST_STEP] ?: 1
            val schemaVersion = preferences[KEY_SETUP_SCHEMA_VERSION] ?: CURRENT_SCHEMA_VERSION
            val completedAt = preferences[KEY_SETUP_COMPLETED_AT] ?: 0L

            SetupPreferences(state, lastStep, schemaVersion, completedAt)
        }

    private fun isV1UpgradeCompleted(): Boolean {
        val cryptoPrefs = context.getSharedPreferences("ronova_crypto_identity", Context.MODE_PRIVATE)
        val hasKey = cryptoPrefs.getString("ec_public_key", null) != null
        val hasBtPerms = PermissionManager.hasBluetoothPermissions(context)
        return hasKey && hasBtPerms
    }

    suspend fun saveStepProgress(step: Int) {
        dataStore.edit { prefs ->
            prefs[KEY_SETUP_STATE] = SetupState.IN_PROGRESS.name
            prefs[KEY_SETUP_LAST_STEP] = step.coerceIn(1, 5)
            prefs[KEY_SETUP_SCHEMA_VERSION] = CURRENT_SCHEMA_VERSION
        }
    }

    suspend fun markCompleted() {
        dataStore.edit { prefs ->
            prefs[KEY_SETUP_STATE] = SetupState.COMPLETED.name
            prefs[KEY_SETUP_LAST_STEP] = 5
            prefs[KEY_SETUP_SCHEMA_VERSION] = CURRENT_SCHEMA_VERSION
            prefs[KEY_SETUP_COMPLETED_AT] = System.currentTimeMillis()
        }
    }

    suspend fun resetSetup() {
        dataStore.edit { prefs ->
            prefs[KEY_SETUP_STATE] = SetupState.NOT_STARTED.name
            prefs[KEY_SETUP_LAST_STEP] = 1
            prefs[KEY_SETUP_SCHEMA_VERSION] = CURRENT_SCHEMA_VERSION
            prefs[KEY_SETUP_COMPLETED_AT] = 0L
        }
    }

    suspend fun computeStartDestination(): StartDestination {
        val prefs = try {
            setupPreferencesFlow.first()
        } catch (e: Exception) {
            SetupPreferences(SetupState.NOT_STARTED, 1, CURRENT_SCHEMA_VERSION, 0L)
        }

        val hasBtPermissions = PermissionManager.hasBluetoothPermissions(context)

        return when (prefs.state) {
            SetupState.COMPLETED -> {
                if (!hasBtPermissions) {
                    StartDestination.PermissionRecovery
                } else {
                    StartDestination.Home
                }
            }
            SetupState.IN_PROGRESS -> {
                StartDestination.Setup(prefs.lastStep.coerceIn(1, 4))
            }
            SetupState.NOT_STARTED -> {
                StartDestination.Setup(1)
            }
        }
    }
}
