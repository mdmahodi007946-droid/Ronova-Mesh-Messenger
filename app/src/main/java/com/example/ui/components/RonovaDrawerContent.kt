package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.navigation.Screen
import com.example.ui.theme.RonovaCardBorder
import com.example.ui.theme.RonovaGoldDark
import com.example.ui.theme.RonovaGoldLight
import com.example.ui.theme.RonovaGoldPrimary
import com.example.ui.theme.RonovaTextMuted
import com.example.ui.theme.RonovaTextPrimary

data class DrawerItem(
    val route: String,
    val titleRes: Int,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun RonovaDrawerContent(
    currentRoute: String?,
    myDeviceId: String,
    onNavigate: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    val drawerItems = listOf(
        DrawerItem(Screen.Home.route, R.string.nav_home, Icons.Default.Home, "drawer_item_home"),
        DrawerItem(Screen.Chats.route, R.string.nav_chats, Icons.AutoMirrored.Filled.Chat, "drawer_item_chats"),
        DrawerItem(Screen.CallHistory.route, R.string.nav_calls, Icons.Default.Call, "drawer_item_calls"),
        DrawerItem(Screen.Discovery.route, R.string.drawer_connected_peers, Icons.Default.Bluetooth, "drawer_item_peers"),
        DrawerItem(Screen.RelayNetwork.route, R.string.drawer_mesh_map, Icons.Default.Hub, "drawer_item_mesh_map"),
        DrawerItem(Screen.PendingMessages.route, R.string.drawer_store_forward, Icons.Default.SwapHoriz, "drawer_item_store_forward"),
        DrawerItem(Screen.BatteryGuidance.route, R.string.drawer_battery_guidance, Icons.Default.BatteryAlert, "drawer_item_battery"),
        DrawerItem(Screen.Permissions.route, R.string.drawer_permissions, Icons.Default.Security, "drawer_item_permissions"),
        DrawerItem(Screen.PrivacyPolicy.route, R.string.drawer_privacy_security, Icons.Default.Policy, "drawer_item_privacy"),
        DrawerItem(Screen.Settings.route, R.string.nav_settings, Icons.Default.Settings, "drawer_item_settings"),
        DrawerItem(Screen.About.route, R.string.drawer_about, Icons.Default.Info, "drawer_item_about")
    )

    ModalDrawerSheet(
        modifier = Modifier
            .widthIn(max = 320.dp)
            .fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerTonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .statusBarsPadding()
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RonovaGoldLight)
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RonovaGoldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Router,
                                    contentDescription = null,
                                    tint = RonovaTextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = RonovaTextPrimary
                                    )
                                )
                                Text(
                                    text = stringResource(R.string.drawer_header_subtitle),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = RonovaTextMuted,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(32.dp).testTag("drawer_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = RonovaTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Node ID chip
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, RonovaCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Node ID", myDeviceId))
                                    android.widget.Toast.makeText(context, context.getString(R.string.home_copied_id), android.widget.Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = myDeviceId,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = RonovaGoldDark
                            )
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = RonovaGoldPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = RonovaCardBorder.copy(alpha = 0.5f))

            // Navigation Items List
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                drawerItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    val itemBg = if (isSelected) RonovaGoldLight else Color.Transparent
                    val itemBorder = if (isSelected) BorderStroke(1.dp, RonovaGoldPrimary) else null
                    val itemTextColor = if (isSelected) RonovaTextPrimary else RonovaTextPrimary
                    val itemIconColor = if (isSelected) RonovaGoldDark else RonovaTextMuted

                    Surface(
                        onClick = {
                            onClose()
                            onNavigate(item.route)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = itemBg,
                        border = itemBorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(item.testTag)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = itemIconColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(item.titleRes),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = itemTextColor,
                                    fontSize = 13.5.sp
                                )
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = RonovaCardBorder.copy(alpha = 0.3f))

            // Footer info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "${stringResource(R.string.company_name)} • ${stringResource(R.string.home_specs_version)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = RonovaTextMuted
                    )
                )
            }
        }
    }
}
