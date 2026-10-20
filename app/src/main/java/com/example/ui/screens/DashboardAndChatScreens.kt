package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Hub
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.core.database.ConversationEntity
import com.example.core.database.MessageEntity
import com.example.ui.components.ConnectionStatusBar
import com.example.ui.components.MessageBubble
import com.example.ui.components.RonovaTopBar
import com.example.ui.components.SpecFeatureItem
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.RonovaGoldContainer
import com.example.ui.theme.RonovaGoldDark
import com.example.ui.theme.RonovaGoldLight
import com.example.ui.theme.RonovaGoldPrimary
import com.example.ui.theme.RonovaGoldSecondary
import com.example.ui.theme.RonovaCardBorder
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: RonovaViewModel,
    onNavigateToDiscovery: () -> Unit,
    onNavigateToChats: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToRelayNetwork: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPending: () -> Unit,
    onNavigateToVerify: (String) -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    onMenuClick: (() -> Unit)? = null
) {
    val status by viewModel.bluetoothStatus.collectAsState()
    val connectedPeers by viewModel.connectedPeers.collectAsState()
    val pendingPackets by viewModel.pendingRelayPackets.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val isRelayOn by viewModel.isRelayOptedIn.collectAsState()
    val diagnostics by viewModel.diagnostics.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current

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
            // Top Persistent Connection Status Bar (Warns of permission needs / BT off per screenshots)
            ConnectionStatusBar(
                viewModel = viewModel,
                onNavigateToDiscovery = onNavigateToDiscovery,
                onNavigateToRelayNetwork = onNavigateToRelayNetwork
            )

            // Header with Hamburger Menu, Title, Status and Shortcuts
            RonovaTopBar(
                title = stringResource(R.string.app_name),
                subtitle = stringResource(R.string.drawer_header_subtitle),
                onMenuClick = onMenuClick,
                statusBadge = {
                    StatusBadge(status = status)
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToRelayNetwork,
                        modifier = Modifier.testTag("home_mesh_topology_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = stringResource(R.string.home_mesh_map),
                            tint = RonovaGoldDark
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.nav_settings),
                            tint = RonovaGoldDark
                        )
                    }
                }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(2.dp))
                    // Compact Identity Card with cryptographic fingerprint & status
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = BorderStroke(1.dp, RonovaCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(RonovaGoldLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = RonovaGoldDark,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = stringResource(R.string.home_node_identity),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = RonovaTextPrimary
                                            )
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable {
                                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Node ID", viewModel.myDeviceId))
                                                android.widget.Toast.makeText(context, context.getString(R.string.home_copied_id), android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Text(
                                                text = viewModel.myDeviceId,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = RonovaGoldDark,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.5.sp
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = stringResource(R.string.settings_copied),
                                                tint = RonovaGoldPrimary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }

                                // Status indicator tag on right
                                val (statusTagColor, statusTagText) = when (status) {
                                    com.example.core.bluetooth.BluetoothStatus.CONNECTED -> RonovaSuccess to stringResource(R.string.bt_status_connected)
                                    com.example.core.bluetooth.BluetoothStatus.DISCOVERING -> RonovaWarning to stringResource(R.string.disc_scanning)
                                    com.example.core.bluetooth.BluetoothStatus.PERMISSION_REQUIRED -> RonovaError to stringResource(R.string.bt_status_permission_required)
                                    com.example.core.bluetooth.BluetoothStatus.DISABLED -> Color.Gray to stringResource(R.string.bt_status_disabled)
                                    else -> RonovaGoldDark to stringResource(R.string.bt_status_ready)
                                }

                                Surface(
                                    color = statusTagColor.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, statusTagColor.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = statusTagText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusTagColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Stats Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = stringResource(R.string.home_peers_stat),
                            value = "${connectedPeers.count { it.isConnected }}",
                            icon = Icons.Default.Hub,
                            accentColor = RonovaGoldPrimary,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToDiscovery
                        )
                        StatCard(
                            title = stringResource(R.string.home_store_fwd_stat),
                            value = "${pendingPackets.size}",
                            icon = Icons.Default.SwapHoriz,
                            accentColor = RonovaWarning,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToPending
                        )
                        StatCard(
                            title = stringResource(R.string.home_chats_stat),
                            value = "${conversations.size}",
                            icon = Icons.AutoMirrored.Filled.Chat,
                            accentColor = RonovaGoldDark,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToChats
                        )
                    }
                }

                // Primary Quick Actions Bar (Scan Peers & Mesh Map)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onNavigateToDiscovery,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("home_scan_peers_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RonovaGoldPrimary,
                                contentColor = RonovaTextPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = RonovaTextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.home_scan_peers),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = RonovaTextPrimary
                            )
                        }

                        OutlinedButton(
                            onClick = onNavigateToRelayNetwork,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("home_mesh_topology_button"),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, RonovaGoldPrimary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RonovaGoldDark)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = RonovaGoldDark
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.home_mesh_map),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = RonovaGoldDark
                            )
                        }
                    }
                }

                // System Architecture & Specs Card (Screenshots 2 & 3)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = BorderStroke(1.dp, RonovaCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.home_specs_title),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = RonovaTextPrimary
                                    )
                                )
                                Text(
                                    text = stringResource(R.string.home_specs_version),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = RonovaGoldDark,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            SpecFeatureItem(
                                icon = Icons.Default.PhoneAndroid,
                                title = stringResource(R.string.home_spec_platform_title),
                                description = stringResource(R.string.home_spec_platform_desc),
                                accentColor = RonovaGoldPrimary
                            )
                            SpecFeatureItem(
                                icon = Icons.Default.Bluetooth,
                                title = stringResource(R.string.home_spec_comm_title),
                                description = stringResource(R.string.home_spec_comm_desc),
                                accentColor = RonovaGoldPrimary
                            )
                            SpecFeatureItem(
                                icon = Icons.Default.WifiOff,
                                title = stringResource(R.string.home_spec_conn_title),
                                description = stringResource(R.string.home_spec_conn_desc),
                                accentColor = RonovaGoldPrimary
                            )
                            SpecFeatureItem(
                                icon = Icons.Default.AccountBalanceWallet,
                                title = stringResource(R.string.home_spec_biz_title),
                                description = stringResource(R.string.home_spec_biz_desc),
                                accentColor = RonovaGoldPrimary
                            )
                        }
                    }
                }

                // Connected Mesh Peers Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.home_connected_peers_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = RonovaTextPrimary
                            )
                        )
                        Text(
                            text = stringResource(R.string.home_view_all),
                            color = RonovaGoldDark,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.clickable { onNavigateToDiscovery() }
                        )
                    }
                }

                val connectedList = connectedPeers.filter { it.isConnected }
                if (connectedList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, RonovaCardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(RonovaGoldLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bluetooth,
                                        contentDescription = null,
                                        tint = RonovaGoldPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = stringResource(R.string.home_no_peers_msg),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = RonovaTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.home_no_peers_sub),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = RonovaTextMuted,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    ),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(connectedList) { peer ->
                        ConnectedPeerItem(
                            peer = peer,
                            onChat = {
                                val targetId = peer.deviceId.ifEmpty { peer.bluetoothAddress }
                                onNavigateToChat(targetId)
                            },
                            onVerify = {
                                val targetId = peer.deviceId.ifEmpty { peer.bluetoothAddress }
                                onNavigateToVerify(targetId)
                            }
                        )
                    }
                }

                // Recent Conversations Section
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.home_recent_convs),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = RonovaTextPrimary
                            )
                        )
                        Text(
                            text = stringResource(R.string.home_open_chats),
                            color = RonovaGoldDark,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.clickable { onNavigateToChats() }
                        )
                    }
                }

                if (conversations.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, RonovaCardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(RonovaGoldLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = null,
                                        tint = RonovaGoldPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = stringResource(R.string.home_no_convs_msg),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = RonovaTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = onNavigateToDiscovery,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, RonovaGoldPrimary)
                                ) {
                                    Text(
                                        text = stringResource(R.string.home_find_nearby_nodes),
                                        color = RonovaGoldDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(conversations.take(4)) { conv ->
                        ConversationRowItem(
                            conversation = conv,
                            onClick = { onNavigateToChat(conv.conversationId) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun ConnectedPeerItem(
    peer: com.example.core.database.PeerEntity,
    onChat: () -> Unit,
    onVerify: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, RonovaCardBorder)
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
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFECFDF5)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = RonovaSuccess,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = peer.deviceName.ifEmpty { "Mesh Peer" },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = RonovaTextPrimary
                        )
                    )
                    Text(
                        text = peer.deviceId.ifEmpty { peer.bluetoothAddress },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = RonovaTextMuted,
                            fontSize = 11.5.sp
                        )
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onVerify) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Verify",
                        tint = RonovaTextMuted
                    )
                }
                IconButton(
                    onClick = onChat,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(RonovaPinkLight)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Chat",
                        tint = RonovaPinkPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationRowItem(
    conversation: ConversationEntity,
    onClick: () -> Unit
) {
    val timeFormatted = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
        .format(Date(conversation.lastMessageTimestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, RonovaCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(RonovaPinkLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    tint = RonovaPinkPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.contactName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = RonovaTextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = RonovaTextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.lastMessageText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = RonovaTextSecondary,
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (conversation.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(RonovaPinkPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${conversation.unreadCount}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationListScreen(
    viewModel: RonovaViewModel,
    onBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onStartNewChat: () -> Unit,
    onMenuClick: (() -> Unit)? = null
) {
    val conversations by viewModel.conversations.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filtered = conversations.filter {
        it.contactName.contains(searchQuery, ignoreCase = true) ||
        it.conversationId.contains(searchQuery, ignoreCase = true) ||
        it.lastMessageText.contains(searchQuery, ignoreCase = true)
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
                .widthIn(max = 680.dp)
        ) {
            RonovaTopBar(
                title = stringResource(R.string.chat_list_title),
                subtitle = stringResource(R.string.chat_encrypted_badge),
                onBack = if (onMenuClick == null) onBack else null,
                onMenuClick = onMenuClick
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                placeholder = { Text(stringResource(R.string.chat_type_hint)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = RonovaTextMuted)
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RonovaPinkPrimary,
                    unfocusedBorderColor = RonovaCardBorder,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(RonovaPinkLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                tint = RonovaPinkPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.chat_no_conversations),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.chat_start_first),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = RonovaTextMuted,
                                fontSize = 12.sp
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered) { conv ->
                        ConversationRowItem(
                            conversation = conv,
                            onClick = { onNavigateToChat(conv.conversationId) }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = onStartNewChat,
                    containerColor = RonovaPinkPrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.testTag("fab_new_chat")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "New Chat")
                }
            }
        }
    }
}

@Composable
fun OneToOneChatScreen(
    deviceId: String,
    viewModel: RonovaViewModel,
    onBack: () -> Unit,
    onVoiceCall: (String) -> Unit,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToVerify: (String) -> Unit
) {
    val messages by viewModel.repository.getMessagesForConversation(deviceId)
        .collectAsState(initial = emptyList())
    val device by viewModel.repository.getDeviceByIdFlow(deviceId)
        .collectAsState(initial = null)

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Clear unread badge on entering
    LaunchedEffect(deviceId) {
        viewModel.repository.clearUnread(deviceId)
    }

    // Scroll to latest message on receive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAFAFA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = device?.displayName ?: deviceId,
                subtitle = stringResource(R.string.chat_encrypted_badge),
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = { onVoiceCall(deviceId) },
                        modifier = Modifier.testTag("chat_voice_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = stringResource(R.string.chat_call),
                            tint = RonovaPinkPrimary
                        )
                    }
                    IconButton(
                        onClick = { onNavigateToVerify(deviceId) },
                        modifier = Modifier.testTag("chat_verify_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.chat_verify),
                            tint = RonovaTextMuted
                        )
                    }
                }
            )

            // Encryption disclaimer pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = RonovaPinkLight,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = RonovaPinkDark,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.chat_encrypted_badge),
                            fontSize = 10.sp,
                            color = RonovaPinkDark,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Message list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState
            ) {
                items(messages) { msg ->
                    MessageBubble(
                        text = msg.content,
                        timestamp = msg.timestamp,
                        deliveryState = msg.deliveryState,
                        hopCount = msg.hopCount,
                        isOutgoing = msg.isOutgoing,
                        onDetailsClick = { onNavigateToDetails(msg.messageId) }
                    )
                }
            }

            // Chat input bar
            Surface(
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input"),
                        placeholder = { Text(stringResource(R.string.chat_type_hint)) },
                        maxLines = 4,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RonovaPinkPrimary,
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                viewModel.sendMessage(deviceId, textInput)
                                textInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(RonovaPinkPrimary)
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.chat_send),
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageDeliveryDetailsScreen(
    messageId: String,
    viewModel: RonovaViewModel,
    onBack: () -> Unit
) {
    val message by viewModel.repository.getMessageByIdFlow(messageId)
        .collectAsState(initial = null)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAFAFA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = stringResource(R.string.delivery_details_title),
                subtitle = stringResource(R.string.delivery_details_subtitle),
                onBack = onBack
            )

            if (message == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.msg_not_found), color = RonovaTextMuted)
                }
            } else {
                val msg = message!!
                val timeFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(msg.timestamp))

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
                            Text(
                                text = "Message Content",
                                style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.content,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RonovaTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(stringResource(R.string.delivery_state_label), fontSize = 11.sp, color = RonovaTextMuted)
                                    Text(
                                        text = msg.deliveryState,
                                        fontWeight = FontWeight.Bold,
                                        color = when (msg.deliveryState) {
                                            "DELIVERED" -> RonovaSuccess
                                            "RELAYED" -> RonovaWarning
                                            "FAILED", "EXPIRED" -> RonovaError
                                            else -> RonovaPinkPrimary
                                        }
                                    )
                                }
                                Column {
                                    Text(stringResource(R.string.hops_traversed_label), fontSize = 11.sp, color = RonovaTextMuted)
                                    Text(
                                        text = "${msg.hopCount}",
                                        fontWeight = FontWeight.Bold,
                                        color = RonovaTextPrimary
                                    )
                                }
                                Column {
                                    Text(stringResource(R.string.direction_label), fontSize = 11.sp, color = RonovaTextMuted)
                                    Text(
                                        text = if (msg.isOutgoing) "Outgoing" else "Incoming",
                                        fontWeight = FontWeight.Bold,
                                        color = RonovaTextPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Protocol Metadata",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            MetadataRow(label = stringResource(R.string.delivery_packet_id), value = msg.messageId)
                            MetadataRow(label = stringResource(R.string.delivery_sender_label), value = msg.senderDeviceId)
                            MetadataRow(label = stringResource(R.string.delivery_recipient_label), value = msg.recipientDeviceId)
                            MetadataRow(label = stringResource(R.string.delivery_timestamp_label), value = timeFormatted)
                            MetadataRow(label = "Relay Nodes", value = msg.routeNodesJson)
                            MetadataRow(
                                label = stringResource(R.string.delivery_cipher_label),
                                value = msg.encryptedPayload.take(24).plus("... (AES-256-GCM)")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (msg.deliveryState in listOf("QUEUED", "FAILED", "SENDING")) {
                        Button(
                            onClick = {
                                viewModel.retryPendingMessage(msg)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.retry_sending_mesh))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.deleteMessage(msg.messageId)
                            onBack()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RonovaError),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.delete_local_msg))
                    }
                }
            }
        }
    }
}

