package com.example.ui.components


import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.bluetooth.BluetoothStatus
import com.example.ui.theme.AlataFontFamily
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
import com.example.ui.theme.RonovaPinkSecondary
import com.example.ui.theme.RonovaSuccess
import com.example.ui.theme.RonovaTextMuted
import com.example.ui.theme.RonovaTextPrimary
import com.example.ui.theme.RonovaTextSecondary
import com.example.ui.theme.RonovaVioletAccent
import com.example.ui.theme.RonovaWarning

import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import android.widget.Toast
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.example.R
import com.example.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RonovaTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    onOpenDrawer: (() -> Unit)? = null,
    statusBadge: (@Composable () -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, RonovaCardBorder.copy(alpha = 0.6f)),
        shadowElevation = 0.5.dp
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 0.2.sp
                            ),
                            maxLines = 1
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = RonovaTextMuted,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }
                    if (statusBadge != null) {
                        statusBadge()
                    }
                }
            },
            navigationIcon = {
                if (onOpenDrawer != null) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("top_bar_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = RonovaPinkPrimary
                        )
                    }
                } else if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("top_bar_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = RonovaPinkPrimary
                        )
                    }
                }
            },
            actions = { actions() },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            ),
            windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
        )
    }
}

@Composable
fun StatusBadge(status: BluetoothStatus) {
    val (bgColor, textColor, label) = when (status) {
        BluetoothStatus.CONNECTED -> Triple(Color(0xFFECFDF5), RonovaSuccess, "Connected")
        BluetoothStatus.DISCOVERING -> Triple(Color(0xFFFFFBEB), RonovaWarning, "Scanning...")
        BluetoothStatus.CONNECTING -> Triple(Color(0xFFFFFBEB), RonovaWarning, "Linking...")
        BluetoothStatus.READY -> Triple(RonovaGoldLight, RonovaGoldDark, "Mesh Ready")
        BluetoothStatus.PERMISSION_REQUIRED -> Triple(Color(0xFFFEF2F2), RonovaError, "Permission Needed")
        BluetoothStatus.DISABLED -> Triple(Color(0xFFF3F4F6), Color.Gray, "Bluetooth Off")
        BluetoothStatus.UNSUPPORTED -> Triple(Color(0xFFFEF2F2), RonovaError, "No Bluetooth")
        BluetoothStatus.ERROR -> Triple(Color(0xFFFEF2F2), RonovaError, "Mesh Error")
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_anim"
    )

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(
                        if (status == BluetoothStatus.CONNECTED || status == BluetoothStatus.DISCOVERING || status == BluetoothStatus.READY)
                            textColor.copy(alpha = pulseAlpha)
                        else textColor
                    )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                fontFamily = AlataFontFamily
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color = RonovaPinkPrimary,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, RonovaCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = RonovaTextMuted,
                        fontWeight = FontWeight.Medium
                    )
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = RonovaTextPrimary
                )
            )
        }
    }
}

@Composable
fun MessageBubble(
    text: String,
    timestamp: Long,
    deliveryState: String,
    hopCount: Int,
    isOutgoing: Boolean,
    onDetailsClick: () -> Unit
) {
    val bubbleShape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (isOutgoing) 18.dp else 4.dp,
        bottomEnd = if (isOutgoing) 4.dp else 18.dp
    )

    val timeFormatted = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(timestamp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    ) {
        Surface(
            modifier = Modifier
                .clickable { onDetailsClick() }
                .then(
                    if (isOutgoing) {
                        Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(RonovaGoldPrimary, RonovaGoldSecondary)
                                ),
                                shape = bubbleShape
                            )
                            .border(BorderStroke(1.dp, RonovaCardBorder), bubbleShape)
                    } else {
                        Modifier.border(BorderStroke(1.dp, RonovaCardBorder), bubbleShape)
                    }
                ),
            color = if (isOutgoing) Color.Transparent else MaterialTheme.colorScheme.surface,
            shape = bubbleShape,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)) {
                Text(
                    text = text,
                    color = RonovaTextPrimary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 21.sp,
                        fontFamily = AlataFontFamily,
                        fontWeight = if (isOutgoing) FontWeight.Medium else FontWeight.Normal
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = timeFormatted,
                        fontSize = 10.5.sp,
                        color = if (isOutgoing) RonovaTextPrimary.copy(alpha = 0.75f) else RonovaTextMuted,
                        fontFamily = AlataFontFamily
                    )
                    if (isOutgoing) {
                        Spacer(modifier = Modifier.width(5.dp))
                        when (deliveryState) {
                            "DELIVERED" -> Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Delivered",
                                tint = RonovaTextPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            "RELAYED" -> Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Relayed",
                                tint = RonovaTextPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            "SENDING" -> Icon(
                                imageVector = Icons.Default.HourglassEmpty,
                                contentDescription = "Sending",
                                tint = RonovaTextPrimary.copy(alpha = 0.75f),
                                modifier = Modifier.size(14.dp)
                            )
                            "QUEUED" -> Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Queued",
                                tint = RonovaTextPrimary.copy(alpha = 0.75f),
                                modifier = Modifier.size(14.dp)
                            )
                            "FAILED", "EXPIRED" -> Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Failed",
                                tint = RonovaError,
                                modifier = Modifier.size(14.dp)
                            )
                            else -> Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Sent",
                                tint = RonovaTextPrimary.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
        if (hopCount > 1) {
            Text(
                text = "⚡ Relayed through $hopCount hops",
                fontSize = 10.sp,
                color = RonovaVioletAccent,
                fontFamily = AlataFontFamily,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

/**
 * Reusable feature item with clean iconic badge and Alata typography
 * matching the user reference design (Screenshot 8)
 */
@Composable
fun SpecFeatureItem(
    icon: ImageVector,
    title: String,
    description: String,
    accentColor: Color = RonovaPinkPrimary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accentColor.copy(alpha = 0.08f))
                .border(BorderStroke(1.dp, accentColor.copy(alpha = 0.2f)), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = RonovaTextPrimary,
                    fontFamily = AlataFontFamily,
                    fontSize = 17.sp
                )
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = RonovaTextSecondary,
                    lineHeight = 22.sp,
                    fontFamily = AlataFontFamily,
                    fontSize = 13.5.sp
                )
            )
        }
    }
}

