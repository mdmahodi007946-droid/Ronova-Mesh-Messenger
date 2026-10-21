package com.example.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.core.protocol.MeshPacket
import com.example.core.protocol.MeshPacketType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

enum class TransportRoute {
    DIRECT_BLUETOOTH,
    MESH_RELAY,
    INTERNET_REMOTE,
    OFFLINE_QUEUE
}

enum class RoutePreference(val code: String) {
    AUTOMATIC("auto"),
    BLUETOOTH_ONLY("bluetooth_only"),
    INTERNET_PREFERRED("internet_preferred");

    companion object {
        fun fromCode(code: String): RoutePreference {
            return entries.firstOrNull { it.code == code } ?: AUTOMATIC
        }
    }
}

/**
 * Real Mobile Internet & Connectivity Transport Manager for Ronova Mesh PRD v2.1.
 * Supports:
 * - Real-time network telemetry (WiFi, Cellular, Disconnected).
 * - Automatic route selection (Section 2.4).
 * - Real internet packet dispatch and delivery verification.
 */
class InternetTransportManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _isInternetAvailable = MutableStateFlow(false)
    val isInternetAvailable: StateFlow<Boolean> = _isInternetAvailable.asStateFlow()

    private val _isCellular = MutableStateFlow(false)
    val isCellular: StateFlow<Boolean> = _isCellular.asStateFlow()

    private val _isWifi = MutableStateFlow(false)
    val isWifi: StateFlow<Boolean> = _isWifi.asStateFlow()

    private val _incomingInternetPackets = MutableSharedFlow<MeshPacket>(extraBufferCapacity = 64)
    val incomingInternetPackets: SharedFlow<MeshPacket> = _incomingInternetPackets.asSharedFlow()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            updateNetworkCapabilities()
        }

        override fun onLost(network: Network) {
            updateNetworkCapabilities()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            updateNetworkCapabilities()
        }
    }

    init {
        registerNetworkCallback()
        updateNetworkCapabilities()
    }

    private fun registerNetworkCallback() {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
        } catch (_: Exception) {}
    }

    private fun updateNetworkCapabilities() {
        val cm = connectivityManager ?: return
        val activeNetwork = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNetwork)

        if (caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            _isInternetAvailable.value = true
            _isWifi.value = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            _isCellular.value = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } else {
            _isInternetAvailable.value = false
            _isWifi.value = false
            _isCellular.value = false
        }
    }

    /**
     * Determines best transport route based on PRD v2.1 Section 2.4:
     * 1. Prefer direct Bluetooth for nearby eligible peers.
     * 2. Use valid Bluetooth relay when destination is reachable through mesh.
     * 3. Use internet route when destination is remote or no usable Bluetooth route is available.
     * 4. Queue message when no route is available.
     */
    fun selectRoute(
        isDirectBluetoothConnected: Boolean,
        isBluetoothMeshReachable: Boolean,
        preference: RoutePreference
    ): TransportRoute {
        return when (preference) {
            RoutePreference.BLUETOOTH_ONLY -> {
                when {
                    isDirectBluetoothConnected -> TransportRoute.DIRECT_BLUETOOTH
                    isBluetoothMeshReachable -> TransportRoute.MESH_RELAY
                    else -> TransportRoute.OFFLINE_QUEUE
                }
            }
            RoutePreference.INTERNET_PREFERRED -> {
                when {
                    _isInternetAvailable.value -> TransportRoute.INTERNET_REMOTE
                    isDirectBluetoothConnected -> TransportRoute.DIRECT_BLUETOOTH
                    isBluetoothMeshReachable -> TransportRoute.MESH_RELAY
                    else -> TransportRoute.OFFLINE_QUEUE
                }
            }
            RoutePreference.AUTOMATIC -> {
                when {
                    isDirectBluetoothConnected -> TransportRoute.DIRECT_BLUETOOTH
                    isBluetoothMeshReachable -> TransportRoute.MESH_RELAY
                    _isInternetAvailable.value -> TransportRoute.INTERNET_REMOTE
                    else -> TransportRoute.OFFLINE_QUEUE
                }
            }
        }
    }

    /**
     * Sends encrypted packet over internet transport.
     * Performs real network IO without mock fallback.
     */
    suspend fun dispatchInternetPacket(packet: MeshPacket, gatewayUrl: String = "https://mesh-gateway.ronovamesh.org/api/v1/relay"): Boolean {
        if (!_isInternetAvailable.value) return false

        return withContext(Dispatchers.IO) {
            try {
                val jsonBody = packet.toJsonObject().toString()
                val body = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(gatewayUrl)
                    .post(body)
                    .addHeader("X-Ronova-Version", "2.1")
                    .addHeader("X-Ronova-Origin", packet.originDeviceId)
                    .addHeader("X-Ronova-Destination", packet.destinationDeviceId)
                    .build()

                val response = httpClient.newCall(request).execute()
                val success = response.isSuccessful
                response.close()
                success
            } catch (e: IOException) {
                // Network unreachable or gateway offline, return false so repository queues for retry
                false
            }
        }
    }

    fun cleanup() {
        try {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {}
    }
}
