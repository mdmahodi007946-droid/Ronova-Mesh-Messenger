package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.stringResource
import com.example.R
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleanHands
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WifiOff
import com.example.core.database.MessageEntity
import com.example.data.RetentionPolicy
import com.example.ui.components.SpecFeatureItem
import com.example.ui.theme.RonovaCardBorder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
import com.example.ui.theme.RonovaTextSecondary
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
        ) {
            RonovaTopBar(
                title = "Security & Encryption",
                subtitle = "Cryptographic identity verification",
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RonovaCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Your Device Cryptographic Fingerprint",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = RonovaPinkLight,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = viewModel.myDeviceId,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = RonovaPinkDark
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Device ID", viewModel.myDeviceId))
                                        Toast.makeText(context, "Copied ID to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(RonovaPinkLight)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = RonovaPinkPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Stable EC public key SHA-256 fingerprint. No centralized identity server required.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = RonovaTextMuted,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RonovaCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Cryptographic Architecture",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("• Key Agreement: ECDH (Curve secp256r1 / NIST P-256)", fontSize = 13.sp, color = RonovaTextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Symmetric Cipher: AES-256-GCM (12-byte IV, 128-bit Auth Tag)", fontSize = 13.sp, color = RonovaTextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Packet Integrity: HMAC-SHA256 signature", fontSize = 13.sp, color = RonovaTextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Relay Privacy: Intermediate nodes forward encrypted payload only", fontSize = 13.sp, color = RonovaTextPrimary)
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
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, RonovaError.copy(alpha = 0.7f))
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
    onBack: () -> Unit,
    onNavigateToArchived: () -> Unit
) {
    val context = LocalContext.current
    val dbSizeBytes = viewModel.getDatabaseSizeBytes()
    val dbSizeKb = dbSizeBytes / 1024
    val currentPolicy by viewModel.retentionPolicy.collectAsState()
    val olderCount by viewModel.olderThan30DaysCount.collectAsState()
    val archivedCount by viewModel.archivedMessagesCount.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAFAFA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = "Storage & Auto-Retention",
                subtitle = "Local Room SQLite & 30-Day Policy",
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Database Storage Stat Card
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
                                Column {
                                    Text(
                                        text = "Local Database Size",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "On-Device Room SQLite",
                                        fontSize = 11.sp,
                                        color = RonovaTextMuted
                                    )
                                }
                                Surface(
                                    color = RonovaPinkLight,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "$dbSizeKb KB",
                                        fontWeight = FontWeight.Bold,
                                        color = RonovaPinkDark,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "All messages, device identities, and routing packets are stored purely in local storage. No cloud server or external sync exists.",
                                fontSize = 11.5.sp,
                                color = RonovaTextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // 30-Day Auto Retention Policy Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = RonovaPinkPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "30-Day Message Retention Policy",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = RonovaTextPrimary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Select automatic message retention or archiving policy to optimize storage and maintain peak performance on local devices.",
                                fontSize = 12.sp,
                                color = RonovaTextMuted,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Policy Option 1: Auto-Delete
                            RetentionPolicyRow(
                                title = "Auto-Delete (> 30 Days)",
                                subtitle = "Automatically delete messages older than 30 days to free storage (Recommended)",
                                isSelected = currentPolicy == RetentionPolicy.AUTO_DELETE,
                                onClick = {
                                    viewModel.setRetentionPolicy(RetentionPolicy.AUTO_DELETE)
                                    Toast.makeText(context, "30-Day Auto-Delete policy activated", Toast.LENGTH_SHORT).show()
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Policy Option 2: Auto-Archive
                            RetentionPolicyRow(
                                title = "Auto-Archive (> 30 Days)",
                                subtitle = "Move messages older than 30 days from active chat into local archive",
                                isSelected = currentPolicy == RetentionPolicy.AUTO_ARCHIVE,
                                onClick = {
                                    viewModel.setRetentionPolicy(RetentionPolicy.AUTO_ARCHIVE)
                                    Toast.makeText(context, "30-Day Auto-Archive policy activated", Toast.LENGTH_SHORT).show()
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Policy Option 3: Keep All
                            RetentionPolicyRow(
                                title = "Keep All Forever (No Auto-Cleanup)",
                                subtitle = "Do not auto-delete or archive messages (All history preserved locally)",
                                isSelected = currentPolicy == RetentionPolicy.KEEP_ALL,
                                onClick = {
                                    viewModel.setRetentionPolicy(RetentionPolicy.KEEP_ALL)
                                    Toast.makeText(context, "Retention policy set to Keep All", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }

                // Storage Health & Quick Action Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Retention Status & Quick Actions",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    color = RonovaPinkLight.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Messages > 30 Days",
                                            fontSize = 11.sp,
                                            color = RonovaTextMuted
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "$olderCount",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RonovaPinkDark
                                        )
                                    }
                                }
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    color = RonovaPinkLight.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Archived Messages",
                                            fontSize = 11.sp,
                                            color = RonovaTextMuted
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "$archivedCount",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RonovaPinkDark
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.runRetentionCleanupNow { count ->
                                            Toast.makeText(
                                                context,
                                                if (count > 0) "Cleaned/Processed $count messages older than 30 days" else "No messages older than 30 days found",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.CleanHands, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Run Cleanup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.archiveMessagesOlderThan30Days { count ->
                                            Toast.makeText(
                                                context,
                                                if (count > 0) "Archived $count messages" else "No messages to archive",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RonovaPinkDark),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Archive > 30d", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = onNavigateToArchived,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = RonovaPinkPrimary),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("View Archived Messages ($archivedCount)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // General Maintenance
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Queue & Log Maintenance",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    viewModel.clearRelayQueue()
                                    Toast.makeText(context, "Completed relay cache cleared", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkLight),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = RonovaPinkDark, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Clear Completed Relay Cache", color = RonovaPinkDark, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    viewModel.clearCallHistory()
                                    Toast.makeText(context, "Call history cleared", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = RonovaError),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delete All Voice Call Logs", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RetentionPolicyRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) RonovaPinkLight.copy(alpha = 0.5f) else Color(0xFFF9F9F9)
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, RonovaPinkPrimary) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) RonovaPinkPrimary else RonovaTextMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isSelected) RonovaPinkDark else RonovaTextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = RonovaTextMuted,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun ArchivedMessagesScreen(
    viewModel: RonovaViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val archivedMessages by viewModel.archivedMessages.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAFAFA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = "Archived Messages",
                subtitle = "${archivedMessages.size} Messages Stored Offline",
                onBack = onBack,
                actions = {
                    if (archivedMessages.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.deleteAllArchivedMessages { count ->
                                Toast.makeText(context, "Deleted $count archived messages", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete All", tint = RonovaError)
                        }
                    }
                }
            )

            if (archivedMessages.isEmpty()) {
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
                                imageVector = Icons.Default.Archive,
                                contentDescription = null,
                                tint = RonovaPinkPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Archived Messages",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Messages older than 30 days will appear here when Auto-Archive policy is active.",
                            style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Archived items are hidden from chat lists",
                            fontSize = 11.5.sp,
                            color = RonovaTextMuted
                        )
                        Button(
                            onClick = {
                                viewModel.unarchiveAllMessages { count ->
                                    Toast.makeText(context, "Restored $count messages to chat", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkLight),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Restore All", fontSize = 11.sp, color = RonovaPinkDark, fontWeight = FontWeight.Bold)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(archivedMessages) { msg ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = if (msg.isOutgoing) "To: ${msg.recipientDeviceId}" else "From: ${msg.senderDeviceId}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = RonovaPinkDark
                                            )
                                            Text(
                                                text = dateFormat.format(Date(msg.timestamp)),
                                                fontSize = 10.sp,
                                                color = RonovaTextMuted
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = msg.content,
                                            fontSize = 13.sp,
                                            color = RonovaTextPrimary,
                                            maxLines = 2,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.unarchiveMessage(msg.messageId)
                                            Toast.makeText(context, "Restored to chat", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Unarchive,
                                            contentDescription = "Restore",
                                            tint = RonovaPinkPrimary
                                        )
                                    }
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

                item {
                    val context = LocalContext.current
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = android.net.Uri.fromParts("package", context.packageName, null)
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("battery_settings_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryAlert,
                            contentDescription = null,
                            tint = RonovaTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Open System Battery Settings",
                            fontWeight = FontWeight.Bold,
                            color = RonovaTextPrimary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationSettingsScreen(
    viewModel: RonovaViewModel,
    onBack: () -> Unit
) {
    val callAlerts by viewModel.callAlertsEnabled.collectAsState()
    val vibration by viewModel.vibrationEnabled.collectAsState()
    val messageSound by viewModel.messageSoundEnabled.collectAsState()

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
                                checked = callAlerts,
                                onCheckedChange = { viewModel.setCallAlertsEnabled(it) },
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
                                onCheckedChange = { viewModel.setVibrationEnabled(it) },
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
                                onCheckedChange = { viewModel.setMessageSoundEnabled(it) },
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
fun PrivacyPolicyScreen(
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAFAFA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = "Privacy Policy",
                subtitle = "Offline Privacy & Cryptographic Assurance",
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
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(RonovaPinkLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = RonovaPinkPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "1. Zero-Internet & Zero-Cloud Architecture",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = RonovaTextPrimary
                                    )
                                    Text(
                                        text = "Guaranteed complete offline communication",
                                        fontSize = 11.sp,
                                        color = RonovaTextMuted
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Ronova Mesh Messenger operates completely independent of centralized servers, cellular data, or internet connectivity. The application does not declare or request the Android INTERNET permission, ensuring that your messages, contacts, and voice audio can never leave your device over the internet.",
                                fontSize = 12.5.sp,
                                color = RonovaTextSecondary,
                                lineHeight = 18.sp
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
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(RonovaPinkLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = RonovaPinkPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "2. End-to-End Encryption (E2EE)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = RonovaTextPrimary
                                    )
                                    Text(
                                        text = "ECDH P-256 + AES-256-GCM Cryptography",
                                        fontSize = 11.sp,
                                        color = RonovaTextMuted
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Every private message is encrypted using standard Elliptic Curve Diffie-Hellman (secp256r1) key agreement and AES-256-GCM authenticated encryption. Your device's private keys never leave local storage. Only you and the intended recipient possess the keys necessary to decrypt message plaintext.",
                                fontSize = 12.5.sp,
                                color = RonovaTextSecondary,
                                lineHeight = 18.sp
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
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(RonovaPinkLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Hub,
                                        contentDescription = null,
                                        tint = RonovaPinkPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "3. Intermediate Relay Privacy",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = RonovaTextPrimary
                                    )
                                    Text(
                                        text = "Zero Intermediate Node Payload Access",
                                        fontSize = 11.sp,
                                        color = RonovaTextMuted
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "When intermediate devices forward packets across the multi-hop mesh, they only process ciphertext envelopes and routing metadata. Intermediate nodes cannot inspect private message content or tamper with message signatures without triggering immediate packet rejection.",
                                fontSize = 12.5.sp,
                                color = RonovaTextSecondary,
                                lineHeight = 18.sp
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
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(RonovaPinkLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storage,
                                        contentDescription = null,
                                        tint = RonovaPinkPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "4. Local SQLite Storage & Retention",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = RonovaTextPrimary
                                    )
                                    Text(
                                        text = "Local SQLite Storage & 30-Day Auto Retention",
                                        fontSize = 11.sp,
                                        color = RonovaTextMuted
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Your messages, peer contacts, and call history are stored exclusively in the local on-device Room SQLite database. You retain full control over retention policies, including automatic 30-day archiving, auto-deletion, or permanent local preservation.",
                                fontSize = 12.5.sp,
                                color = RonovaTextSecondary,
                                lineHeight = 18.sp
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
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(RonovaPinkLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PrivacyTip,
                                        contentDescription = null,
                                        tint = RonovaPinkPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "5. Zero Tracking, Zero Ads & No Profiling",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = RonovaTextPrimary
                                    )
                                    Text(
                                        text = "Zero Analytics, Ads, or Location Profiling",
                                        fontSize = 11.sp,
                                        color = RonovaTextMuted
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "The application contains no analytics SDKs, trackers, or advertising frameworks. Bluetooth scanning runs with 'neverForLocation' flags purely to discover physical peer radios in range. Your geographical location is never recorded or tracked.",
                                fontSize = 12.5.sp,
                                color = RonovaTextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserPolicyScreen(
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAFAFA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = "User Policy & Operational Safety",
                subtitle = "Rules of Engagement & Security Guidelines",
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
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(RonovaPinkLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Gavel,
                                        contentDescription = null,
                                        tint = RonovaPinkPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "1. Acceptable Use Guidelines",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = RonovaTextPrimary
                                    )
                                    Text(
                                        text = "Rules of Application Usage",
                                        fontSize = 11.sp,
                                        color = RonovaTextMuted
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "• Ronova is engineered for sovereign offline communication, emergency coordination, network outages, and private peer-to-peer messaging.\n• Intentional network flooding, spamming radio airwaves, or malicious packet injection is strictly prohibited.\n• Users remain solely responsible for communication authored on their nodes.",
                                fontSize = 12.5.sp,
                                color = RonovaTextSecondary,
                                lineHeight = 19.sp
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
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(RonovaPinkLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = RonovaPinkPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "2. Cryptographic Device Verification",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = RonovaTextPrimary
                                    )
                                    Text(
                                        text = "Identity Verification & MitM Protection",
                                        fontSize = 11.sp,
                                        color = RonovaTextMuted
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "• When establishing communication with a new peer, open the 'Verify Device' screen to compare cryptographic fingerprints in person or via secondary channel.\n• Marking a peer as 'Trusted' verifies their public key and prevents impersonation or Man-in-the-Middle attacks.",
                                fontSize = 12.5.sp,
                                color = RonovaTextSecondary,
                                lineHeight = 19.sp
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
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(RonovaPinkLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Block,
                                        contentDescription = null,
                                        tint = RonovaPinkPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "3. Suspicious Node Blocking",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = RonovaTextPrimary
                                    )
                                    Text(
                                        text = "Isolating Malicious or Unwanted Nodes",
                                        fontSize = 11.sp,
                                        color = RonovaTextMuted
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "• If an unknown node transmits unsolicited messages or voice call requests, mark the device as 'Blocked'.\n• Blocked devices cannot send messages, trigger voice call ringers, or utilize your device's relay queue.",
                                fontSize = 12.5.sp,
                                color = RonovaTextSecondary,
                                lineHeight = 19.sp
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
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(RonovaPinkLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = null,
                                        tint = RonovaPinkPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "4. Direct Voice Call Security",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = RonovaTextPrimary
                                    )
                                    Text(
                                        text = "Direct Point-to-Point RFCOMM Audio",
                                        fontSize = 11.sp,
                                        color = RonovaTextMuted
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "• Two-way voice calls stream directly over point-to-point Bluetooth RFCOMM sockets between the two handsets.\n• No audio recordings are saved locally or transmitted to external servers.\n• Microphone recording is engaged exclusively during an active call and immediately terminated upon hangup.",
                                fontSize = 12.5.sp,
                                color = RonovaTextSecondary,
                                lineHeight = 19.sp
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
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(RonovaPinkLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BatteryAlert,
                                        contentDescription = null,
                                        tint = RonovaPinkPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "5. Background Relay Reliability",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = RonovaTextPrimary
                                    )
                                    Text(
                                        text = "Foreground Service & Battery Guidance",
                                        fontSize = 11.sp,
                                        color = RonovaTextMuted
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "To ensure packets can be received and relayed while your screen is turned off, keep the Foreground Service enabled and configure battery settings to 'Unrestricted' for Ronova in Android system settings.",
                                fontSize = 12.5.sp,
                                color = RonovaTextSecondary,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsHubScreen(
    viewModel: RonovaViewModel,
    onBack: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToUserPolicy: () -> Unit,
    onNavigateToStorage: () -> Unit,
    onNavigateToArchivedMessages: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToTrustedDevices: () -> Unit,
    onNavigateToBlockedDevices: () -> Unit,
    onNavigateToRelaySettings: () -> Unit,
    onNavigateToCallHistory: () -> Unit,
    onNavigateToBattery: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val retentionPolicy by viewModel.retentionPolicy.collectAsState()
    val archivedCount by viewModel.archivedMessagesCount.collectAsState()
    val isRelayOn by viewModel.isRelayOptedIn.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAFAFA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = stringResource(R.string.settings_title),
                subtitle = stringResource(R.string.settings_subtitle),
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 0: Language Selection
                item {
                    Text(
                        text = stringResource(R.string.settings_language_section),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = RonovaPinkDark
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = BorderStroke(1.dp, RonovaCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.settings_language_title),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = stringResource(R.string.settings_language_subtitle),
                                fontSize = 11.5.sp,
                                color = RonovaTextMuted
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val isEn = currentLang == "en"
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setLanguage("en") }
                                        .testTag("lang_english_button"),
                                    color = if (isEn) RonovaPinkLight else Color(0xFFF9F9F9),
                                    shape = RoundedCornerShape(12.dp),
                                    border = if (isEn) BorderStroke(1.5.dp, RonovaPinkPrimary) else null
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isEn) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (isEn) RonovaPinkPrimary else RonovaTextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.lang_english),
                                            fontWeight = if (isEn) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isEn) RonovaPinkDark else RonovaTextPrimary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                val isBn = currentLang == "bn"
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setLanguage("bn") }
                                        .testTag("lang_bangla_button"),
                                    color = if (isBn) RonovaPinkLight else Color(0xFFF9F9F9),
                                    shape = RoundedCornerShape(12.dp),
                                    border = if (isBn) BorderStroke(1.5.dp, RonovaPinkPrimary) else null
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isBn) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (isBn) RonovaPinkPrimary else RonovaTextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.lang_bangla),
                                            fontWeight = if (isBn) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isBn) RonovaPinkDark else RonovaTextPrimary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 1: Policies & Security
                item {
                    Text(
                        text = stringResource(R.string.settings_policies_section),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = RonovaPinkDark
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )
                }

                item {
                    SettingsTileCard(
                        icon = Icons.Default.Shield,
                        title = stringResource(R.string.settings_privacy_policy),
                        subtitle = stringResource(R.string.settings_privacy_policy_sub),
                        onClick = onNavigateToPrivacyPolicy
                    )
                }

                item {
                    SettingsTileCard(
                        icon = Icons.Default.Gavel,
                        title = stringResource(R.string.settings_user_policy),
                        subtitle = stringResource(R.string.settings_user_policy_sub),
                        onClick = onNavigateToUserPolicy
                    )
                }

                // Section 2: Storage & 30-Day Auto Retention
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.settings_storage_section),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = RonovaPinkDark
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )
                }

                item {
                    SettingsTileCard(
                        icon = Icons.Default.Storage,
                        title = stringResource(R.string.settings_storage),
                        subtitle = "Policy: ${retentionPolicy.title}",
                        badgeText = "30-Day Policy",
                        onClick = onNavigateToStorage
                    )
                }

                item {
                    SettingsTileCard(
                        icon = Icons.Default.FolderZip,
                        title = stringResource(R.string.settings_archived),
                        subtitle = "$archivedCount messages stored in local archive",
                        badgeText = if (archivedCount > 0) "$archivedCount" else null,
                        onClick = onNavigateToArchivedMessages
                    )
                }

                // Section 3: Security & Devices
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.settings_security_section),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = RonovaPinkDark
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )
                }

                item {
                    SettingsTileCard(
                        icon = Icons.Default.Lock,
                        title = stringResource(R.string.settings_security),
                        subtitle = stringResource(R.string.settings_security_sub),
                        onClick = onNavigateToSecurity
                    )
                }

                item {
                    SettingsTileCard(
                        icon = Icons.Default.CheckCircle,
                        title = stringResource(R.string.settings_trusted),
                        subtitle = stringResource(R.string.settings_trusted_sub),
                        onClick = onNavigateToTrustedDevices
                    )
                }

                item {
                    SettingsTileCard(
                        icon = Icons.Default.Block,
                        title = stringResource(R.string.settings_blocked),
                        subtitle = stringResource(R.string.settings_blocked_sub),
                        onClick = onNavigateToBlockedDevices
                    )
                }

                // Section 4: Mesh Network & Call History
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.settings_mesh_section),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = RonovaPinkDark
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )
                }

                item {
                    SettingsTileCard(
                        icon = Icons.Default.Hub,
                        title = stringResource(R.string.settings_relay),
                        subtitle = if (isRelayOn) "Relay active (Participating in mesh)" else "Relay disabled",
                        onClick = onNavigateToRelaySettings
                    )
                }

                item {
                    SettingsTileCard(
                        icon = Icons.Default.Call,
                        title = stringResource(R.string.settings_calls),
                        subtitle = stringResource(R.string.settings_calls_sub),
                        onClick = onNavigateToCallHistory
                    )
                }

                // Section 5: Device Optimization & About
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "System & App Info",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = RonovaPinkDark
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )
                }

                item {
                    SettingsTileCard(
                        icon = Icons.Default.BatteryAlert,
                        title = "Battery & Background Guidance",
                        subtitle = "Protect mesh background service from OS task killers",
                        onClick = onNavigateToBattery
                    )
                }

                item {
                    SettingsTileCard(
                        icon = Icons.Default.Notifications,
                        title = "Notifications & Alerts",
                        subtitle = "Incoming call ringtone and message vibrations",
                        onClick = onNavigateToNotifications
                    )
                }

                item {
                    SettingsTileCard(
                        icon = Icons.Default.Router,
                        title = "Application Diagnostics",
                        subtitle = "Bluetooth RFCOMM telemetry and packet analysis",
                        onClick = onNavigateToDiagnostics
                    )
                }

                item {
                    SettingsTileCard(
                        icon = Icons.Default.Info,
                        title = "About Ronova LTD",
                        subtitle = "Version 1.0 • Ronova LTD contact & open source info",
                        onClick = onNavigateToAbout
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun SettingsTileCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
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
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = RonovaTextPrimary
                    )
                    if (badgeText != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = RonovaPinkLight,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = RonovaPinkDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = RonovaTextMuted,
                    lineHeight = 15.sp
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = RonovaPinkLight,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun AboutScreen(
    onBack: () -> Unit
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
                .widthIn(max = 680.dp)
        ) {
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
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(RonovaPinkLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = RonovaPinkPrimary,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Ronova Bluetooth Mesh Messenger",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = RonovaTextPrimary
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        color = RonovaPinkLight,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Version 1.0 Production Release",
                            fontSize = 12.sp,
                            color = RonovaPinkDark,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Specification Matrix matching User Reference (Screenshot 8)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(22.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RonovaCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Technical Specifications",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RonovaTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            SpecFeatureItem(
                                icon = Icons.Default.PhoneAndroid,
                                title = "Platform",
                                description = "Android Native • Kotlin"
                            )

                            SpecFeatureItem(
                                icon = Icons.Default.Bluetooth,
                                title = "Communication",
                                description = "Bluetooth Classic + BLE, custom multi-hop relay"
                            )

                            SpecFeatureItem(
                                icon = Icons.Default.WifiOff,
                                title = "Connectivity",
                                description = "No internet, SIM, mobile data, cloud server or paid API required"
                            )

                            SpecFeatureItem(
                                icon = Icons.Default.AccountBalanceWallet,
                                title = "Business model",
                                description = "Free to use • No ads • No subscription"
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(22.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RonovaCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Project Mission",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Ronova LTD is dedicated to providing free, sovereign communication tools that empower communities during network outages, emergency scenarios, or off-grid environments.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = RonovaTextSecondary,
                                    lineHeight = 20.sp,
                                    fontSize = 13.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Developer & Organization:",
                                fontSize = 11.5.sp,
                                color = RonovaTextMuted
                            )
                            Text(
                                text = "Ronova LTD",
                                fontWeight = FontWeight.Bold,
                                color = RonovaTextPrimary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Official Support Contact:",
                                fontSize = 11.5.sp,
                                color = RonovaTextMuted
                            )
                            Text(
                                text = "ronovaltd@gmail.com",
                                fontWeight = FontWeight.Bold,
                                color = RonovaPinkDark,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