@Composable
fun AudioWaveformVisualizer(
    amplitude: Float,
    isMuted: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val animScale by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_anim"
    )

    Row(
        modifier = modifier.height(48.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bars = 18
        for (i in 0 until bars) {
            val factor = if (isMuted) 0.05f else ((amplitude * 1.5f).coerceIn(0.1f, 1f) * (0.35f + (i % 4) * 0.2f * animScale))
            val barHeight = (42.dp * factor).coerceAtLeast(4.dp)
            val barColor = if (isMuted) Color.LightGray else if (i % 2 == 0) RonovaPinkPrimary else RonovaVioletAccent
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .width(4.dp)
                    .height(barHeight)
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )
        }
    }
}

data class RonovaDrawerItemData(
    val route: String,
    val titleRes: Int,
    val icon: ImageVector,
    val testTag: String,
    val badgeCount: Int = 0
)

@Composable
fun RonovaNavigationDrawerSheet(
    currentRoute: String?,
    deviceId: String,
    unreadMessagesCount: Int,
    missedCallsCount: Int,
    pendingRelaysCount: Int,
    onNavigate: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val items = listOf(
        RonovaDrawerItemData(Screen.Home.route, R.string.nav_home, Icons.Default.Home, "drawer_item_home"),
        RonovaDrawerItemData(Screen.Chats.route, R.string.nav_chats, Icons.AutoMirrored.Filled.Chat, "drawer_item_chats", unreadMessagesCount),
        RonovaDrawerItemData(Screen.CallHistory.route, R.string.nav_calls, Icons.Default.Call, "drawer_item_calls", missedCallsCount),
        RonovaDrawerItemData(Screen.Discovery.route, R.string.drawer_connected_peers, Icons.Default.BluetoothSearching, "drawer_item_peers"),
        RonovaDrawerItemData(Screen.RelayNetwork.route, R.string.drawer_mesh_map, Icons.Default.Hub, "drawer_item_mesh_map"),
        RonovaDrawerItemData(Screen.PendingMessages.route, R.string.drawer_store_forward, Icons.Default.SwapHoriz, "drawer_item_queue", pendingRelaysCount),
        RonovaDrawerItemData(Screen.BatteryGuidance.route, R.string.drawer_battery_guidance, Icons.Default.BatteryAlert, "drawer_item_battery"),
        RonovaDrawerItemData(Screen.Permissions.route, R.string.drawer_permissions, Icons.Default.Security, "drawer_item_permissions"),
        RonovaDrawerItemData(Screen.SecuritySettings.route, R.string.drawer_privacy_security, Icons.Default.Lock, "drawer_item_security"),
        RonovaDrawerItemData(Screen.Settings.route, R.string.nav_settings, Icons.Default.Settings, "drawer_item_settings"),
        RonovaDrawerItemData(Screen.About.route, R.string.drawer_about, Icons.Default.Info, "drawer_item_about")
    )

    ModalDrawerSheet(
        modifier = Modifier.width(310.dp),
        drawerContainerColor = Color.White,
        drawerTonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(RonovaGoldLight, Color.White)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_image),
                            contentDescription = "Ronova Logo",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RonovaTextPrimary
                                )
                            )
                            Text(
                                text = stringResource(R.string.splash_subtitle),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = RonovaTextMuted,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Node ID badge with copy action
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Node ID", deviceId))
                                Toast.makeText(context, context.getString(R.string.settings_copied), Toast.LENGTH_SHORT).show()
                            },
                        color = RonovaGoldLight,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, RonovaCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Node ID",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RonovaGoldDark
                                )
                                Text(
                                    text = deviceId,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RonovaTextPrimary
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = RonovaGoldDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = RonovaCardBorder.copy(alpha = 0.5f), thickness = 1.dp)

            Spacer(modifier = Modifier.height(6.dp))

            items.forEach { item ->
                val isSelected = currentRoute == item.route
                NavigationDrawerItem(
                    label = {
                        Text(
                            text = stringResource(item.titleRes),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) RonovaTextPrimary else RonovaTextPrimary.copy(alpha = 0.85f),
                                fontSize = 13.5.sp
                            )
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (isSelected) RonovaGoldDark else RonovaTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    badge = if (item.badgeCount > 0) {
                        {
                            Badge(
                                containerColor = RonovaGoldPrimary,
                                contentColor = RonovaTextPrimary
                            ) {
                                Text(
                                    text = "${item.badgeCount}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    } else null,
                    selected = isSelected,
                    onClick = {
                        onClose()
                        onNavigate(item.route)
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = RonovaGoldLight,
                        unselectedContainerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .testTag(item.testTag)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Ronova Mesh v1.0 • Offline Native",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = RonovaTextMuted,
                        fontSize = 11.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}


