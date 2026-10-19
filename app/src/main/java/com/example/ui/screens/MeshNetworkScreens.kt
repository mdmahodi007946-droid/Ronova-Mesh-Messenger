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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.RonovaWarning
import com.example.viewmodel.RonovaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RelayNetworkScreen(
    viewModel: RonovaViewModel,
    onBack: () -> Unit,
    onNavigateToRelaySettings: () -> Unit
) {
    val connectedPeers by viewModel.connectedPeers.collectAsState()
    val allDevices by viewModel.allDevices.collectAsState()
    val isRelayOn by viewModel.isRelayOptedIn.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAFAFA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = "Relay Network Topology",
                subtitle = "Live Bluetooth Mesh Visualizer",
                onBack = onBack,
                actions = {
                    IconButton(onClick = onNavigateToRelaySettings) {
                        Icon(
                            imageVector = Icons.Default.Router,
                            contentDescription = "Relay Settings",
                            tint = RonovaPinkPrimary
                        )
                    }
                }
            )

            // Animated Mesh Radar Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Color.White)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)

                    // Draw concentric radar circles
                    drawCircle(
                        color = Color(0xFFFCE7F3),
                        radius = 40.dp.toPx(),
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xFFFDF2F8),
                        radius = 80.dp.toPx(),
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0xFFFFF7FB),
                        radius = 120.dp.toPx(),
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Draw animated pulse
                    drawCircle(
                        color = Color(0xFFEC4899).copy(alpha = (1f - (pulseRadius / 90f)).coerceIn(0f, 0.4f)),
                        radius = pulseRadius.dp.toPx(),
                        center = center
                    )

                    // Draw Center Node (Self)
                    drawCircle(
                        color = Color(0xFFEC4899),
                        radius = 12.dp.toPx(),
                        center = center
                    )

                    // Draw connected nodes around center
                    val peerCount = connectedPeers.size.coerceAtLeast(1)
                    val angleStep = (2 * Math.PI) / peerCount

                    connectedPeers.forEachIndexed { index, peer ->
                        val angle = index * angleStep
                        val dist = 75.dp.toPx()
                        val nodeOffset = Offset(
                            x = center.x + (dist * cos(angle)).toFloat(),
                            y = center.y + (dist * sin(angle)).toFloat()
                        )

                        // Draw connection link
                        drawLine(
                            color = if (peer.isConnected) Color(0xFF16A34A) else Color(0xFFD97706),
                            start = center,
                            end = nodeOffset,
                            strokeWidth = 2.dp.toPx()
                        )

                        // Draw peer circle
                        drawCircle(
                            color = if (peer.isConnected) Color(0xFF16A34A) else Color(0xFFD97706),
                            radius = 9.dp.toPx(),
                            center = nodeOffset
                        )
                    }
                }

                // Center Label
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "Center: This Device (${viewModel.myDeviceId})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RonovaPinkDark
                    )
                }
            }

            // Mesh Summary List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Active Mesh Routing Table",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (connectedPeers.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No mesh routes currently established.",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = RonovaTextMuted)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "When other Android devices running Ronova are within 10-30 meters, they will link as hops.",
                                    fontSize = 11.sp,
                                    color = RonovaTextMuted
                                )
                            }
                        }
                    }
                } else {
                    items(connectedPeers) { peer ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = peer.deviceName,
                                        fontWeight = FontWeight.Bold,
                                        color = RonovaTextPrimary
                                    )
                                    Text(
                                        text = "Address: ${peer.bluetoothAddress}",
                                        fontSize = 11.sp,
                                        color = RonovaTextMuted
                                    )
                                    Text(
                                        text = "Distance: 1 Hop (Direct RFCOMM)",
                                        fontSize = 11.sp,
                                        color = RonovaPinkDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (peer.isConnected) RonovaSuccess else RonovaWarning)
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
                title = "Relay Participation Settings",
                subtitle = "Store-and-forward mesh behavior",
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
                                        text = "Participate in Mesh Relay",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Allow your device to forward encrypted packets for nearby peers.",
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
                        Text("Save Relay Preferences", fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
        color = Color(0xFFFAFAFA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = "Application Diagnostics",
                subtitle = "RFCOMM telemetry & Security Audit",
                onBack = onBack,
                actions = {
                    IconButton(onClick = onNavigateToErrorRecovery) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Error Recovery",
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
                        text = "Radio & Protocol Telemetry",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "Packets Sent",
                            value = "${diag.packetsSent}",
                            icon = Icons.Default.Router,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Packets Rcvd",
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
                            title = "Relayed",
                            value = "${diag.packetsRelayed}",
                            icon = Icons.Default.Router,
                            accentColor = RonovaWarning,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Dropped / Exp",
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
        color = Color(0xFFFAFAFA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = "Error & Mesh Recovery",
                subtitle = "Self-healing radio and queue tools",
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
                                text = "Mesh Network Recovery Console",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "If connections drop or packets fail to relay due to Bluetooth hardware glitches, use these recovery actions.",
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
                    Text("Restart Bluetooth Server Sockets", fontWeight = FontWeight.Bold)
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
                    Text("Flush Corrupted Relay Queue", fontWeight = FontWeight.Bold)
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
                    Text("Regenerate EC Identity Keypair", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
