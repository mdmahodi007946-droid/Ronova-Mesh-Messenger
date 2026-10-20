package com.example.ui.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.bluetooth.BluetoothStatus
import com.example.ui.components.BluetoothScanComponent
import com.example.ui.components.RonovaTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.RonovaError
import com.example.ui.theme.RonovaPinkContainer
import com.example.ui.theme.RonovaPinkDark
import com.example.ui.theme.RonovaPinkLight
import com.example.ui.theme.RonovaPinkPrimary
import com.example.ui.theme.RonovaSuccess
import com.example.ui.theme.RonovaTextMuted
import com.example.ui.theme.RonovaTextPrimary
import com.example.viewmodel.RonovaViewModel

@SuppressLint("MissingPermission")
@Composable
fun DiscoveryScreen(
    viewModel: RonovaViewModel,
    onBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToVerify: (String) -> Unit
) {
    val status by viewModel.bluetoothStatus.collectAsState()

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
                title = stringResource(R.string.disc_title),
                subtitle = stringResource(R.string.disc_subtitle),
                onBack = onBack,
                actions = {
                    if (status == BluetoothStatus.DISCOVERING) {
                        IconButton(
                            onClick = { viewModel.stopDiscovery() },
                            modifier = Modifier.testTag("stop_discovery_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop Scanning",
                                tint = RonovaError
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { viewModel.startDiscovery() },
                            modifier = Modifier.testTag("start_discovery_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Start Scanning",
                                tint = RonovaPinkPrimary
                            )
                        }
                    }
                }
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                item {
                    BluetoothScanComponent(
                        viewModel = viewModel,
                        onNavigateToChat = onNavigateToChat,
                        onNavigateToVerify = onNavigateToVerify
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DiscoveredDeviceRow(
    device: BluetoothDevice,
    onConnect: () -> Unit,
    onVerify: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(RonovaPinkLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = RonovaPinkPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = device.name ?: "Unknown Bluetooth Device",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = RonovaTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = device.address,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = RonovaTextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Row {
                Button(
                    onClick = onConnect,
                    colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("connect_device_${device.address}")
                ) {
                    Text(stringResource(R.string.action_connect), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun VerifyDeviceScreen(
    deviceId: String,
    viewModel: RonovaViewModel,
    onBack: () -> Unit,
    onStartChat: (String) -> Unit,
    onVoiceCall: (String) -> Unit
) {
    val device by viewModel.repository.getDeviceByIdFlow(deviceId).collectAsState(initial = null)
    var editName by remember(device?.displayName) { mutableStateOf(device?.displayName ?: deviceId) }
    var isEditing by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAFAFA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = stringResource(R.string.verify_title),
                subtitle = stringResource(R.string.verify_subtitle),
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(RonovaPinkLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = RonovaPinkPrimary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    if (isEditing) {
                                        OutlinedTextField(
                                            value = editName,
                                            onValueChange = { editName = it },
                                            modifier = Modifier.width(160.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = RonovaPinkPrimary
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = device?.displayName ?: deviceId,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = RonovaTextPrimary
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Status: ${device?.trustStatus ?: "UNVERIFIED"}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (device?.trustStatus == "TRUSTED") RonovaSuccess else RonovaPinkDark,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    if (isEditing) {
                                        viewModel.updateDeviceDisplayName(deviceId, editName)
                                        isEditing = false
                                    } else {
                                        isEditing = true
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isEditing) Icons.Default.Done else Icons.Default.Edit,
                                    contentDescription = "Edit Contact Name",
                                    tint = RonovaPinkPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Cryptographic Fingerprint (Public ID):",
                            style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = deviceId,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = RonovaTextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Bluetooth Physical Address:",
                            style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = device?.bluetoothAddress?.ifEmpty { "Not bound" } ?: "Direct Mesh Peer",
                            style = MaterialTheme.typography.bodyMedium.copy(color = RonovaTextPrimary)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Public Key Hash:",
                            style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = device?.publicKeyBase64?.take(32)?.plus("...") ?: "Pending beacon exchange",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = RonovaTextMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Trust verification actions
                Text(
                    text = "Trust Management",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = RonovaTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.setDeviceTrust(deviceId, "TRUSTED") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (device?.trustStatus == "TRUSTED") RonovaSuccess else Color(0xFFE5E7EB)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (device?.trustStatus == "TRUSTED") Color.White else Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mark Trusted",
                            color = if (device?.trustStatus == "TRUSTED") Color.White else Color.Black,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.setDeviceTrust(deviceId, "BLOCKED") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (device?.trustStatus == "BLOCKED") RonovaError else Color(0xFFE5E7EB)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = if (device?.trustStatus == "BLOCKED") Color.White else Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Block Device",
                            color = if (device?.trustStatus == "BLOCKED") Color.White else Color.Black,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Communication triggers
                Button(
                    onClick = { onStartChat(deviceId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("verify_start_chat_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_open_encrypted_chat), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { onVoiceCall(deviceId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("verify_start_call_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = RonovaPinkPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Start Offline Voice Call",
                        color = RonovaPinkPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
