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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
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

import androidx.compose.foundation.layout.offset

/**
 * 3D header control adhering to PRD v2.0 Section 5:
 * - 40 dp rounded-square button (RoundedCornerShape(10.dp))
 * - Two-layer shadow (ambient 2 dp plus key 4 dp)
 * - Subtle top highlight (white to warm tint vertical gradient)
 * - 1 dp light border
 * - Pressed state lowers elevation and shifts content 1 dp
 * - Vector icons only
 * - Shared across all screens
 */
@Composable
fun HeaderAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(10.dp)
    val elevation = if (isPressed) 1.dp else 4.dp
    val offsetY = if (isPressed) 1.dp else 0.dp

    Box(
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .size(40.dp)
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color(0x33000000),
                spotColor = Color(0x40000000)
            )
            .offset(y = offsetY)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFFFF9E6)
                    )
                )
            )
            .border(1.dp, RonovaCardBorder.copy(alpha = 0.9f), shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = RonovaGoldDark,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RonovaTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    onOpenDrawer: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    statusBadge: (@Composable () -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    val drawerAction = onOpenDrawer ?: onMenuClick
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, RonovaCardBorder.copy(alpha = 0.6f)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                if (drawerAction != null) {
                    HeaderAction(
                        icon = Icons.Default.Menu,
                        contentDescription = "মেনু",
                        onClick = drawerAction,
                        testTag = "top_bar_menu_button"
                    )
                } else if (onBack != null) {
                    HeaderAction(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "পেছনে যান",
                        onClick = onBack,
                        testTag = "top_bar_back_button"
                    )
                }

                Column(modifier = Modifier.padding(start = 2.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 17.sp
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
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (statusBadge != null) {
                    statusBadge()
                }
                actions()
            }
        }
    }
}

/**
 * 3D/Glassmorphic styled button adhering to PRD v2.0:
 * - Height: 44 dp (min touch target 48 dp)
 * - Corner radius: 14 dp
 * - Brand yellow translucent gradient with highlight border
 * - Pressed animation (scale 0.97, 100 ms)
 * - Accessible contrast (min 4.5:1 with dark text)
 * - Supports loading and disabled states
 */
@Composable
fun Ronova3DButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    selected: Boolean = false,
    icon: ImageVector? = null,
    testTag: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed && enabled && !loading) 0.97f else 1.0f

    val baseAlpha = when {
        !enabled -> 0.4f
        selected -> 1.0f
        isPressed -> 0.95f
        else -> 0.85f
    }

    val gradient = when {
        !enabled -> Brush.verticalGradient(
            colors = listOf(
                RonovaGoldLight.copy(alpha = 0.3f),
                RonovaGoldSecondary.copy(alpha = 0.2f)
            )
        )
        selected -> Brush.verticalGradient(
            colors = listOf(
                RonovaGoldPrimary,
                RonovaGoldDark
            )
        )
        else -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFF3B0).copy(alpha = baseAlpha),
                RonovaGoldPrimary.copy(alpha = baseAlpha)
            )
        )
    }

    val borderColor = when {
        !enabled -> RonovaCardBorder.copy(alpha = 0.3f)
        selected -> RonovaGoldDark
        else -> Color.White.copy(alpha = 0.7f)
    }

    val shape = RoundedCornerShape(14.dp)
    val buttonModifier = modifier
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
        .defaultMinSize(minHeight = 48.dp)
        .shadow(
            elevation = if (enabled && !isPressed) 3.dp else 0.dp,
            shape = shape
        )
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clip(shape)
        .background(gradient)
        .border(1.dp, borderColor, shape)
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(),
            enabled = enabled && !loading,
            onClick = onClick
        )
        .padding(horizontal = 16.dp, vertical = 10.dp)

    Box(
        modifier = buttonModifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(18.dp)
                        .padding(end = 6.dp),
                    color = RonovaTextPrimary,
                    strokeWidth = 2.dp
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(end = 6.dp),
                    tint = if (enabled) RonovaTextPrimary else RonovaTextMuted
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (enabled) RonovaTextPrimary else RonovaTextMuted
                )
            )
        }
    }
}

