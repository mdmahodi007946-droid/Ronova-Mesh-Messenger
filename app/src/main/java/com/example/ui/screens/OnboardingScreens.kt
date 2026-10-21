package com.example.ui.screens

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.bluetooth.BluetoothStatus
import com.example.core.permission.PermissionManager
import com.example.ui.components.Ronova3DButton
import com.example.ui.components.RonovaTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.RonovaCardBorder
import com.example.ui.theme.RonovaError
import com.example.ui.theme.RonovaGoldContainer
import com.example.ui.theme.RonovaGoldDark
import com.example.ui.theme.RonovaGoldLight
import com.example.ui.theme.RonovaGoldPrimary
import com.example.ui.theme.RonovaGoldSecondary
import com.example.ui.theme.RonovaPinkContainer
import com.example.ui.theme.RonovaPinkDark
import com.example.ui.theme.RonovaPinkLight
import com.example.ui.theme.RonovaPinkPrimary
import com.example.ui.theme.RonovaPinkSecondary
import com.example.ui.theme.RonovaSuccess
import com.example.ui.theme.RonovaTextMuted
import com.example.ui.theme.RonovaTextPrimary
import com.example.data.StartDestination
import com.example.viewmodel.RonovaViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: RonovaViewModel,
    onNavigateNext: (StartDestination) -> Unit
) {
    LaunchedEffect(Unit) {
        val dest = viewModel.getStartDestination()
        delay(1200L)
        onNavigateNext(dest)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.White, RonovaPinkLight, RonovaPinkContainer)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(RonovaPinkPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Router,
                    contentDescription = stringResource(R.string.app_name),
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.splash_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = RonovaTextPrimary
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.splash_subtitle),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = RonovaPinkDark,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(
                color = RonovaPinkPrimary,
                modifier = Modifier.size(28.dp),
                strokeWidth = 2.5.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.splash_initializing),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = RonovaTextMuted,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
fun WelcomeScreen(
    onContinue: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(RonovaPinkLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Router,
                        contentDescription = null,
                        tint = RonovaPinkPrimary,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.welcome_title),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = RonovaTextPrimary
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.welcome_subtitle),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = RonovaTextMuted,
                        lineHeight = 22.sp
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))

                WelcomeFeatureItem(
                    icon = Icons.Default.WifiOff,
                    title = stringResource(R.string.feat_offline_title),
                    description = stringResource(R.string.feat_offline_desc)
                )
                WelcomeFeatureItem(
                    icon = Icons.Default.Router,
                    title = stringResource(R.string.feat_relay_title),
                    description = stringResource(R.string.feat_relay_desc)
                )
                WelcomeFeatureItem(
                    icon = Icons.Default.Lock,
                    title = stringResource(R.string.feat_crypto_title),
                    description = stringResource(R.string.feat_crypto_desc)
                )
                WelcomeFeatureItem(
                    icon = Icons.Default.Mic,
                    title = stringResource(R.string.feat_voice_title),
                    description = stringResource(R.string.feat_voice_desc)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Ronova3DButton(
                    text = stringResource(R.string.welcome_get_started),
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "welcome_continue_button"
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun WelcomeFeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, RonovaCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(RonovaPinkLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RonovaPinkPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = RonovaTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = RonovaTextMuted,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
fun PermissionsScreen(
    onPermissionsGranted: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var permissionsDeniedState by remember { mutableStateOf(false) }

    var hasBt by remember { mutableStateOf(PermissionManager.hasBluetoothPermissions(context)) }
    var hasLoc by remember { mutableStateOf(PermissionManager.hasLocationPermission(context)) }
    var hasNotif by remember { mutableStateOf(PermissionManager.hasNotificationPermission(context)) }
    var hasMic by remember { mutableStateOf(PermissionManager.hasMicrophonePermission(context)) }

    fun refreshPermissions() {
        hasBt = PermissionManager.hasBluetoothPermissions(context)
        hasLoc = PermissionManager.hasLocationPermission(context)
        hasNotif = PermissionManager.hasNotificationPermission(context)
        hasMic = PermissionManager.hasMicrophonePermission(context)
    }

    LaunchedEffect(Unit) {
        refreshPermissions()
    }

    val requiredPermissions = remember {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        } else {
            permissions.add(Manifest.permission.BLUETOOTH)
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissions.toTypedArray()
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        refreshPermissions()
        val btGranted = PermissionManager.hasBluetoothPermissions(context)
        if (btGranted) {
            onPermissionsGranted()
        } else {
            permissionsDeniedState = true
        }
    }

    val singleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        refreshPermissions()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                RonovaTopBar(
                    title = stringResource(R.string.perm_screen_title),
                    subtitle = stringResource(R.string.perm_screen_subtitle),
                    onBack = onBack
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.perm_why_needed),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = RonovaTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                PermissionRowItem(
                    icon = Icons.Default.Bluetooth,
                    title = stringResource(R.string.perm_bt_title),
                    description = stringResource(R.string.perm_bt_desc),
                    isGranted = hasBt,
                    onRequest = {
                        launcher.launch(requiredPermissions)
                    }
                )

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                    PermissionRowItem(
                        icon = Icons.Default.Security,
                        title = stringResource(R.string.perm_loc_title),
                        description = stringResource(R.string.perm_loc_desc),
                        isGranted = hasLoc,
                        onRequest = {
                            singleLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PermissionRowItem(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.perm_notif_title),
                        description = stringResource(R.string.perm_notif_desc),
                        isGranted = hasNotif,
                        onRequest = {
                            singleLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    )
                }

                PermissionRowItem(
                    icon = Icons.Default.Mic,
                    title = stringResource(R.string.perm_mic_title),
                    description = stringResource(R.string.perm_mic_explanation),
                    isGranted = hasMic,
                    onRequest = {
                        singleLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                )

                PermissionExplanationItem(
                    icon = Icons.Default.Security,
                    title = stringResource(R.string.perm_zero_location_title),
                    description = stringResource(R.string.perm_zero_location_desc)
                )

                if (permissionsDeniedState) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = RonovaGoldLight),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, RonovaCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = stringResource(R.string.perm_denied_warning),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = RonovaGoldDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Ronova3DButton(
                    text = stringResource(R.string.perm_grant_button),
                    onClick = { launcher.launch(requiredPermissions) },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "request_permissions_button"
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { PermissionManager.openAppSettings(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("open_settings_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, RonovaGoldPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RonovaGoldDark)
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp), tint = RonovaGoldDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.perm_open_settings),
                        color = RonovaGoldDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onPermissionsGranted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("skip_permissions_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, RonovaCardBorder)
                ) {
                    Text(
                        text = stringResource(R.string.perm_continue_setup),
                        color = RonovaTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun PermissionRowItem(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, RonovaCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isGranted) Color(0xFFE8F5E9) else RonovaGoldLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isGranted) RonovaSuccess else RonovaGoldDark,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = RonovaTextPrimary,
                        fontSize = 13.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = RonovaTextMuted,
                        lineHeight = 16.sp,
                        fontSize = 11.sp
                    )
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (isGranted) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, RonovaSuccess.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = RonovaSuccess,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.perm_status_granted),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RonovaSuccess
                        )
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onRequest,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, RonovaGoldPrimary),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(
                        text = stringResource(R.string.perm_request_action),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RonovaGoldDark
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionExplanationItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, RonovaCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(RonovaPinkLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RonovaPinkPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = RonovaTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = RonovaTextMuted,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
fun BluetoothSetupScreen(
    viewModel: RonovaViewModel,
    onSetupComplete: () -> Unit
) {
    val status by viewModel.bluetoothStatus.collectAsState()
    val enableBtLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkBluetoothStatus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RonovaTopBar(
                    title = stringResource(R.string.bt_setup_title),
                    subtitle = stringResource(R.string.bt_setup_subtitle)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            if (status == BluetoothStatus.READY || status == BluetoothStatus.CONNECTED)
                                Color(0xFFDCFCE7) else RonovaGoldLight
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = if (status == BluetoothStatus.READY || status == BluetoothStatus.CONNECTED)
                            RonovaSuccess else RonovaGoldDark,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = when (status) {
                        BluetoothStatus.READY, BluetoothStatus.CONNECTED -> stringResource(R.string.bt_status_ready)
                        BluetoothStatus.DISABLED -> stringResource(R.string.bt_status_disabled)
                        BluetoothStatus.PERMISSION_REQUIRED -> stringResource(R.string.bt_status_permission_required)
                        BluetoothStatus.UNSUPPORTED -> stringResource(R.string.bt_status_unsupported)
                        else -> stringResource(R.string.bt_status_checking)
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = RonovaTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                StatusBadge(status = status)
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, RonovaCardBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = stringResource(R.string.bt_device_id_label),
                            style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = viewModel.myDeviceId,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = RonovaGoldDark
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.bt_device_id_desc),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = RonovaTextMuted,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                if (status == BluetoothStatus.DISABLED) {
                    Ronova3DButton(
                        text = stringResource(R.string.bt_turn_on),
                        onClick = {
                            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                            enableBtLauncher.launch(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "enable_bluetooth_button"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                val canEnter = status == BluetoothStatus.READY || status == BluetoothStatus.CONNECTED
                Ronova3DButton(
                    text = "পরবর্তী ধাপ: পরিচয় যাচাই",
                    onClick = onSetupComplete,
                    enabled = canEnter,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "finish_setup_button"
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun IdentitySetupScreen(
    viewModel: RonovaViewModel,
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RonovaTopBar(
                    title = stringResource(R.string.identity_setup_title),
                    subtitle = stringResource(R.string.identity_setup_subtitle)
                )
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(RonovaGoldLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = RonovaGoldDark,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(R.string.identity_keystore_verified),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = RonovaTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.identity_keystore_desc),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = RonovaTextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Identity Card with Fingerprint & Copy Action
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, RonovaCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.home_node_identity),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = RonovaTextMuted,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Device ID", viewModel.myDeviceId))
                                    android.widget.Toast.makeText(context, context.getString(R.string.home_copied_id), android.widget.Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = stringResource(R.string.home_node_id_copy_desc),
                                    tint = RonovaGoldDark,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "কপি",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RonovaGoldDark
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = viewModel.myDeviceId,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = RonovaTextPrimary,
                                fontSize = 13.5.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Specs list
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, RonovaCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.identity_specs_title),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = RonovaTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.identity_spec_ecdh),
                            style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextPrimary, fontSize = 12.sp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.identity_spec_aes),
                            style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextPrimary, fontSize = 12.sp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.identity_spec_hmac),
                            style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextPrimary, fontSize = 12.sp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.identity_spec_relay),
                            style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted, fontSize = 11.5.sp)
                        )
                    }
                }

                if (hasError) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = RonovaError.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, RonovaError.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = errorMessage,
                            color = RonovaError,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Ronova3DButton(
                    text = stringResource(R.string.identity_complete_button),
                    onClick = {
                        val hasBtPerm = viewModel.hasBluetoothPermissions()
                        val isBtOn = viewModel.isBluetoothEnabled()
                        val hasId = viewModel.myDeviceId.isNotEmpty()

                        if (!hasBtPerm || !isBtOn || !hasId) {
                            hasError = true
                            errorMessage = context.getString(R.string.identity_setup_failed_error)
                        } else {
                            hasError = false
                            viewModel.completeSetup()
                            onSetupComplete()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "complete_setup_button"
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
