package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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

import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonSearch

data class RonovaDrawerItem(
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
    unreadMessagesCount: Int = 0,
    missedCallsCount: Int = 0,
    pendingRelaysCount: Int = 0,
    onNavigate: (String) -> Unit,
    onClose: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val drawerWidth = if (screenWidth * 0.8f < 320.dp) screenWidth * 0.8f else 320.dp

    val drawerItems = listOf(
        RonovaDrawerItem(Screen.Home.route, R.string.nav_home, Icons.Default.Home, "drawer_item_home"),
        RonovaDrawerItem(Screen.FriendsList.route, R.string.nav_friends, Icons.Default.People, "drawer_item_friends"),
        RonovaDrawerItem(Screen.Directory.route, R.string.nav_directory, Icons.Default.PersonSearch, "drawer_item_directory"),
        RonovaDrawerItem(Screen.FriendRequests.route, R.string.nav_requests, Icons.Default.Notifications, "drawer_item_requests"),
        RonovaDrawerItem(Screen.Chats.route, R.string.nav_chats, Icons.AutoMirrored.Filled.Chat, "drawer_item_chats", unreadMessagesCount),
        RonovaDrawerItem(Screen.CallHistory.route, R.string.nav_calls, Icons.Default.Call, "drawer_item_calls", missedCallsCount),
        RonovaDrawerItem(Screen.Discovery.route, R.string.drawer_connected_peers, Icons.Default.Bluetooth, "drawer_item_peers"),
        RonovaDrawerItem(Screen.RelayNetwork.route, R.string.drawer_mesh_map, Icons.Default.Hub, "drawer_item_mesh_map"),
        RonovaDrawerItem(Screen.PendingMessages.route, R.string.drawer_store_forward, Icons.Default.SwapHoriz, "drawer_item_store_forward", pendingRelaysCount),
        RonovaDrawerItem(Screen.Permissions.route, R.string.drawer_permissions, Icons.Default.Security, "drawer_item_permissions"),
        RonovaDrawerItem(Screen.BatteryGuidance.route, R.string.drawer_battery_guidance, Icons.Default.BatteryAlert, "drawer_item_battery"),
        RonovaDrawerItem(Screen.SecuritySettings.route, R.string.drawer_privacy_security, Icons.Default.Lock, "drawer_item_security"),
        RonovaDrawerItem(Screen.Settings.route, R.string.nav_settings, Icons.Default.Settings, "drawer_item_settings"),
        RonovaDrawerItem(Screen.About.route, R.string.drawer_about, Icons.Default.Info, "drawer_item_about")
    )

    ModalDrawerSheet(
        modifier = Modifier
            .width(drawerWidth)
            .fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerTonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .statusBarsPadding()
        ) {
            // Drawer Header: App logo, name, and tagline ONLY (Section 6)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RonovaGoldLight)
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_image),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = RonovaTextPrimary,
                                fontSize = 18.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.tagline),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = RonovaTextMuted,
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp
                            ),
                            maxLines = 2
                        )
                    }
                }
            }

            HorizontalDivider(color = RonovaCardBorder.copy(alpha = 0.5f), thickness = 1.dp)

            // Navigation Items List: 56 dp row height, 24 dp vector icons, 16 sp Bengali labels, active pill
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
                    val itemBorder = if (isSelected) BorderStroke(1.dp, RonovaGoldPrimary.copy(alpha = 0.6f)) else null
                    val itemTextColor = if (isSelected) RonovaGoldDark else RonovaTextPrimary
                    val itemIconColor = if (isSelected) RonovaGoldDark else RonovaTextMuted

                    Surface(
                        onClick = {
                            onClose()
                            onNavigate(item.route)
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = itemBg,
                        border = itemBorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag(item.testTag)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = stringResource(item.titleRes),
                                    tint = itemIconColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = stringResource(item.titleRes),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = itemTextColor,
                                        fontSize = 15.5.sp
                                    )
                                )
                            }

                            if (item.badgeCount > 0) {
                                Badge(
                                    containerColor = RonovaGoldPrimary,
                                    contentColor = RonovaTextPrimary
                                ) {
                                    Text(
                                        text = "${item.badgeCount}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = RonovaCardBorder.copy(alpha = 0.4f), thickness = 1.dp)

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
