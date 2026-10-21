package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.RonovaTopBar
import com.example.ui.components.StatCard
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
import com.example.ui.theme.RonovaWarning
import com.example.viewmodel.RonovaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

data class RadarNode(
    val deviceId: String,
    val displayName: String,
    val bluetoothAddress: String,
    val isConnected: Boolean,
    val isDirectPeer: Boolean,
    val hopDistance: Int,
    val isProfileLocked: Boolean,
    val isInternetReachable: Boolean,
    val rssi: Int
)

@Composable
fun RelayNetworkScreen(
    viewModel: RonovaViewModel,
    onBack: () -> Unit,
    onNavigateToRelaySettings: () -> Unit,
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToVoiceCall: (String) -> Unit = {},
    onNavigateToVerify: (String) -> Unit = {}
) {
    val connectedPeers by viewModel.connectedPeers.collectAsState()
    val allDevices by viewModel.allDevices.collectAsState()
    val customNickname by viewModel.customNickname.collectAsState()

    var selectedNode by remember { mutableStateOf<RadarNode?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )

    // Build real discovered radar nodes
    val radarNodes = remember(connectedPeers, allDevices, viewModel.myDeviceId) {
        val list = mutableListOf<RadarNode>()
        val seen = mutableSetOf<String>()

        connectedPeers.forEach { p ->
            if (p.deviceId != viewModel.myDeviceId && p.deviceId.isNotBlank() && p.deviceId !in seen) {
                seen.add(p.deviceId)
                val dev = allDevices.firstOrNull { it.deviceId == p.deviceId }
                list.add(
                    RadarNode(
                        deviceId = p.deviceId,
                        displayName = p.deviceName.ifBlank { dev?.displayName ?: p.deviceId },
                        bluetoothAddress = p.bluetoothAddress,
                        isConnected = p.isConnected,
                        isDirectPeer = true,
                        hopDistance = 1,
                        isProfileLocked = p.isProfileLocked || (dev?.isProfileLocked == true),
                        isInternetReachable = dev?.isInternetReachable == true,
                        rssi = p.rssi
                    )
                )
            }
        }

        allDevices.forEach { d ->
            if (d.deviceId != viewModel.myDeviceId && d.deviceId.isNotBlank() && d.deviceId !in seen) {
                seen.add(d.deviceId)
                list.add(
                    RadarNode(
                        deviceId = d.deviceId,
                        displayName = d.displayName.ifBlank { d.deviceId },
                        bluetoothAddress = d.bluetoothAddress,
                        isConnected = false,
                        isDirectPeer = d.isDirectPeer,
                        hopDistance = d.hopDistance,
                        isProfileLocked = d.isProfileLocked,
                        isInternetReachable = d.isInternetReachable,
                        rssi = 0
                    )
                )
            }
        }
        list
    }

    if (selectedNode != null) {
        val node = selectedNode!!
        val approxDist = when {
            node.isDirectPeer -> {
                if (node.rssi != 0) {
                    when {
                        node.rssi > -60 -> "~1-3m (Close)"
                        node.rssi in -75..-60 -> "~3-10m (Medium range)"
                        else -> "~10-30m (Far range)"
                    }
                } else "~3-15m (Direct RFCOMM)"
            }
            node.hopDistance > 1 -> "Indirect (${node.hopDistance} hops via mesh)"
            node.isInternetReachable -> "Remote (Mobile Internet)"
            else -> "Standby"
        }

        AlertDialog(
            onDismissRequest = { selectedNode = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = node.displayName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (node.isProfileLocked) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = stringResource(R.string.radar_peer_locked),
                            tint = RonovaWarning,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ID: ${node.deviceId}",
                        fontSize = 11.5.sp,
                        color = RonovaTextMuted
                    )
                    Text(
                        text = "Route: " + when {
                            node.isDirectPeer -> stringResource(R.string.radar_peer_direct)
                            node.hopDistance > 1 -> stringResource(R.string.radar_peer_relay, node.hopDistance)
                            node.isInternetReachable -> stringResource(R.string.radar_peer_internet)
                            else -> "Offline"
                        },
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RonovaTextPrimary
                    )
                    Text(
                        text = stringResource(R.string.radar_peer_distance_approx, approxDist),
                        fontSize = 11.5.sp,
                        color = RonovaPinkDark
                    )
                    Text(
                        text = stringResource(R.string.radar_disclaimer),
                        fontSize = 10.5.sp,
                        color = RonovaTextMuted,
                        lineHeight = 14.sp
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            val id = node.deviceId
                            selectedNode = null
                            onNavigateToChat(id)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.radar_action_chat), fontSize = 12.sp)
                    }
                    Button(
                        onClick = {
                            val id = node.deviceId
                            selectedNode = null
                            onNavigateToVoiceCall(id)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.radar_action_call), fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        val id = node.deviceId
                        selectedNode = null
                        onNavigateToVerify(id)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.radar_action_profile), fontSize = 12.sp)
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = stringResource(R.string.radar_title),
                subtitle = stringResource(R.string.radar_subtitle),
                onBack = onBack,
                actions = {
                    IconButton(onClick = onNavigateToRelaySettings) {
                        Icon(
                            imageVector = Icons.Default.Router,
                            contentDescription = stringResource(R.string.relay_settings_title),
                            tint = RonovaPinkPrimary
                        )
                    }
                }
            )

            // Non-GPS Disclaimer Strip
            Surface(
                color = RonovaPinkLight.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.radar_disclaimer),
                    fontSize = 11.sp,
                    color = RonovaTextSecondary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    lineHeight = 15.sp
                )
            }

            // Radar Visualizer Canvas & Legend
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RonovaCardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Legend Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF16A34A)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.radar_legend_direct), fontSize = 10.sp, color = RonovaTextMuted)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFD97706)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.radar_legend_relay), fontSize = 10.sp, color = RonovaTextMuted)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2563EB)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.radar_legend_internet), fontSize = 10.sp, color = RonovaTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Radar Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2, size.height / 2)

                            // Range rings
                            drawCircle(
                                color = RonovaCardBorder,
                                radius = 38.dp.toPx(),
                                center = center,
                                style = Stroke(width = 1.2.dp.toPx())
                            )
                            drawCircle(
                                color = RonovaCardBorder.copy(alpha = 0.7f),
                                radius = 76.dp.toPx(),
                                center = center,
                                style = Stroke(width = 1.2.dp.toPx())
                            )
                            drawCircle(
                                color = RonovaCardBorder.copy(alpha = 0.4f),
                                radius = 114.dp.toPx(),
                                center = center,
                                style = Stroke(width = 1.2.dp.toPx())
                            )

                            // Animated Pulse
                            drawCircle(
                                color = RonovaPinkPrimary.copy(alpha = (1f - (pulseRadius / 90f)).coerceIn(0f, 0.4f)),
                                radius = pulseRadius.dp.toPx(),
                                center = center
                            )

                            // Self center node
                            drawCircle(
                                color = RonovaPinkDark,
                                radius = 10.dp.toPx(),
                                center = center
                            )

                            // Discovered radar nodes
                            val nodeCount = radarNodes.size.coerceAtLeast(1)
                            val angleStep = (2 * Math.PI) / nodeCount

                            radarNodes.forEachIndexed { index, node ->
                                val angle = index * angleStep
                                val ringDist = when {
                                    node.isDirectPeer -> 55.dp.toPx()
                                    node.hopDistance > 1 -> 85.dp.toPx()
                                    else -> 110.dp.toPx()
                                }
                                val nodeOffset = Offset(
                                    x = center.x + (ringDist * cos(angle)).toFloat(),
                                    y = center.y + (ringDist * sin(angle)).toFloat()
                                )

                                val nodeColor = when {
                                    node.isDirectPeer -> Color(0xFF16A34A)
                                    node.hopDistance > 1 -> Color(0xFFD97706)
                                    else -> Color(0xFF2563EB)
                                }

                                drawLine(
                                    color = nodeColor.copy(alpha = 0.6f),
                                    start = center,
                                    end = nodeOffset,
                                    strokeWidth = 1.5.dp.toPx()
                                )

                                drawCircle(
                                    color = nodeColor,
                                    radius = 8.dp.toPx(),
                                    center = nodeOffset
                                )

                                if (node.isProfileLocked) {
                                    drawCircle(
                                        color = Color.White,
                                        radius = 3.dp.toPx(),
                                        center = nodeOffset
                                    )
                                }
                            }
                        }

                        // Center self label
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.radar_center_label,
                                    customNickname.ifBlank { viewModel.myDeviceId }
                                ),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = RonovaPinkDark
                            )
                        }
                    }
                }
            }

            // Discovered Node List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.drawer_connected_peers),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (radarNodes.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RonovaCardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.radar_no_peers),
                                    style = MaterialTheme.typography.bodyMedium.copy(color = RonovaTextMuted)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Nearby Android devices running Ronova Mesh will automatically appear on the radar.",
                                    fontSize = 11.sp,
                                    color = RonovaTextMuted,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                } else {
                    items(radarNodes) { node ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedNode = node },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RonovaCardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    node.isDirectPeer -> Color(0xFF16A34A)
                                                    node.hopDistance > 1 -> Color(0xFFD97706)
                                                    else -> Color(0xFF2563EB)
                                                }
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = node.displayName,
                                                fontWeight = FontWeight.Bold,
                                                color = RonovaTextPrimary
                                            )
                                            if (node.isProfileLocked) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Shield,
                                                    contentDescription = stringResource(R.string.radar_peer_locked),
                                                    tint = RonovaWarning,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = when {
                                                node.isDirectPeer -> stringResource(R.string.radar_peer_direct)
                                                node.hopDistance > 1 -> stringResource(R.string.radar_peer_relay, node.hopDistance)
                                                node.isInternetReachable -> stringResource(R.string.radar_peer_internet)
                                                else -> "Offline"
                                            },
                                            fontSize = 11.sp,
                                            color = RonovaPinkDark,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "ID: ${node.deviceId}",
                                            fontSize = 10.sp,
                                            color = RonovaTextMuted
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { onNavigateToChat(node.deviceId) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Chat,
                                            contentDescription = stringResource(R.string.radar_action_chat),
                                            tint = RonovaPinkPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onNavigateToVoiceCall(node.deviceId) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = stringResource(R.string.radar_action_call),
                                            tint = RonovaPinkDark,
                                            modifier = Modifier.size(18.dp)
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
fun RelaySettingsScreen(
    viewModel: RonovaViewModel,
    onBack: () -> Unit
) {
    val isRelayOptedIn by viewModel.isRelayOptedIn.collectAsState()
    val maxHops by viewModel.maxHops.collectAsState()
    var batteryThreshold by remember { mutableFloatStateOf(15f) }
    var queueSizeLimit by remember { mutableFloatStateOf(50f) }

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
                title = stringResource(R.string.relay_settings_title),
                subtitle = stringResource(R.string.relay_settings_subtitle),
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.relay_participate_toggle_title),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.relay_participate_toggle_desc),
                                        style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted)
                                    )
                                }
                                Switch(
                                    checked = isRelayOptedIn,
                                    onCheckedChange = { viewModel.setRelayOptIn(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = RonovaPinkPrimary
                                    ),
                                    modifier = Modifier.testTag("relay_opt_in_switch")
                                )
                            }
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Maximum Hop Limit",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Surface(
                                    color = RonovaPinkLight,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "$maxHops Hops",
                                        fontWeight = FontWeight.Bold,
                                        color = RonovaPinkDark,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Packets exceeding this hop count will be dropped to prevent network saturation.",
                                style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = maxHops.toFloat(),
                                onValueChange = { viewModel.setMaxHops(it.toInt()) },
                                valueRange = 1f..10f,
                                steps = 8,
                                colors = SliderDefaults.colors(
                                    thumbColor = RonovaPinkPrimary,
                                    activeTrackColor = RonovaPinkPrimary,
                                    inactiveTrackColor = RonovaPinkContainer,
                                    activeTickColor = Color.Transparent,
                                    inactiveTickColor = Color.Transparent
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Battery Threshold",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Surface(
                                    color = RonovaPinkLight,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "${batteryThreshold.toInt()}%",
                                        fontWeight = FontWeight.Bold,
                                        color = RonovaPinkDark,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pause packet forwarding when device battery drops below this percentage.",
                                style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = batteryThreshold,
                                onValueChange = { batteryThreshold = it },
                                valueRange = 5f..30f,
                                steps = 4,
                                colors = SliderDefaults.colors(
                                    thumbColor = RonovaPinkPrimary,
                                    activeTrackColor = RonovaPinkPrimary,
                                    inactiveTrackColor = RonovaPinkContainer,
                                    activeTickColor = Color.Transparent,
                                    inactiveTickColor = Color.Transparent
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Store & Forward Capacity",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Surface(
                                    color = RonovaPinkLight,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "${queueSizeLimit.toInt()} Packets",
                                        fontWeight = FontWeight.Bold,
                                        color = RonovaPinkDark,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Max encrypted packets stored locally while awaiting next-hop routes.",
                                style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = queueSizeLimit,
                                onValueChange = { queueSizeLimit = it },
                                valueRange = 10f..200f,
                                steps = 18,
                                colors = SliderDefaults.colors(
                                    thumbColor = RonovaPinkPrimary,
                                    activeTrackColor = RonovaPinkPrimary,
                                    inactiveTrackColor = RonovaPinkContainer,
                                    activeTickColor = Color.Transparent,
                                    inactiveTickColor = Color.Transparent
                                )
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(stringResource(R.string.relay_save_preferences), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun DiagnosticsScreen(
    viewModel: RonovaViewModel,
    onBack: () -> Unit,
    onNavigateToErrorRecovery: () -> Unit
) {
    val diag by viewModel.diagnostics.collectAsState()
    val securityEvents by viewModel.securityEvents.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = stringResource(R.string.diagnostics_title),
                subtitle = stringResource(R.string.diagnostics_subtitle),
                onBack = onBack,
                actions = {
                    IconButton(onClick = onNavigateToErrorRecovery) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = stringResource(R.string.recovery_title),
                            tint = RonovaWarning
                        )
                    }
                }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.diag_radio_status),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = stringResource(R.string.diag_packets_sent),
                            value = "${diag.packetsSent}",
                            icon = Icons.Default.Router,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = stringResource(R.string.diag_packets_rcvd),
                            value = "${diag.packetsReceived}",
                            icon = Icons.Default.Router,
                            accentColor = RonovaSuccess,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = stringResource(R.string.diag_relayed),
                            value = "${diag.packetsRelayed}",
                            icon = Icons.Default.Router,
                            accentColor = RonovaWarning,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = stringResource(R.string.diag_dropped),
                            value = "${diag.packetsDropped}",
                            icon = Icons.Default.Router,
                            accentColor = RonovaError,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Active Sockets: ${diag.activeConnections}",
                                fontWeight = FontWeight.Bold,
                                color = RonovaTextPrimary
                            )
                            if (diag.lastError != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Last Error: ${diag.lastError}",
                                    fontSize = 11.sp,
                                    color = RonovaError
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Security & Audit Event Log (${securityEvents.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        if (securityEvents.isNotEmpty()) {
                            Text(
                                text = "Clear",
                                color = RonovaPinkPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { viewModel.clearSecurityEvents() }
                            )
                        }
                    }
                }

                if (securityEvents.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "No security events recorded.",
                                style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(securityEvents) { event ->
                        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            .format(Date(event.timestamp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = event.eventType,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = RonovaPinkDark
                                    )
                                    Text(text = timeStr, fontSize = 10.sp, color = RonovaTextMuted)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = event.description,
                                    fontSize = 11.sp,
                                    color = RonovaTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorRecoveryScreen(
    viewModel: RonovaViewModel,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = stringResource(R.string.recovery_title),
                subtitle = stringResource(R.string.recovery_subtitle),
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = RonovaWarning,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.recovery_title),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.recovery_reset_radio_desc),
                            style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted)
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.disconnectAll()
                        viewModel.meshManager.startServer()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.recovery_restart_sockets), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        viewModel.clearRelayQueue()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RonovaWarning),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.recovery_flush_queue_btn), fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        viewModel.resetCryptographicIdentity()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RonovaError),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.recovery_regen_keys), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
