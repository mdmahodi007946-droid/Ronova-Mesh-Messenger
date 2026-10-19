package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.audio.CallState
import com.example.ui.components.ConnectionStatusBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.ActiveVoiceCallScreen
import com.example.ui.screens.BatteryGuidanceScreen
import com.example.ui.screens.BlockedDevicesScreen
import com.example.ui.screens.BluetoothSetupScreen
import com.example.ui.screens.CallHistoryScreen
import com.example.ui.screens.ConversationListScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.DiscoveryScreen
import com.example.ui.screens.ErrorRecoveryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IncomingVoiceCallScreen
import com.example.ui.screens.MessageDeliveryDetailsScreen
import com.example.ui.screens.NotificationSettingsScreen
import com.example.ui.screens.OneToOneChatScreen
import com.example.ui.screens.OutgoingVoiceCallScreen
import com.example.ui.screens.PendingMessagesScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.PrivacyLicensesScreen
import com.example.ui.screens.RelayNetworkScreen
import com.example.ui.screens.RelaySettingsScreen
import com.example.ui.screens.SecuritySettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StorageSettingsScreen
import com.example.ui.screens.TrustedDevicesScreen
import com.example.ui.screens.VerifyDeviceScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.RonovaTheme
import com.example.viewmodel.RonovaViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RonovaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            RonovaTheme {
                RonovaApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun RonovaApp(viewModel: RonovaViewModel) {
    val navController = rememberNavController()
    val callState by viewModel.currentCall.collectAsState()

    // Automatic navigation when an incoming call arrives
    LaunchedEffect(callState.state) {
        if (callState.state == CallState.INCOMING_RINGING) {
            navController.navigate(Screen.IncomingCall.createRoute(callState.peerDeviceId))
        } else if (callState.state == CallState.ACTIVE) {
            navController.navigate(Screen.ActiveCall.createRoute(callState.peerDeviceId))
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showStatusBar = currentRoute != null &&
        currentRoute != Screen.Splash.route &&
        currentRoute != Screen.Welcome.route &&
        currentRoute != Screen.Permissions.route &&
        !currentRoute.startsWith("active_call") &&
        !currentRoute.startsWith("incoming_call") &&
        !currentRoute.startsWith("outgoing_call")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (showStatusBar) {
                ConnectionStatusBar(
                    viewModel = viewModel,
                    onNavigateToDiscovery = {
                        navController.navigate(Screen.Discovery.route)
                    },
                    onNavigateToRelayNetwork = {
                        navController.navigate(Screen.RelayNetwork.route)
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. Splash
            composable(Screen.Splash.route) {
                SplashScreen(
                    viewModel = viewModel,
                    onNavigateNext = {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // 2. Welcome
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onContinue = { navController.navigate(Screen.Permissions.route) }
                )
            }

            // 3. Permissions
            composable(Screen.Permissions.route) {
                PermissionsScreen(
                    onPermissionsGranted = {
                        navController.navigate(Screen.BluetoothSetup.route)
                    }
                )
            }

            // 4. Bluetooth Setup
            composable(Screen.BluetoothSetup.route) {
                BluetoothSetupScreen(
                    viewModel = viewModel,
                    onSetupComplete = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            // 7. Home Dashboard
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToDiscovery = { navController.navigate(Screen.Discovery.route) },
                    onNavigateToChats = { navController.navigate(Screen.Chats.route) },
                    onNavigateToChat = { deviceId ->
                        navController.navigate(Screen.Chat.createRoute(deviceId))
                    },
                    onNavigateToRelayNetwork = { navController.navigate(Screen.RelayNetwork.route) },
                    onNavigateToSettings = { navController.navigate(Screen.SecuritySettings.route) },
                    onNavigateToPending = { navController.navigate(Screen.PendingMessages.route) },
                    onNavigateToVerify = { deviceId ->
                        navController.navigate(Screen.VerifyDevice.createRoute(deviceId))
                    },
                    onNavigateToDiagnostics = { navController.navigate(Screen.Diagnostics.route) }
                )
            }

            // 5. Discovery
            composable(Screen.Discovery.route) {
                DiscoveryScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToChat = { deviceId ->
                        navController.navigate(Screen.Chat.createRoute(deviceId))
                    },
                    onNavigateToVerify = { deviceId ->
                        navController.navigate(Screen.VerifyDevice.createRoute(deviceId))
                    }
                )
            }

            // 6. Verify Device
            composable(
                route = Screen.VerifyDevice.route,
                arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
            ) { backStackEntry ->
                val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
                VerifyDeviceScreen(
                    deviceId = deviceId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onStartChat = {
                        navController.navigate(Screen.Chat.createRoute(deviceId))
                    },
                    onVoiceCall = {
                        viewModel.initiateVoiceCall(deviceId, deviceId)
                        navController.navigate(Screen.OutgoingCall.createRoute(deviceId))
                    }
                )
            }

            // 8. Conversations List
            composable(Screen.Chats.route) {
                ConversationListScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToChat = { deviceId ->
                        navController.navigate(Screen.Chat.createRoute(deviceId))
                    },
                    onStartNewChat = { navController.navigate(Screen.Discovery.route) }
                )
            }

            // 9. Chat Screen
            composable(
                route = Screen.Chat.route,
                arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
            ) { backStackEntry ->
                val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
                OneToOneChatScreen(
                    deviceId = deviceId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onVoiceCall = {
                        viewModel.initiateVoiceCall(deviceId, deviceId)
                        navController.navigate(Screen.OutgoingCall.createRoute(deviceId))
                    },
                    onNavigateToDetails = { messageId ->
                        navController.navigate(Screen.DeliveryDetails.createRoute(messageId))
                    },
                    onNavigateToVerify = {
                        navController.navigate(Screen.VerifyDevice.createRoute(deviceId))
                    }
                )
            }

            // 10. Message Delivery Details
            composable(
                route = Screen.DeliveryDetails.route,
                arguments = listOf(navArgument("messageId") { type = NavType.StringType })
            ) { backStackEntry ->
                val messageId = backStackEntry.arguments?.getString("messageId") ?: ""
                MessageDeliveryDetailsScreen(
                    messageId = messageId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // 11. Pending Messages Queue
            composable(Screen.PendingMessages.route) {
                PendingMessagesScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // 12. Relay Network
            composable(Screen.RelayNetwork.route) {
                RelayNetworkScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToRelaySettings = { navController.navigate(Screen.RelaySettings.route) }
                )
            }

            // 13. Relay Participation Settings
            composable(Screen.RelaySettings.route) {
                RelaySettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // 14. Outgoing Voice Call
            composable(
                route = Screen.OutgoingCall.route,
                arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
            ) { backStackEntry ->
                val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
                OutgoingVoiceCallScreen(
                    deviceId = deviceId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // 15. Incoming Voice Call
            composable(
                route = Screen.IncomingCall.route,
                arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
            ) { backStackEntry ->
                val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
                IncomingVoiceCallScreen(
                    deviceId = deviceId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // 16. Active Voice Call
            composable(
                route = Screen.ActiveCall.route,
                arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
            ) { backStackEntry ->
                val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
                ActiveVoiceCallScreen(
                    deviceId = deviceId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // 17. Call History
            composable(Screen.CallHistory.route) {
                CallHistoryScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onRedial = { peerId ->
                        viewModel.initiateVoiceCall(peerId, peerId)
                        navController.navigate(Screen.OutgoingCall.createRoute(peerId))
                    }
                )
            }

            // 18. Trusted Devices
            composable(Screen.TrustedDevices.route) {
                TrustedDevicesScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToVerify = { devId ->
                        navController.navigate(Screen.VerifyDevice.createRoute(devId))
                    }
                )
            }

            // 19. Blocked Devices
            composable(Screen.BlockedDevices.route) {
                BlockedDevicesScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // 20. Security Settings
            composable(Screen.SecuritySettings.route) {
                SecuritySettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // 21. Storage & Backup Settings
            composable(Screen.StorageSettings.route) {
                StorageSettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // 22. Battery Guidance
            composable(Screen.BatteryGuidance.route) {
                BatteryGuidanceScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // 23. Notification Settings
            composable(Screen.NotificationSettings.route) {
                NotificationSettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // 24. Privacy & Licenses
            composable(Screen.PrivacyLicenses.route) {
                PrivacyLicensesScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // 25. Diagnostics
            composable(Screen.Diagnostics.route) {
                DiagnosticsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToErrorRecovery = { navController.navigate(Screen.ErrorRecovery.route) }
                )
            }

            // 26. About Ronova LTD
            composable(Screen.About.route) {
                AboutScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // 27. Error & Recovery
            composable(Screen.ErrorRecovery.route) {
                ErrorRecoveryScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