@Composable
fun MetadataRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 11.sp, color = RonovaTextMuted)
        Text(
            text = value,
            fontSize = 12.sp,
            color = RonovaTextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PendingMessagesScreen(
    viewModel: RonovaViewModel,
    onBack: () -> Unit
) {
    val pendingMessages by viewModel.pendingMessages.collectAsState()
    val pendingPackets by viewModel.pendingRelayPackets.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAFAFA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = stringResource(R.string.pending_queue_title),
                subtitle = stringResource(R.string.pending_queue_subtitle),
                onBack = onBack,
                actions = {
                    IconButton(onClick = { viewModel.resendPendingPackets() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.pending_retry_all),
                            tint = RonovaPinkPrimary
                        )
                    }
                }
            )

            if (pendingMessages.isEmpty() && pendingPackets.isEmpty()) {
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
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = RonovaSuccess,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = stringResource(R.string.pending_queue_empty),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.pending_queue_empty_desc),
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
                    item {
                        Text(
                            text = stringResource(R.string.pending_outgoing_title, pendingMessages.size),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    items(pendingMessages) { msg ->
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = msg.content,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "To: ${msg.recipientDeviceId}",
                                        fontSize = 11.sp,
                                        color = RonovaTextMuted
                                    )
                                    Text(
                                        text = "State: ${msg.deliveryState}",
                                        fontSize = 10.sp,
                                        color = RonovaWarning
                                    )
                                }
                                Button(
                                    onClick = { viewModel.retryPendingMessage(msg) },
                                    colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(stringResource(R.string.action_retry), fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.pending_relaying_title, pendingPackets.size),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            if (pendingPackets.isNotEmpty()) {
                                Text(
                                    text = "Clear Queue",
                                    color = RonovaError,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { viewModel.clearRelayQueue() }
                                )
                            }
                        }
                    }

                    items(pendingPackets) { packet ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Packet: ${packet.packetId.take(12)}...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Origin: ${packet.originDeviceId} → Dest: ${packet.destinationDeviceId}",
                                    fontSize = 11.sp,
                                    color = RonovaTextMuted
                                )
                                Text(
                                    text = "Current Hops: ${packet.hopCount} / ${packet.maxHops}",
                                    fontSize = 10.sp,
                                    color = RonovaPinkDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
