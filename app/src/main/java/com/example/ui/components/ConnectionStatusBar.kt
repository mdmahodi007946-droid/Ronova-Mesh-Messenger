package com.example.ui.components

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.bluetooth.BluetoothStatus
import com.example.ui.theme.RonovaCardBorder
import com.example.ui.theme.RonovaError
import com.example.ui.theme.RonovaPinkContainer
import com.example.ui.theme.RonovaPinkLight
import com.example.ui.theme.RonovaPinkPrimary
import com.example.ui.theme.RonovaSuccess
import com.example.ui.theme.RonovaTextMuted
import com.example.ui.theme.RonovaTextPrimary
import com.example.ui.theme.RonovaTextSecondary
import com.example.ui.theme.RonovaVioletAccent
import com.example.ui.theme.RonovaWarning
import com.example.viewmodel.RonovaViewModel

/**
 * Top Connection Status Bar showing real-time mesh connectivity and discoverability.
 *
 * States:
 * - CONNECTED: Active peer link(s) on the mesh network.
 * - DISCOVERABLE: RFCOMM mesh server active, broadcasting & listening for neighbor nodes.
 * - DISCOVERING: Actively scanning nearby 2.4GHz radio channels.
 * - DISABLED / OFFLINE: Bluetooth disabled or permissions required.
 *
 * Tapping the bar toggles a quick-status panel for instant controls (Scan, Discoverable toggle, Peer info).
 */
