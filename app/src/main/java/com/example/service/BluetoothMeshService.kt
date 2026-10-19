package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.RonovaApplication

class BluetoothMeshService : Service() {

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothMeshService = this@BluetoothMeshService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val isCallActive = intent?.getBooleanExtra(EXTRA_IS_CALL_ACTIVE, false) == true
        val notification = buildMeshNotification(
            if (isCallActive) "Ronova Active Voice Call" else "Ronova Mesh Active",
            if (isCallActive) "Encrypted Bluetooth voice stream running" else "Relaying encrypted packets and listening for nearby peers"
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var fgsType = android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (isCallActive && com.example.core.permission.PermissionManager.hasMicrophonePermission(this)) {
                    fgsType = fgsType or android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                }
            }
            startForeground(NOTIFICATION_ID, notification, fgsType)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Ensure Bluetooth mesh server is running
        val app = application as? RonovaApplication
        app?.bluetoothMeshManager?.startServer()

        return START_STICKY
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val meshChannel = NotificationChannel(
                CHANNEL_MESH,
                "Ronova Mesh Background Network",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Bluetooth mesh routing and peer discovery active in background"
                setShowBadge(false)
            }
            nm.createNotificationChannel(meshChannel)

            val callChannel = NotificationChannel(
                CHANNEL_CALLS,
                "Ronova Voice Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming offline voice call alerts"
                enableVibration(true)
            }
            nm.createNotificationChannel(callChannel)
        }
    }

    private fun buildMeshNotification(title: String, content: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, BluetoothMeshService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_MESH)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Mesh", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_MESH = "ronova_mesh_channel"
        const val CHANNEL_CALLS = "ronova_calls_channel"
        const val ACTION_START = "com.example.ronova.START_SERVICE"
        const val ACTION_STOP = "com.example.ronova.STOP_SERVICE"
        const val EXTRA_IS_CALL_ACTIVE = "extra_is_call_active"

        fun startService(context: Context, isCallActive: Boolean = false) {
            val intent = Intent(context, BluetoothMeshService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_IS_CALL_ACTIVE, isCallActive)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, BluetoothMeshService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
