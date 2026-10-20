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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.ui.components.RonovaTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.RonovaCardBorder
import com.example.ui.theme.RonovaError
import com.example.ui.theme.RonovaPinkContainer
import com.example.ui.theme.RonovaPinkDark
import com.example.ui.theme.RonovaPinkLight
import com.example.ui.theme.RonovaPinkPrimary
import com.example.ui.theme.RonovaPinkSecondary
import com.example.ui.theme.RonovaSuccess
import com.example.ui.theme.RonovaTextMuted
import com.example.ui.theme.RonovaTextPrimary
import com.example.viewmodel.RonovaViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: RonovaViewModel,
    onNavigateNext: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1600L)
        onNavigateNext()
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
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("welcome_continue_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.welcome_get_started),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
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

    // Request only Bluetooth and Notifications here (Android 11 asks for Location for BLE).
    // Microphone is NEVER requested at first launch per prompt requirements.
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
        val btGranted = PermissionManager.hasBluetoothPermissions(context)
        if (btGranted) {
            onPermissionsGranted()
        } else {
            permissionsDeniedState = true
        }
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
                .padding(24.dp)
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
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.perm_why_needed),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = RonovaTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                PermissionExplanationItem(
                    icon = Icons.Default.Bluetooth,
                    title = stringResource(R.string.perm_bt_title),
                    description = stringResource(R.string.perm_bt_desc)
                )

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                    PermissionExplanationItem(
                        icon = Icons.Default.Security,
                        title = stringResource(R.string.perm_loc_title),
                        description = stringResource(R.string.perm_loc_desc)
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PermissionExplanationItem(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.perm_notif_title),
                        description = stringResource(R.string.perm_notif_desc)
                    )
                }

                PermissionExplanationItem(
                    icon = Icons.Default.Security,
                    title = stringResource(R.string.perm_zero_location_title),
                    description = stringResource(R.string.perm_zero_location_desc)
                )

                // Educational note that Microphone will only be requested when making voice calls
                PermissionExplanationItem(
                    icon = Icons.Default.Mic,
                    title = stringResource(R.string.perm_mic_title),
                    description = stringResource(R.string.perm_mic_explanation)
                )

                if (permissionsDeniedState) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = RonovaPinkLight.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, RonovaPinkSecondary)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = stringResource(R.string.perm_denied_warning),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = RonovaPinkDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        launcher.launch(requiredPermissions)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("request_permissions_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.perm_grant_button),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                if (permissionsDeniedState) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { PermissionManager.openAppSettings(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("open_settings_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp), tint = RonovaPinkPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.perm_open_settings),
                            color = RonovaPinkPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onPermissionsGranted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("skip_permissions_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.perm_continue_setup),
                        color = RonovaPinkPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
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
                                Color(0xFFDCFCE7) else RonovaPinkLight
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = if (status == BluetoothStatus.READY || status == BluetoothStatus.CONNECTED)
                            RonovaSuccess else RonovaPinkPrimary,
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
                                color = RonovaPinkDark
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
                    Button(
                        onClick = {
                            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                            enableBtLauncher.launch(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("enable_bluetooth_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.bt_turn_on),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = onSetupComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("finish_setup_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (status == BluetoothStatus.READY || status == BluetoothStatus.CONNECTED)
                            RonovaPinkPrimary else Color.Gray
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.bt_enter_mesh),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
