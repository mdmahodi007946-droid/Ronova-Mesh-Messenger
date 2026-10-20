package com.example.ui.components


import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.remember
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
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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

@Composable
fun Ronova3DHeaderButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 100),
        label = "header_btn_scale"
    )
    val elevation by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isPressed) 0.5.dp else 2.dp,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 100),
        label = "header_btn_elevation"
    )

    Surface(
        modifier = modifier
            .size(38.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(11.dp),
        color = Color(0xFFFFFDF7),
        shadowElevation = elevation,
        border = BorderStroke(
            1.dp,
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.95f),
                    RonovaGoldSecondary.copy(alpha = 0.7f),
                    RonovaGoldDark.copy(alpha = 0.25f)
                )
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        if (isPressed) listOf(Color(0xFFFFECB3), Color(0xFFFFF8E1))
                        else listOf(Color(0xFFFFFFFF), Color(0xFFFFF8E1))
                    )
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RonovaTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    statusBadge: (@Composable () -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, RonovaCardBorder.copy(alpha = 0.5f)),
        shadowElevation = 1.dp
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
                if (onMenuClick != null) {
                    Box(modifier = Modifier.padding(start = 10.dp, end = 4.dp)) {
                        Ronova3DHeaderButton(
                            icon = Icons.Default.Menu,
                            contentDescription = stringResource(R.string.content_desc_menu),
                            onClick = onMenuClick,
                            testTag = "top_bar_menu_button"
                        )
                    }
                } else if (onBack != null) {
                    Box(modifier = Modifier.padding(start = 10.dp, end = 4.dp)) {
                        Ronova3DHeaderButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_back),
                            onClick = onBack,
                            testTag = "top_bar_back_button"
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
        BluetoothStatus.CONNECTED -> Triple(Color(0xFFECFDF5), RonovaSuccess, stringResource(R.string.bt_status_connected))
        BluetoothStatus.DISCOVERING -> Triple(Color(0xFFFFFBEB), RonovaWarning, stringResource(R.string.disc_scanning))
        BluetoothStatus.CONNECTING -> Triple(Color(0xFFFFFBEB), RonovaWarning, stringResource(R.string.bt_status_checking))
        BluetoothStatus.READY -> Triple(RonovaGoldLight, RonovaGoldDark, stringResource(R.string.bt_status_ready))
        BluetoothStatus.PERMISSION_REQUIRED -> Triple(Color(0xFFFEF2F2), RonovaError, stringResource(R.string.bt_status_permission_required))
        BluetoothStatus.DISABLED -> Triple(Color(0xFFF3F4F6), Color.Gray, stringResource(R.string.bt_status_disabled))
        BluetoothStatus.UNSUPPORTED -> Triple(Color(0xFFFEF2F2), RonovaError, stringResource(R.string.bt_status_unsupported))
        BluetoothStatus.ERROR -> Triple(Color(0xFFFEF2F2), RonovaError, stringResource(R.string.bt_status_permission_required))
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
                        color = if (isOutgoing) RonovaTextPrimary.copy(alpha = 0.75f) else RonovaTextMuted
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
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

/**
 * Reusable feature item with clean iconic badge and adaptive typography
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
                    fontSize = 17.sp
                )
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = RonovaTextSecondary,
                    lineHeight = 22.sp,
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

@Composable
fun Ronova3DButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    selected: Boolean = false,
    testTag: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1.0f,
        animationSpec = androidx.compose.animation.core.tween(100),
        label = "btn_scale"
    )
    val elevation by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (!enabled) 0.dp else if (isPressed) 1.dp else 2.5.dp,
        animationSpec = androidx.compose.animation.core.tween(100),
        label = "btn_elevation"
    )

    Surface(
        modifier = modifier
            .height(46.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading
            ) { onClick() },
        shape = RoundedCornerShape(13.dp),
        color = Color.Transparent,
        shadowElevation = elevation
    ) {
        // Glassmorphism styling with yellow brand palette: #FFC107, #FFD54F, #FFF8E1
        val backgroundBrush = when {
            !enabled -> Brush.verticalGradient(
                listOf(Color(0xFFF3F4F6), Color(0xFFE5E7EB))
            )
            selected -> Brush.verticalGradient(
                listOf(Color(0xFFFFD54F), Color(0xFFFFC107))
            )
            isPressed -> Brush.verticalGradient(
                listOf(Color(0xFFFFE082), Color(0xFFFFC107))
            )
            else -> Brush.verticalGradient(
                listOf(Color(0xFFFFFDE7), Color(0xFFFFD54F))
            )
        }

        val borderBrush = when {
            !enabled -> Brush.verticalGradient(listOf(Color(0xFFE0E0E0), Color(0xFFD6D6D6)))
            selected -> Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.95f), RonovaGoldDark)
            )
            else -> Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.95f),
                    RonovaGoldSecondary.copy(alpha = 0.85f),
                    RonovaGoldDark.copy(alpha = 0.4f)
                )
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush, shape = RoundedCornerShape(13.dp))
                .border(BorderStroke(1.dp, borderBrush), RoundedCornerShape(13.dp))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = RonovaTextPrimary,
                    modifier = Modifier.size(19.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (enabled) RonovaTextPrimary else RonovaTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (enabled) RonovaTextPrimary else RonovaTextMuted,
                            fontSize = 14.sp,
                            letterSpacing = 0.1.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun RonovaSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = ""
) {
    androidx.compose.material3.Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier.then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
        colors = androidx.compose.material3.SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = RonovaGoldPrimary,
            checkedBorderColor = RonovaGoldDark,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color(0xFFE0E0E0),
            uncheckedBorderColor = Color(0xFFBDBDBD)
        )
    )
}

@Composable
fun RonovaGlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = RonovaCardBorder.copy(alpha = 0.6f),
    elevation: androidx.compose.ui.unit.Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            content = content
        )
    }
}

