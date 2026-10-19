package com.example.ui.navigation

sealed class Screen(val route: String) {
    // Onboarding
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Permissions : Screen("permissions")
    object BluetoothSetup : Screen("bluetooth_setup")

    // Core Mesh & Communication
    object Home : Screen("home")
    object Chats : Screen("chats")
    object Chat : Screen("chat/{deviceId}") {
        fun createRoute(deviceId: String) = "chat/$deviceId"
    }
    object DeliveryDetails : Screen("delivery_details/{messageId}") {
        fun createRoute(messageId: String) = "delivery_details/$messageId"
    }
    object PendingMessages : Screen("pending_messages")

    // Discovery & Identity
    object Discovery : Screen("discovery")
    object VerifyDevice : Screen("verify_device/{deviceId}") {
        fun createRoute(deviceId: String) = "verify_device/$deviceId"
    }

    // Mesh Network & Relay
    object RelayNetwork : Screen("relay_network")
    object RelaySettings : Screen("relay_settings")

    // Voice Calling
    object OutgoingCall : Screen("outgoing_call/{deviceId}") {
        fun createRoute(deviceId: String) = "outgoing_call/$deviceId"
    }
    object IncomingCall : Screen("incoming_call/{deviceId}") {
        fun createRoute(deviceId: String) = "incoming_call/$deviceId"
    }
    object ActiveCall : Screen("active_call/{deviceId}") {
        fun createRoute(deviceId: String) = "active_call/$deviceId"
    }
    object CallHistory : Screen("call_history")

    // Security & Devices
    object TrustedDevices : Screen("trusted_devices")
    object BlockedDevices : Screen("blocked_devices")
    object SecuritySettings : Screen("security_settings")

    // System & Settings
    object StorageSettings : Screen("storage_settings")
    object BatteryGuidance : Screen("battery_guidance")
    object NotificationSettings : Screen("notification_settings")
    object PrivacyLicenses : Screen("privacy_licenses")
    object Diagnostics : Screen("diagnostics")
    object About : Screen("about")
    object ErrorRecovery : Screen("error_recovery")
}
