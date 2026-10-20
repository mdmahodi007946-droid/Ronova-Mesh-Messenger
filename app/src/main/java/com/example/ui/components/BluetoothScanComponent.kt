package com.example.ui.components

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.bluetooth.BluetoothStatus
import com.example.core.database.PeerEntity
import com.example.ui.theme.AlataFontFamily
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
import com.example.ui.theme.RonovaTextSecondary
import com.example.ui.theme.RonovaVioletAccent
import com.example.ui.theme.RonovaWarning
import com.example.viewmodel.RonovaViewModel

/**
 * Standard Android Bluetooth Discovery & Scan UI Component for Mesh Networking.
 * Implements standard discovery patterns:
 * - Scanning radar animation and progress states
 * - Paired/Bonded devices list vs Discovered/Available nearby devices
 * - RSSI signal strength meter (dBm + bar indicators)
 * - Device class classification (Phone, PC, Audio, Wearable, Generic Mesh Node)
 * - Connect/Mesh Link, Direct Chat, and Verification actions
 * - Filter by search query, paired status, or signal strength
 * - Runtime permission and Bluetooth enable handling
 */
@SuppressLint("MissingPermission")
@Composable
fun BluetoothScanComponent(
    viewModel: RonovaViewModel,
    modifier: Modifier = Modifier,
    onNavigateToChat: ((String) -> Unit)? = null,
    onNavigateToVerify: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val status by viewModel.bluetoothStatus.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val rssiMap by viewModel.deviceRssiMap.collectAsState()
    val connectedPeers by viewModel.connectedPeers.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(BluetoothFilter.ALL) }
    var pairedDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    var isPairedExpanded by remember { mutableStateOf(true) }

    // Refresh paired devices when status is READY or DISCOVERING
    LaunchedEffect(status) {
        pairedDevices = viewModel.getBondedDevices()
    }

    // Permission launcher for Bluetooth discovery on Android 12+ (API 31+) & legacy
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            viewModel.startDiscovery()
            pairedDevices = viewModel.getBondedDevices()
        }
    }

    val isScanning = status == BluetoothStatus.DISCOVERING

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bluetooth_scan_component")
    ) {
        // Scanner Header Card with Animated Radar
        BluetoothScanHeaderCard(
            status = status,
            isScanning = isScanning,
            deviceCount = discoveredDevices.size + pairedDevices.size,
            onStartScan = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    permissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.BLUETOOTH_SCAN,
                            android.Manifest.permission.BLUETOOTH_CONNECT
                        )
                    )
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.BLUETOOTH,
                            android.Manifest.permission.BLUETOOTH_ADMIN,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                }
            },
            onStopScan = { viewModel.stopDiscovery() },
            onClear = { viewModel.clearDiscoveredDevices() }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Bluetooth Disabled or Permission Notice
        if (status == BluetoothStatus.DISABLED) {
            BluetoothNoticeCard(
                icon = Icons.Default.BluetoothDisabled,
                title = "Bluetooth is turned off",
                description = "Turn on Bluetooth to scan and interconnect with neighboring Ronova mesh nodes.",
                buttonText = "Enable Bluetooth",
                accentColor = RonovaWarning,
                onClick = {
                    try {
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(enableBtIntent)
                    } catch (e: Exception) {
                        viewModel.checkBluetoothStatus()
                    }
                }
            )
            Spacer(modifier = Modifier.height(14.dp))
        } else if (status == BluetoothStatus.PERMISSION_REQUIRED) {
            BluetoothNoticeCard(
                icon = Icons.Default.Security,
                title = "Bluetooth Permissions Required",
                description = "Ronova mesh requires Nearby Devices (Bluetooth) permission to detect peers.",
                buttonText = "Grant Permission",
                accentColor = RonovaPinkPrimary,
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        permissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.BLUETOOTH_SCAN,
                                android.Manifest.permission.BLUETOOTH_CONNECT
                            )
                        )
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.BLUETOOTH,
                                android.Manifest.permission.BLUETOOTH_ADMIN,
                                android.Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Search & Filter Toolbar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, RonovaCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search device name or MAC address...",
                            fontSize = 13.5.sp,
                            color = RonovaTextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = RonovaPinkPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = RonovaTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bluetooth_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RonovaPinkPrimary,
                        unfocusedBorderColor = RonovaCardBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BluetoothFilterChip(
                        label = "All (${discoveredDevices.size + pairedDevices.size})",
                        selected = selectedFilter == BluetoothFilter.ALL,
                        onClick = { selectedFilter = BluetoothFilter.ALL }
                    )
                    BluetoothFilterChip(
                        label = "Paired (${pairedDevices.size})",
                        selected = selectedFilter == BluetoothFilter.PAIRED,
                        onClick = { selectedFilter = BluetoothFilter.PAIRED }
                    )
                    BluetoothFilterChip(
                        label = "Discovered (${discoveredDevices.size})",
                        selected = selectedFilter == BluetoothFilter.DISCOVERED,
                        onClick = { selectedFilter = BluetoothFilter.DISCOVERED }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Device Lists (Filtered)
        val filteredDiscovered = discoveredDevices.filter { dev ->
            val matchesQuery = searchQuery.isBlank() ||
                (dev.name ?: "").contains(searchQuery, ignoreCase = true) ||
                dev.address.contains(searchQuery, ignoreCase = true)
            matchesQuery && (selectedFilter == BluetoothFilter.ALL || selectedFilter == BluetoothFilter.DISCOVERED)
        }

        val filteredPaired = pairedDevices.filter { dev ->
            val matchesQuery = searchQuery.isBlank() ||
                (dev.name ?: "").contains(searchQuery, ignoreCase = true) ||
                dev.address.contains(searchQuery, ignoreCase = true)
            matchesQuery && (selectedFilter == BluetoothFilter.ALL || selectedFilter == BluetoothFilter.PAIRED)
        }

        if (filteredDiscovered.isEmpty() && filteredPaired.isEmpty()) {
            BluetoothEmptyScanState(
                isScanning = isScanning,
                onStartScan = { viewModel.startDiscovery() }
            )
        } else {
            // Paired Devices Section (Standard Android Bluetooth Discovery Pattern)
            if (filteredPaired.isNotEmpty() && (selectedFilter == BluetoothFilter.ALL || selectedFilter == BluetoothFilter.PAIRED)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, RonovaCardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isPairedExpanded = !isPairedExpanded }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(RonovaVioletAccent.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = RonovaVioletAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Paired Devices",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = RonovaTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = RonovaVioletAccent.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${filteredPaired.size}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RonovaVioletAccent,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isPairedExpanded) "Hide" else "Show",
                                fontSize = 12.sp,
                                color = RonovaPinkPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        AnimatedVisibility(visible = isPairedExpanded) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(top = 10.dp)
                            ) {
                                filteredPaired.forEach { device ->
                                    val isConnected = connectedPeers.any { it.bluetoothAddress == device.address }
                                    val rssi = rssiMap[device.address]
                                    BluetoothDeviceItemCard(
                                        device = device,
                                        rssi = rssi,
                                        isBonded = true,
                                        isConnected = isConnected,
                                        onConnect = { viewModel.connectDevice(device) },
                                        onChat = onNavigateToChat,
                                        onVerify = onNavigateToVerify
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Discovered / Available Devices Section
            if (filteredDiscovered.isNotEmpty() && (selectedFilter == BluetoothFilter.ALL || selectedFilter == BluetoothFilter.DISCOVERED)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, RonovaCardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(RonovaPinkPrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BluetoothSearching,
                                        contentDescription = null,
                                        tint = RonovaPinkPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Available Nearby Devices",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = RonovaTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = RonovaPinkPrimary.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${filteredDiscovered.size}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RonovaPinkPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            filteredDiscovered.forEach { device ->
                                val isConnected = connectedPeers.any { it.bluetoothAddress == device.address }
                                val rssi = rssiMap[device.address]
                                BluetoothDeviceItemCard(
                                    device = device,
                                    rssi = rssi,
                                    isBonded = false,
                                    isConnected = isConnected,
                                    onConnect = { viewModel.connectDevice(device) },
                                    onChat = onNavigateToChat,
                                    onVerify = onNavigateToVerify
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class BluetoothFilter {
    ALL,
    PAIRED,
    DISCOVERED
}

/**
 * Filter Chip with Alata styling
 */
@Composable
private fun BluetoothFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = RonovaPinkPrimary,
            selectedLabelColor = Color.White,
            containerColor = Color.White,
            labelColor = RonovaTextPrimary
        ),
        border = BorderStroke(
            1.dp,
            if (selected) RonovaPinkPrimary else RonovaCardBorder
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * Header Card displaying animated radar sweep, current radio state, and master actions
 */
@Composable
private fun BluetoothScanHeaderCard(
    status: BluetoothStatus,
    isScanning: Boolean,
    deviceCount: Int,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onClear: () -> Unit
) {
    // Pulse animation for radar ring
    val infiniteTransition = rememberInfiniteTransition(label = "bluetooth_radar_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radar_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radar_alpha"
    )
    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_rotate"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, RonovaCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Radar visual indicator
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isScanning) {
                        // Outer pulsating ring
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(RonovaPinkPrimary.copy(alpha = pulseAlpha))
                        )
                        // Second subtle pulse
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .scale(pulseScale * 0.9f)
                                .clip(CircleShape)
                                .border(BorderStroke(1.5.dp, RonovaPinkPrimary.copy(alpha = 0.4f)), CircleShape)
                        )
                    }

                    // Inner circle with icon
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = if (isScanning)
                                        listOf(RonovaPinkPrimary, RonovaPinkDark)
                                    else
                                        listOf(RonovaPinkLight, RonovaPinkContainer)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isScanning) Icons.Default.BluetoothSearching else Icons.Default.Bluetooth,
                            contentDescription = null,
                            tint = if (isScanning) Color.White else RonovaPinkPrimary,
                            modifier = Modifier
                                .size(24.dp)
                                .then(if (isScanning) Modifier.rotate(rotateAngle * 0.1f) else Modifier)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Title & Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isScanning) "Scanning Airwaves..." else "Bluetooth Mesh Radio",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = RonovaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isScanning)
                            "Broadcasting Ronova UUID & listening for peer nodes"
                        else
                            "$deviceCount nodes discovered in wireless range",
                        fontSize = 12.5.sp,
                        color = RonovaTextSecondary,
                        lineHeight = 17.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Scan / Stop Action Button
                if (isScanning) {
                    Button(
                        onClick = onStopScan,
                        colors = ButtonDefaults.buttonColors(containerColor = RonovaError),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("stop_scan_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Stop",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Button(
                        onClick = onStartScan,
                        colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("start_scan_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Scan",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Scan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Indeterminate progress bar when scanning
            if (isScanning) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = RonovaPinkPrimary,
                    trackColor = RonovaPinkLight
                )
            }
        }
    }
}

/**
 * Notice card for Bluetooth Disabled or Permissions Missing
 */
@Composable
private fun BluetoothNoticeCard(
    icon: ImageVector,
    title: String,
    description: String,
    buttonText: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.06f)),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = RonovaTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = RonovaTextSecondary,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = buttonText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Discovered / Paired Bluetooth Device Item Card
 */
@SuppressLint("MissingPermission")
@Composable
fun BluetoothDeviceItemCard(
    device: BluetoothDevice,
    rssi: Int?,
    isBonded: Boolean,
    isConnected: Boolean,
    onConnect: () -> Unit,
    onChat: ((String) -> Unit)?,
    onVerify: ((String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val deviceName = try {
        device.name?.ifBlank { null } ?: "Unnamed Mesh Node"
    } catch (e: Exception) {
        "Unnamed Mesh Node"
    }

    val deviceClassIcon = getDeviceClassIcon(device)
    val deviceCategoryLabel = getDeviceCategoryLabel(device)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("device_card_${device.address}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (isConnected) RonovaSuccess.copy(alpha = 0.5f) else RonovaCardBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Device Icon with badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isConnected) RonovaSuccess.copy(alpha = 0.12f)
                            else if (isBonded) RonovaVioletAccent.copy(alpha = 0.1f)
                            else RonovaPinkLight.copy(alpha = 0.6f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = deviceClassIcon,
                        contentDescription = null,
                        tint = if (isConnected) RonovaSuccess
                        else if (isBonded) RonovaVioletAccent
                        else RonovaPinkPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Device Name & Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = deviceName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = RonovaTextPrimary,
                            maxLines = 1
                        )
                        if (isBonded) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = RonovaVioletAccent.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "PAIRED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RonovaVioletAccent,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = device.address,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = RonovaTextMuted
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• $deviceCategoryLabel",
                            fontSize = 11.sp,
                            color = RonovaTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // RSSI Signal Strength Indicator (dBm + 4 bars)
                RssiSignalStrengthIndicator(rssi = rssi)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isConnected) {
                    Surface(
                        color = RonovaSuccess.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.BluetoothConnected,
                                contentDescription = null,
                                tint = RonovaSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mesh Linked",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = RonovaSuccess
                            )
                        }
                    }

                    if (onChat != null) {
                        Button(
                            onClick = { onChat(device.address) },
                            colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("chat_device_${device.address}")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Chat",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (onVerify != null) {
                        OutlinedButton(
                            onClick = { onVerify(device.address) },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, RonovaCardBorder),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("verify_device_${device.address}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = RonovaPinkPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Verify",
                                fontSize = 12.sp,
                                color = RonovaPinkPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onConnect,
                        colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("connect_device_${device.address}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBonded) "Link Mesh Peer" else "Connect to Mesh",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * RSSI Signal Strength Visualizer (Standard Android Bluetooth Discovery Pattern)
 * Calculates signal level from dBm:
 * > -60 dBm: 4 bars (Excellent)
 * -60 to -70 dBm: 3 bars (Good)
 * -70 to -85 dBm: 2 bars (Fair)
 * < -85 dBm: 1 bar (Weak)
 */
@Composable
fun RssiSignalStrengthIndicator(
    rssi: Int?,
    modifier: Modifier = Modifier
) {
    val bars = when {
        rssi == null || rssi == 0 -> 2 // Default preview
        rssi >= -60 -> 4
        rssi >= -70 -> 3
        rssi >= -85 -> 2
        else -> 1
    }

    val barColor = when (bars) {
        4 -> RonovaSuccess
        3 -> Color(0xFF22C55E)
        2 -> RonovaWarning
        else -> RonovaError
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val heights = listOf(4.dp, 7.dp, 10.dp, 14.dp)
            heights.forEachIndexed { index, height ->
                val isActive = index < bars
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(height)
                        .clip(RoundedCornerShape(1.dp))
                        .background(if (isActive) barColor else Color(0xFFE2E8F0))
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = if (rssi != null && rssi != 0) "$rssi dBm" else "Nearby",
            fontSize = 10.sp,
            color = if (rssi != null && rssi != 0) barColor else RonovaTextMuted,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Standard device class icon resolver
 */
@SuppressLint("MissingPermission")
private fun getDeviceClassIcon(device: BluetoothDevice): ImageVector {
    val majorClass = try {
        device.bluetoothClass?.majorDeviceClass
    } catch (e: Exception) {
        null
    }

    return when (majorClass) {
        BluetoothClass.Device.Major.PHONE -> Icons.Default.PhoneAndroid
        BluetoothClass.Device.Major.COMPUTER -> Icons.Default.Computer
        BluetoothClass.Device.Major.AUDIO_VIDEO -> Icons.Default.Headphones
        BluetoothClass.Device.Major.WEARABLE -> Icons.Default.Watch
        BluetoothClass.Device.Major.NETWORKING -> Icons.Default.Hub
        else -> Icons.Default.Bluetooth
    }
}

/**
 * Standard device class label resolver
 */
@SuppressLint("MissingPermission")
private fun getDeviceCategoryLabel(device: BluetoothDevice): String {
    val majorClass = try {
        device.bluetoothClass?.majorDeviceClass
    } catch (e: Exception) {
        null
    }

    return when (majorClass) {
        BluetoothClass.Device.Major.PHONE -> "Smartphone / Tablet"
        BluetoothClass.Device.Major.COMPUTER -> "PC / Computer"
        BluetoothClass.Device.Major.AUDIO_VIDEO -> "Audio / Headset"
        BluetoothClass.Device.Major.WEARABLE -> "Smart Watch"
        BluetoothClass.Device.Major.NETWORKING -> "Mesh Node"
        else -> "Bluetooth Node"
    }
}

/**
 * Empty scan state when no devices have been discovered yet
 */
@Composable
private fun BluetoothEmptyScanState(
    isScanning: Boolean,
    onStartScan: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(RonovaPinkLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isScanning) Icons.Default.BluetoothSearching else Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = RonovaPinkPrimary,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isScanning) "Listening for mesh broadcasts..." else "No Bluetooth devices found",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = RonovaTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isScanning)
                    "Scanning RFCOMM and BLE radio channels for nearby Ronova nodes. Hold devices within 10-30 meters."
                else
                    "Tap scan to search the 2.4 GHz airwaves for nearby peer devices to form a mesh link.",
                fontSize = 13.sp,
                color = RonovaTextSecondary,
                lineHeight = 18.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            if (!isScanning) {
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onStartScan,
                    colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("empty_state_scan_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start Scanning Now",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }
            }
        }
    }
}
