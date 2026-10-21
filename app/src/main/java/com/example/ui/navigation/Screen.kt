package com.example.ui.navigation

sealed class Screen(val route: String) {
    // Onboarding & Setup
    object Splash : Screen("splash")
    object SignIn : Screen("sign_in")
    object ProfileInit : Screen("profile_init")
    object Welcome : Screen("welcome")
    object Permissions : Screen("permissions")
    object BluetoothSetup : Screen("bluetooth_setup")
    object IdentitySetup : Screen("identity_setup")

    // Cloud Directory & Social (PRD v2.2)
    object Directory : Screen("directory")
    object FriendRequests : Screen("friend_requests")
    object FriendsList : Screen("friends_list")
    object EditProfile : Screen("edit_profile")
    object PublicUserProfile : Screen("public_user_profile/{targetUid}") {
        fun createRoute(targetUid: String) = "public_user_profile/$targetUid"
    }

    // Core Bottom Navigation (5 destinations per PRD v2.1 Section 9)
    object Home : Screen("home")
    object NetworkMap : Screen("network_map")
    object Chats : Screen("chats")
    object CallHistory : Screen("call_history")
    object Settings : Screen("settings")

    // Communication Details
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
    object UserProfile : Screen("user_profile")
    object ProfileLock : Screen("profile_lock")
    object ApprovedContacts : Screen("approved_contacts")

    // Mesh Network & Relay
    object RelayNetwork : Screen("network_map") // alias for NetworkMap
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

    // Security & Devices
    object TrustedDevices : Screen("trusted_devices")
    object BlockedDevices : Screen("blocked_devices")
    object SecuritySettings : Screen("security_settings")

    // System & Settings
    object StorageSettings : Screen("storage_settings")
    object BatteryGuidance : Screen("battery_guidance")
    object NotificationSettings : Screen("notification_settings")
    object PrivacyLicenses : Screen("privacy_licenses")
    object PrivacyPolicy : Screen("privacy_policy")
    object TermsAndConditions : Screen("terms_and_conditions")
    object RulesOfUse : Screen("rules_of_use")
    object HelpAndSupport : Screen("help_and_support")
    object UserPolicy : Screen("rules_of_use") // alias
    object ArchivedMessages : Screen("archived_messages")
    object Diagnostics : Screen("diagnostics")
    object About : Screen("about")
    object ErrorRecovery : Screen("error_recovery")
}
