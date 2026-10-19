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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RonovaTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, RonovaCardBorder.copy(alpha = 0.6f)),
        shadowElevation = 0.5.dp
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 0.2.sp
                        )
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = RonovaTextMuted,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
            },
            navigationIcon = {
                if (onBack != null) {
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
        BluetoothStatus.READY -> Triple(Color(0xFFFFF1F2), RonovaPinkPrimary, "Mesh Ready")
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
                        Modifier.background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(RonovaPinkPrimary, RonovaPinkDark)
                            ),
                            shape = bubbleShape
                        )
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
                    color = if (isOutgoing) Color.White else RonovaTextPrimary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 21.sp,
                        fontFamily = AlataFontFamily
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
                        color = if (isOutgoing) Color.White.copy(alpha = 0.85f) else RonovaTextMuted,
                        fontFamily = AlataFontFamily
                    )
                    if (isOutgoing) {
                        Spacer(modifier = Modifier.width(5.dp))
                        when (deliveryState) {
                            "DELIVERED" -> Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Delivered",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            "RELAYED" -> Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Relayed",
                                tint = Color.White.copy(alpha = 0.95f),
                                modifier = Modifier.size(14.dp)
                            )
                            "SENDING" -> Icon(
                                imageVector = Icons.Default.HourglassEmpty,
                                contentDescription = "Sending",
                                tint = Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.size(14.dp)
                            )
                            "QUEUED" -> Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Queued",
                                tint = Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.size(14.dp)
                            )
                            "FAILED", "EXPIRED" -> Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Failed",
                                tint = Color(0xFFFFCDD2),
                                modifier = Modifier.size(14.dp)
                            )
                            else -> Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Sent",
                                tint = Color.White.copy(alpha = 0.8f),
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