@SuppressLint("MissingPermission")
@Composable
fun ConnectionStatusBar(
    viewModel: RonovaViewModel,
    modifier: Modifier = Modifier,
    onNavigateToDiscovery: (() -> Unit)? = null,
    onNavigateToRelayNetwork: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val status by viewModel.bluetoothStatus.collectAsState()
    val connectedPeers by viewModel.connectedPeers.collectAsState()
    val isDiscoverable by viewModel.isDiscoverable.collectAsState()
    val diagnostics by viewModel.diagnostics.collectAsState()

    var isExpanded by remember { mutableStateOf(false) }

    val hasActiveConnections = connectedPeers.isNotEmpty() || status == BluetoothStatus.CONNECTED || diagnostics.activeConnections > 0
    val isScanning = status == BluetoothStatus.DISCOVERING
    val isBtDisabled = status == BluetoothStatus.DISABLED
    val isPermissionRequired = status == BluetoothStatus.PERMISSION_REQUIRED

    // Determine state configuration
    val (statusTitle, statusSubtitle, statusColor, statusIcon) = when {
        isBtDisabled -> Quadruple(
            stringResource(R.string.csb_mesh_offline),
            stringResource(R.string.csb_bt_off),
            RonovaError,
            Icons.Default.BluetoothDisabled
        )
        isPermissionRequired -> Quadruple(
            stringResource(R.string.csb_perms_needed),
            stringResource(R.string.csb_perms_desc),
            RonovaWarning,
            Icons.Default.Warning
        )
        hasActiveConnections && isDiscoverable -> Quadruple(
            stringResource(R.string.csb_mesh_connected_disc),
            stringResource(R.string.csb_peers_active, connectedPeers.size.coerceAtLeast(diagnostics.activeConnections)),
            RonovaSuccess,
            Icons.Default.Hub
        )
        hasActiveConnections -> Quadruple(
            stringResource(R.string.csb_mesh_connected),
            stringResource(R.string.csb_peers_linked, connectedPeers.size.coerceAtLeast(diagnostics.activeConnections)),
            RonovaSuccess,
            Icons.Default.BluetoothConnected
        )
        isScanning -> Quadruple(
            stringResource(R.string.csb_scanning),
            stringResource(R.string.csb_scanning_desc),
            RonovaPinkPrimary,
            Icons.Default.BluetoothSearching
        )
        isDiscoverable -> Quadruple(
            stringResource(R.string.csb_discoverable),
            stringResource(R.string.csb_discoverable_desc),
            RonovaVioletAccent,
            Icons.Default.Sensors
        )
        else -> Quadruple(
            stringResource(R.string.csb_mesh_standby),
            stringResource(R.string.csb_ready_connect),
            RonovaTextSecondary,
            Icons.Default.Bluetooth
        )
    }

    // Beacon pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "status_beacon")
    val beaconScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beacon_scale"
    )
    val beaconAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beacon_alpha"
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, RonovaCardBorder.copy(alpha = 0.7f)),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .testTag("connection_status_bar")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Top Bar Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Beacon Dot + Title & Subtitle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier.size(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hasActiveConnections || isDiscoverable || isScanning) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .scale(beaconScale)
                                    .clip(CircleShape)
                                    .background(statusColor.copy(alpha = beaconAlpha))
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(8.5.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                    }

                    Spacer(modifier = Modifier.width(9.dp))

                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(15.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = statusTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = RonovaTextPrimary,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "• $statusSubtitle",
                        fontSize = 11.5.sp,
                        color = RonovaTextSecondary,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right: Compact Quick Chips + Expand Chevron
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasActiveConnections) {
                        Surface(
                            color = RonovaSuccess.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.csb_badge_online),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RonovaSuccess
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    } else if (isDiscoverable) {
                        Surface(
                            color = RonovaVioletAccent.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.csb_badge_discoverable),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = RonovaVioletAccent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand details",
                        tint = RonovaTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Expandable Details Drawer
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .background(Color(0xFFFAFCFE))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    HorizontalDivider(
                        color = RonovaCardBorder.copy(alpha = 0.5f),
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Node Identity Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LOCAL MESH NODE",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = RonovaTextMuted,
                                letterSpacing = 0.6.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "ID: ${viewModel.myDeviceId.take(12)}...",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RonovaTextPrimary
                            )
                        }

                        // Discoverable Switch Toggle
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isDiscoverable) stringResource(R.string.csb_discoverable_on) else stringResource(R.string.csb_discoverable_off),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDiscoverable) RonovaVioletAccent else RonovaTextMuted
                                )
                                Text(
                                    text = if (isDiscoverable) stringResource(R.string.discoverable_visible) else stringResource(R.string.discoverable_invisible),
                                    fontSize = 10.sp,
                                    color = RonovaTextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = isDiscoverable,
                                onCheckedChange = { checked ->
                                    viewModel.setDiscoverable(checked)
                                    if (checked) {
                                        try {
                                            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                                                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(discoverableIntent)
                                        } catch (e: Exception) {
                                            viewModel.checkDiscoverability()
                                        }
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = RonovaVioletAccent,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFCBD5E1)
                                ),
                                modifier = Modifier.scale(0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stats Grid Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Connected Peers Stat
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, RonovaCardBorder),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = stringResource(R.string.drawer_connected_peers),
                                    fontSize = 11.sp,
                                    color = RonovaTextSecondary
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Hub,
                                        contentDescription = null,
                                        tint = RonovaSuccess,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "${connectedPeers.size}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = RonovaTextPrimary
                                    )
                                }
                            }
                        }

                        // Discoverable Status Stat
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, RonovaCardBorder),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = stringResource(R.string.csb_beacon),
                                    fontSize = 11.sp,
                                    color = RonovaTextSecondary
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Sensors,
                                        contentDescription = null,
                                        tint = if (isDiscoverable) RonovaVioletAccent else RonovaTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (isDiscoverable) stringResource(R.string.csb_active) else stringResource(R.string.csb_hidden),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isDiscoverable) RonovaVioletAccent else RonovaTextMuted
                                    )
                                }
                            }
                        }

                        // Multi-Hop Relay Stat
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, RonovaCardBorder),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = stringResource(R.string.csb_relayed_packets),
                                    fontSize = 11.sp,
                                    color = RonovaTextSecondary
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${diagnostics.packetsRelayed}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = RonovaPinkPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                isExpanded = false
                                onNavigateToDiscovery?.invoke() ?: viewModel.startDiscovery()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("status_bar_scan_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BluetoothSearching,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.home_scan_peers),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                try {
                                    val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                                        putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(discoverableIntent)
                                } catch (e: Exception) {
                                    viewModel.checkDiscoverability()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, RonovaCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("status_bar_make_discoverable_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                tint = RonovaVioletAccent,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.csb_be_discoverable),
                                fontSize = 12.sp,
                                color = RonovaVioletAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (hasActiveConnections) {
                            OutlinedButton(
                                onClick = { viewModel.disconnectAll() },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, RonovaError.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("status_bar_disconnect_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.csb_disconnect_all),
                                    tint = RonovaError,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
