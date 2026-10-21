package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.launch
import com.example.ui.components.RonovaNavigationDrawerSheet
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.audio.CallState
import com.example.core.locale.LocaleHelper
import com.example.core.permission.PermissionManager
import com.example.ui.components.ConnectionStatusBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.ActiveVoiceCallScreen
import com.example.ui.screens.ArchivedMessagesScreen
import com.example.ui.screens.BatteryGuidanceScreen
import com.example.ui.screens.BlockedDevicesScreen
import com.example.ui.screens.BluetoothSetupScreen
import com.example.ui.screens.CallHistoryScreen
import com.example.ui.screens.ConversationListScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.DiscoveryScreen
import com.example.ui.screens.ErrorRecoveryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IdentitySetupScreen
import com.example.ui.screens.IncomingVoiceCallScreen
import com.example.ui.screens.MessageDeliveryDetailsScreen
import com.example.ui.screens.NotificationSettingsScreen
import com.example.ui.screens.OneToOneChatScreen
import com.example.ui.screens.OutgoingVoiceCallScreen
import com.example.ui.screens.PendingMessagesScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.PrivacyLicensesScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.RelayNetworkScreen
import com.example.ui.screens.RelaySettingsScreen
import com.example.ui.screens.SecuritySettingsScreen
import com.example.ui.screens.SettingsHubScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StorageSettingsScreen
import com.example.ui.screens.TrustedDevicesScreen
import com.example.ui.screens.UserPolicyScreen
import com.example.ui.screens.VerifyDeviceScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.RonovaCardBorder
import com.example.ui.theme.RonovaGoldLight
import com.example.ui.theme.RonovaPinkPrimary
import com.example.ui.theme.RonovaTextMuted
import com.example.ui.theme.RonovaTextPrimary
import com.example.ui.theme.RonovaTheme
import com.example.viewmodel.RonovaViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RonovaViewModel by viewModels()

    override fun attachBaseContext(newBase: android.content.Context) {
        val lang = LocaleHelper.getPersistedLanguage(newBase)
        super.attachBaseContext(LocaleHelper.getLocalizedContext(newBase, lang))
    }

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
            val currentLanguage by viewModel.currentLanguage.collectAsState()
            val isBangla = currentLanguage == LocaleHelper.LANGUAGE_BANGLA
            val baseContext = LocalContext.current

            val localizedContext = remember(currentLanguage) {
                LocaleHelper.getLocalizedContext(baseContext, currentLanguage)
            }
            val localizedConfiguration = remember(currentLanguage) {
                LocaleHelper.getLocalizedConfiguration(baseContext, currentLanguage)
            }

            CompositionLocalProvider(
                androidx.activity.compose.LocalActivityResultRegistryOwner provides this,
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedConfiguration
            ) {
                RonovaTheme(isBangla = isBangla) {
                    RonovaApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun RonovaApp(viewModel: RonovaViewModel) {
    val navController = rememberNavController()
    val callState by viewModel.currentCall.collectAsState()
    val context = LocalContext.current

    var showMicDeniedDialog by remember { mutableStateOf(false) }
    var pendingCallPeerId by remember { mutableStateOf<String?>(null) }

    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val peer = pendingCallPeerId
        pendingCallPeerId = null
        if (granted && peer != null) {
            viewModel.initiateVoiceCall(peer, peer)
            navController.navigate(Screen.OutgoingCall.createRoute(peer))
        } else if (!granted) {
            showMicDeniedDialog = true
        }
    }

    val onSafeVoiceCall: (String) -> Unit = { peerId ->
        if (PermissionManager.hasMicrophonePermission(context)) {
            viewModel.initiateVoiceCall(peerId, peerId)
            navController.navigate(Screen.OutgoingCall.createRoute(peerId))
        } else {
            pendingCallPeerId = peerId
            micLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    if (showMicDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showMicDeniedDialog = false },
            title = { Text(stringResource(R.string.perm_mic_required_title)) },
            text = { Text(stringResource(R.string.perm_mic_required_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        showMicDeniedDialog = false
                        PermissionManager.openAppSettings(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary)
                ) {
                    Text(stringResource(R.string.perm_open_settings))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showMicDeniedDialog = false }
                ) {
                    Text(stringResource(R.string.dialog_close))
                }
            }
        )
    }

    // Automatic navigation when an incoming call arrives
    LaunchedEffect(callState.state) {
        if (callState.state == CallState.INCOMING_RINGING) {
            navController.navigate(Screen.IncomingCall.createRoute(callState.peerDeviceId))
        } else if (callState.state == CallState.ACTIVE) {
            navController.navigate(Screen.ActiveCall.createRoute(callState.peerDeviceId))
        }
    }

    val conversations by viewModel.conversations.collectAsState()
    val callHistory by viewModel.callHistory.collectAsState()
    val pendingPackets by viewModel.pendingRelayPackets.collectAsState()
    val unreadMessagesCount = conversations.sumOf { it.unreadCount }
    val missedCallsCount = callHistory.count { it.callType == "MISSED" }
    val pendingRelaysCount = pendingPackets.size

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) {
        coroutineScope.launch { drawerState.close() }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Chats.route,
        Screen.CallHistory.route,
        Screen.Settings.route
    )

    data class BottomNavDestination(
        val route: String,
        val label: String,
        val icon: ImageVector,
        val testTag: String,
        val badgeCount: Int = 0
    )

    val bottomNavItems = listOf(
        BottomNavDestination(Screen.Home.route, stringResource(R.string.nav_home), Icons.Default.Home, "bottom_nav_home"),
        BottomNavDestination(Screen.Chats.route, stringResource(R.string.nav_chats), Icons.AutoMirrored.Filled.Chat, "bottom_nav_chats", unreadMessagesCount),
        BottomNavDestination(Screen.CallHistory.route, stringResource(R.string.nav_calls), Icons.Default.Call, "bottom_nav_calls", missedCallsCount),
        BottomNavDestination(Screen.Settings.route, stringResource(R.string.nav_settings), Icons.Default.Settings, "bottom_nav_settings")
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showBottomBar,
        drawerContent = {
            RonovaNavigationDrawerSheet(
                currentRoute = currentRoute,
                deviceId = viewModel.myDeviceId,
                unreadMessagesCount = unreadMessagesCount,
                missedCallsCount = missedCallsCount,
                pendingRelaysCount = pendingRelaysCount,
                onNavigate = { route ->
                    coroutineScope.launch { drawerState.close() }
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onClose = {
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        modifier = Modifier.border(
                            BorderStroke(1.dp, RonovaCardBorder.copy(alpha = 0.5f))
                        )
                    ) {
                        bottomNavItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(Screen.Home.route) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    if (item.badgeCount > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge(
                                                    containerColor = RonovaPinkPrimary,
                                                    contentColor = RonovaTextPrimary
                                                ) {
                                                    Text(
                                                        text = "${item.badgeCount}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.label,
                                                tint = if (selected) RonovaTextPrimary else RonovaTextMuted
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                            tint = if (selected) RonovaTextPrimary else RonovaTextMuted
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.5.sp,
                                        color = if (selected) RonovaTextPrimary else RonovaTextMuted
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = RonovaGoldLight,
                                    selectedIconColor = RonovaTextPrimary,
                                    selectedTextColor = RonovaTextPrimary,
                                    unselectedIconColor = RonovaTextMuted,
                                    unselectedTextColor = RonovaTextMuted
                                ),
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
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
                    onNavigateNext = { startDest ->
                        when (startDest) {
                            is com.example.data.StartDestination.Home -> {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                            is com.example.data.StartDestination.PermissionRecovery -> {
                                navController.navigate(Screen.Permissions.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                            is com.example.data.StartDestination.Setup -> {
                                val target = when (startDest.resumeAtStep) {
                                    2 -> Screen.Permissions.route
                                    3 -> Screen.BluetoothSetup.route
                                    4 -> Screen.IdentitySetup.route
                                    else -> Screen.Welcome.route
                                }
                                navController.navigate(target) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                            is com.example.data.StartDestination.Loading -> {}
                        }
                    }
                )
            }

            // 2. Welcome (Step 1)
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onContinue = {
                        viewModel.saveSetupStep(2)
                        navController.navigate(Screen.Permissions.route)
                    }
                )
            }

            // 3. Permissions (Step 2)
            composable(Screen.Permissions.route) {
                PermissionsScreen(
                    onPermissionsGranted = {
                        val setupPrefs = viewModel.setupPreferences.value
                        if (setupPrefs.state == com.example.data.SetupState.COMPLETED) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Permissions.route) { inclusive = true }
                            }
                        } else {
                            viewModel.saveSetupStep(3)
                            navController.navigate(Screen.BluetoothSetup.route)
                        }
                    }
                )
            }

            // 4. Bluetooth Setup (Step 3)
            composable(Screen.BluetoothSetup.route) {
                BluetoothSetupScreen(
                    viewModel = viewModel,
                    onSetupComplete = {
                        viewModel.saveSetupStep(4)
                        navController.navigate(Screen.IdentitySetup.route)
                    }
                )
            }

            // 5. Identity Verification (Step 4 & 5)
            composable(Screen.IdentitySetup.route) {
                IdentitySetupScreen(
                    viewModel = viewModel,
                    onSetupComplete = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // 7. Home Dashboard
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                    onNavigateToDiscovery = { navController.navigate(Screen.Discovery.route) },
                    onNavigateToChats = { navController.navigate(Screen.Chats.route) },
                    onNavigateToChat = { deviceId ->
                        navController.navigate(Screen.Chat.createRoute(deviceId))
                    },
                    onNavigateToRelayNetwork = { navController.navigate(Screen.RelayNetwork.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
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
                        onSafeVoiceCall(deviceId)
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
                        onSafeVoiceCall(deviceId)
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
                        onSafeVoiceCall(peerId)
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

            // Master Settings Hub
            composable(Screen.Settings.route) {
                SettingsHubScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToPrivacyPolicy = { navController.navigate(Screen.PrivacyPolicy.route) },
                    onNavigateToUserPolicy = { navController.navigate(Screen.UserPolicy.route) },
                    onNavigateToStorage = { navController.navigate(Screen.StorageSettings.route) },
                    onNavigateToArchivedMessages = { navController.navigate(Screen.ArchivedMessages.route) },
                    onNavigateToSecurity = { navController.navigate(Screen.SecuritySettings.route) },
                    onNavigateToTrustedDevices = { navController.navigate(Screen.TrustedDevices.route) },
                    onNavigateToBlockedDevices = { navController.navigate(Screen.BlockedDevices.route) },
                    onNavigateToRelaySettings = { navController.navigate(Screen.RelaySettings.route) },
                    onNavigateToCallHistory = { navController.navigate(Screen.CallHistory.route) },
                    onNavigateToBattery = { navController.navigate(Screen.BatteryGuidance.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.NotificationSettings.route) },
                    onNavigateToDiagnostics = { navController.navigate(Screen.Diagnostics.route) },
                    onNavigateToAbout = { navController.navigate(Screen.About.route) }
                )
            }

            // Privacy Policy Screen
            composable(Screen.PrivacyPolicy.route) {
                PrivacyPolicyScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // User Policy & Safety Screen
            composable(Screen.UserPolicy.route) {
                UserPolicyScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Archived Messages Screen
            composable(Screen.ArchivedMessages.route) {
                ArchivedMessagesScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // 21. Storage & Backup Settings
            composable(Screen.StorageSettings.route) {
                StorageSettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToArchived = { navController.navigate(Screen.ArchivedMessages.route) }
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
                    viewModel = viewModel,
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
}
