package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.RonovaTopBar
import com.example.ui.theme.RonovaError
import com.example.ui.theme.RonovaPinkContainer
import com.example.ui.theme.RonovaPinkDark
import com.example.ui.theme.RonovaPinkLight
import com.example.ui.theme.RonovaPinkPrimary
import com.example.ui.theme.RonovaSuccess
import com.example.ui.theme.RonovaTextMuted
import com.example.ui.theme.RonovaTextPrimary
import com.example.ui.theme.RonovaWarning
import com.example.viewmodel.RonovaViewModel

@Composable
fun TrustedDevicesScreen(
    viewModel: RonovaViewModel,
    onBack: () -> Unit,
    onNavigateToVerify: (String) -> Unit
) {
    val trustedDevices by viewModel.trustedDevices.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAFAFA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = "Trusted Devices",
                subtitle = "Cryptographically Verified Peers",
                onBack = onBack
            )

            if (trustedDevices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(RonovaPinkLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = RonovaPinkPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No trusted peers yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Verify device cryptographic fingerprints to pin them as trusted contacts.",
                            style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(trustedDevices) { dev ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToVerify(dev.deviceId) },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFDCFCE7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = RonovaSuccess,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = dev.displayName,
                                            fontWeight = FontWeight.Bold,
                                            color = RonovaTextPrimary
                                        )
                                        Text(
                                            text = dev.deviceId,
                                            fontSize = 11.sp,
                                            color = RonovaTextMuted
                                        )
                                    }
                                }
                                OutlinedButton(
                                    onClick = { viewModel.setDeviceTrust(dev.deviceId, "UNTRUSTED") },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Revoke", fontSize = 11.sp, color = RonovaTextMuted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BlockedDevicesScreen(
    viewModel: RonovaViewModel,
    onBack: () -> Unit
) {
    val blockedDevices by viewModel.blockedDevices.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAFAFA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = "Blocked Devices",
                subtitle = "Prevent packets and calls from these nodes",
                onBack = onBack
            )

            if (blockedDevices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(RonovaPinkLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = RonovaPinkPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No blocked devices",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Blocked nodes cannot send you messages or trigger incoming voice calls.",
                            style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(blockedDevices) { dev ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = dev.displayName,
                                        fontWeight = FontWeight.Bold,
                                        color = RonovaTextPrimary
                                    )
                                    Text(
                                        text = dev.deviceId,
                                        fontSize = 11.sp,
                                        color = RonovaError
                                    )
                                }
                                Button(
                                    onClick = { viewModel.setDeviceTrust(dev.deviceId, "UNTRUSTED") },
                                    colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Unblock", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SecuritySettingsScreen(
    viewModel: RonovaViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAFAFA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = "Security & Encryption",
                subtitle = "Cryptographic identity verification",
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Your Device Cryptographic Fingerprint",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = viewModel.myDeviceId,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = RonovaPinkDark
                                    )
                                )
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Device ID", viewModel.myDeviceId))
                                    Toast.makeText(context, "Copied ID to clipboard", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = RonovaPinkPrimary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Stable EC public key SHA-256 fingerprint. No centralized identity server required.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = RonovaTextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Cryptographic Architecture",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("• Key Agreement: ECDH (Curve secp256r1 / NIST P-256)", fontSize = 12.sp, color = RonovaTextPrimary)
                            Text("• Symmetric Cipher: AES-256-GCM (12-byte IV, 128-bit Auth Tag)", fontSize = 12.sp, color = RonovaTextPrimary)
                            Text("• Packet Integrity: HMAC-SHA256 signature", fontSize = 12.sp, color = RonovaTextPrimary)
                            Text("• Relay Privacy: Intermediate nodes forward encrypted payload only", fontSize = 12.sp, color = RonovaTextPrimary)
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = {
                            viewModel.resetCryptographicIdentity()
                            Toast.makeText(context, "New cryptographic identity generated", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RonovaError),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Regenerate Identity Keypair", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StorageSettingsScreen(
    viewModel: RonovaViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val dbSizeBytes = viewModel.getDatabaseSizeBytes()
    val dbSizeKb = dbSizeBytes / 1024

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAFAFA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = "Storage & Offline Backup",
                subtitle = "Local Room SQLite Database",
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Local Database Size",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$dbSizeKb KB ($dbSizeBytes bytes)",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RonovaPinkDark
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "All messages, device identities, and routing queues are strictly stored on-device in Room SQLite.",
                                fontSize = 11.sp,
                                color = RonovaTextMuted
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            viewModel.clearRelayQueue()
                            Toast.makeText(context, "Relay queue cleared", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear Completed Relay Cache", fontWeight = FontWeight.Bold)
                    }
                }

                item {
                    OutlinedButton(
                        onClick = {
                            viewModel.clearCallHistory()
                            Toast.makeText(context, "Call history cleared", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RonovaError),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete All Call Logs", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BatteryGuidanceScreen(
    viewModel: RonovaViewModel,
    onBack: () -> Unit
) {
    val isRunning by viewModel.isForegroundServiceRunning.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAFAFA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = "Battery & Background Guidance",
                subtitle = "Ensuring reliable mesh relaying",
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Mesh Foreground Service",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Keeps Bluetooth RFCOMM server alive when phone is locked.",
                                        fontSize = 11.sp,
                                        color = RonovaTextMuted
                                    )
                                }
                                Switch(
                                    checked = isRunning,
                                    onCheckedChange = { viewModel.toggleForegroundService(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = RonovaPinkPrimary)
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "OEM Battery Optimization Advice",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• Samsung: Settings → Apps → Ronova Mesh → Battery → Select 'Unrestricted'.",
                                fontSize = 12.sp,
                                color = RonovaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Xiaomi/MIUI: Enable 'Autostart' and set Battery Saver to 'No restrictions'.",
                                fontSize = 12.sp,
                                color = RonovaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Google Pixel: Settings → Apps → Ronova Mesh → App battery usage → Unrestricted.",
                                fontSize = 12.sp,
                                color = RonovaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Without unrestricted battery access, Android may pause Bluetooth background sockets after 10 minutes of screen-off.",
                                fontSize = 11.sp,
                                color = RonovaWarning
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit
) {
    var callRingtone by remember { mutableStateOf(true) }
    var vibration by remember { mutableStateOf(true) }
    var messageSound by remember { mutableStateOf(true) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAFAFA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = "Notification Settings",
                subtitle = "Call alerts and message ringtones",
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Incoming Voice Call Alerts", fontWeight = FontWeight.Bold)
                            Switch(
                                checked = callRingtone,
                                onCheckedChange = { callRingtone = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = RonovaPinkPrimary)
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Vibrate on Messages & Calls", fontWeight = FontWeight.Bold)
                            Switch(
                                checked = vibration,
                                onCheckedChange = { vibration = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = RonovaPinkPrimary)
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Message Chime", fontWeight = FontWeight.Bold)
                            Switch(
                                checked = messageSound,
                                onCheckedChange = { messageSound = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = RonovaPinkPrimary)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyLicensesScreen(
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAFAFA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = "Privacy Policy & Licenses",
                subtitle = "Strict Offline Privacy Commitment",
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Ronova Privacy Guarantee",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RonovaPinkDark
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "1. Zero Internet Access: Ronova does not declare INTERNET permission. No packet can ever leave via the internet.",
                                fontSize = 12.sp,
                                color = RonovaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "2. Zero Tracking / Telemetry: No analytics SDKs, advertising SDKs, or crash uploaders.",
                                fontSize = 12.sp,
                                color = RonovaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "3. Zero Location Harvesting: Bluetooth operates strictly with 'neverForLocation' flags.",
                                fontSize = 12.sp,
                                color = RonovaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "4. End-to-End Cryptography: Only sender and intended recipient hold decryption keys.",
                                fontSize = 12.sp,
                                color = RonovaTextPrimary
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Open Source Acknowledgments",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("• Android Jetpack Compose & Material 3 (Apache 2.0)", fontSize = 11.sp, color = RonovaTextPrimary)
                            Text("• Kotlin Coroutines & Flow (Apache 2.0)", fontSize = 11.sp, color = RonovaTextPrimary)
                            Text("• Android Room Persistence Library (Apache 2.0)", fontSize = 11.sp, color = RonovaTextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAFAFA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = "About Ronova LTD",
                subtitle = "Decentralized Communication Innovation",
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(RonovaPinkLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = RonovaPinkPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Ronova Bluetooth Mesh Messenger",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = RonovaTextPrimary
                        )
                    )
                    Text(
                        text = "Version 1.0 Production Release",
                        fontSize = 12.sp,
                        color = RonovaPinkDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Project Mission",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Ronova LTD is dedicated to providing free, sovereign communication tools that empower communities during network outages, emergency scenarios, or off-grid environments.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = RonovaTextMuted,
                                    lineHeight = 18.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Developer & Project Owner:",
                                fontSize = 11.sp,
                                color = RonovaTextMuted
                            )
                            Text(
                                text = "Ronova LTD",
                                fontWeight = FontWeight.Bold,
                                color = RonovaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Official Support Contact:",
                                fontSize = 11.sp,
                                color = RonovaTextMuted
                            )
                            Text(
                                text = "ronovaltd@gmail.com",
                                fontWeight = FontWeight.Bold,
                                color = RonovaPinkDark
                            )
                        }
                    }
                }
            }
        }
    }
}