@Composable
fun RonovaGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    selected: Boolean = false,
    icon: ImageVector? = null,
    testTag: String? = null
) {
    Ronova3DButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = loading,
        selected = selected,
        icon = icon,
        testTag = testTag
    )
}

@Composable
fun StatusBadge(status: BluetoothStatus) {
    val (bgColor, textColor, label) = when (status) {
        BluetoothStatus.CONNECTED -> Triple(Color(0xFFECFDF5), RonovaSuccess, "মেশে সংযুক্ত")
        BluetoothStatus.DISCOVERING -> Triple(Color(0xFFFFFBEB), RonovaWarning, "স্ক্যান হচ্ছে…")
        BluetoothStatus.CONNECTING -> Triple(Color(0xFFFFFBEB), RonovaWarning, "সংযোগ হচ্ছে…")
        BluetoothStatus.READY -> Triple(RonovaGoldLight, RonovaGoldDark, "মেশ প্রস্তুত")
        BluetoothStatus.PERMISSION_REQUIRED -> Triple(Color(0xFFFEF2F2), RonovaError, "অনুমতি প্রয়োজন")
        BluetoothStatus.DISABLED -> Triple(Color(0xFFF3F4F6), Color.Gray, "ব্লুটুথ বন্ধ")
        BluetoothStatus.UNSUPPORTED -> Triple(Color(0xFFFEF2F2), RonovaError, "সমর্থিত নয়")
        BluetoothStatus.ERROR -> Triple(Color(0xFFFEF2F2), RonovaError, "মেশ ত্রুটি")
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
                color = textColor
            )
        }
    }
}

/**
 * Slim status chip row adhering to Section 5:
 * Inside or directly under the header, expandable on tap to real status.
 * Never creates a gap or double border above the header.
 */
@Composable
fun MeshStatusChipRow(
    status: BluetoothStatus,
    activeConnections: Int = 0,
    isDiscoverable: Boolean = false,
    onEnableBluetooth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val (dotColor, statusText) = when (status) {
        BluetoothStatus.CONNECTED -> Pair(RonovaSuccess, "মেশে সংযুক্ত ($activeConnections টি ডিভাইস)")
        BluetoothStatus.DISCOVERING -> Pair(RonovaWarning, "নোড খোঁজা হচ্ছে…")
        BluetoothStatus.CONNECTING -> Pair(RonovaWarning, "সংযোগ স্থাপন হচ্ছে…")
        BluetoothStatus.READY -> Pair(RonovaGoldDark, "মেশ রেডিও প্রস্তুত")
        BluetoothStatus.PERMISSION_REQUIRED -> Pair(RonovaError, "ব্লুটুথ অনুমতি প্রয়োজন")
        BluetoothStatus.DISABLED -> Pair(Color(0xFFE53935), "ব্লুটুথ বন্ধ রয়েছে")
        BluetoothStatus.UNSUPPORTED -> Pair(RonovaError, "ব্লুটুথ সমর্থিত নয়")
        BluetoothStatus.ERROR -> Pair(RonovaError, "মেশ ত্রুটি")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(RonovaGoldLight.copy(alpha = 0.5f))
            .border(BorderStroke(0.5.dp, RonovaCardBorder.copy(alpha = 0.4f)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 16.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = RonovaTextPrimary,
                        fontSize = 12.sp
                    )
                )
            }

            if (status == BluetoothStatus.DISABLED) {
                Surface(
                    onClick = onEnableBluetooth,
                    shape = RoundedCornerShape(8.dp),
                    color = RonovaGoldPrimary,
                    modifier = Modifier.height(26.dp)
                ) {
                    Text(
                        text = "চালু করুন",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RonovaTextPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            } else {
                Text(
                    text = if (isDiscoverable) "দৃশ্যমান" else "লুকানো",
                    fontSize = 11.sp,
                    color = RonovaTextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
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
    transportType: String = "BLUETOOTH",
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
                    Icon(
                        imageVector = if (transportType == "INTERNET") Icons.Default.Public else Icons.Default.Bluetooth,
                        contentDescription = if (transportType == "INTERNET") "Internet" else "Bluetooth",
                        tint = if (isOutgoing) RonovaTextPrimary.copy(alpha = 0.65f) else RonovaTextMuted,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
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





