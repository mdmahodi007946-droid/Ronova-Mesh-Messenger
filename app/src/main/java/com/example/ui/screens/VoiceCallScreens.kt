package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PhoneCallback
import androidx.compose.material.icons.automirrored.filled.PhoneForwarded
import androidx.compose.material.icons.automirrored.filled.PhoneMissed
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.core.audio.CallState
import com.example.core.database.CallHistoryEntity
import com.example.core.permission.PermissionManager
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.RonovaTopBar
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
import com.example.viewmodel.RonovaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OutgoingVoiceCallScreen(
    deviceId: String,
    viewModel: RonovaViewModel,
    onBack: () -> Unit
) {
    val call by viewModel.currentCall.collectAsState()
    val device by viewModel.repository.getDeviceByIdFlow(deviceId).collectAsState(initial = null)
    val friends by viewModel.friends.collectAsState()
    val friendUser = friends.find { it.uid == deviceId || it.ronovaId == deviceId }

    val peerDisplayName = friendUser?.displayName ?: device?.displayName ?: deviceId
    val peerSubtitle = friendUser?.ronovaId ?: deviceId

    LaunchedEffect(call.state) {
        if (call.state == CallState.ENDED || call.state == CallState.FAILED || call.state == CallState.IDLE) {
            onBack()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "outgoing_pulse")
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 90f,
        targetValue = 130f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "outgoing_scale"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = stringResource(R.string.call_outgoing_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = RonovaPinkDark,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = peerDisplayName,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = RonovaTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = peerSubtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCA8A04), fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.call_signalling),
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFCA8A04))
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(pulseSize.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF9C3).copy(alpha = 0.6f))
                )
                if (!friendUser?.photoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = friendUser?.photoUrl,
                        contentDescription = "Contact Photo",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFFEAB308), CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEAB308)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = Color(0xFF1F2937),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        viewModel.hangupVoiceCall()
                        onBack()
                    },
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(RonovaError)
                        .testTag("cancel_outgoing_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = stringResource(R.string.call_cancel),
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.call_end),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = RonovaTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun IncomingVoiceCallScreen(
    deviceId: String,
    viewModel: RonovaViewModel,
    onBack: () -> Unit
) {
    val call by viewModel.currentCall.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val friendUser = friends.find { it.uid == deviceId || it.ronovaId == deviceId }
    val peerDisplayName = friendUser?.displayName ?: call.peerName.ifEmpty { deviceId }
    val peerSubtitle = friendUser?.ronovaId ?: deviceId

    val context = LocalContext.current
    var showMicRationale by remember { mutableStateOf(false) }

    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.acceptVoiceCall()
    }

    LaunchedEffect(call.state) {
        if (call.state == CallState.ENDED || call.state == CallState.FAILED || call.state == CallState.IDLE) {
            onBack()
        }
    }

    if (showMicRationale) {
        AlertDialog(
            onDismissRequest = {
                showMicRationale = false
                viewModel.acceptVoiceCall()
            },
            title = { Text(stringResource(R.string.perm_mic_required_title)) },
            text = { Text(stringResource(R.string.perm_mic_required_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        showMicRationale = false
                        micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAB308), contentColor = Color(0xFF1F2937))
                ) {
                    Text(stringResource(R.string.perm_grant_button))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showMicRationale = false
                        viewModel.acceptVoiceCall()
                    }
                ) {
                    Text(stringResource(R.string.dialog_close))
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = stringResource(R.string.call_incoming_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFFCA8A04),
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = peerDisplayName,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = RonovaTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = peerSubtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCA8A04), fontWeight = FontWeight.SemiBold)
                )
            }

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!friendUser?.photoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = friendUser?.photoUrl,
                        contentDescription = "Caller Photo",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color(0xFFEAB308), CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF9C3))
                            .border(3.dp, Color(0xFFEAB308), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = Color(0xFFCA8A04),
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            viewModel.rejectVoiceCall()
                            onBack()
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(RonovaError)
                            .testTag("reject_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = stringResource(R.string.call_decline),
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.call_decline), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            if (PermissionManager.hasMicrophonePermission(context)) {
                                viewModel.acceptVoiceCall()
                            } else {
                                showMicRationale = true
                            }
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(RonovaSuccess)
                            .testTag("accept_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = stringResource(R.string.call_accept),
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.call_accept), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ActiveVoiceCallScreen(
    deviceId: String,
    viewModel: RonovaViewModel,
    onBack: () -> Unit
) {
    val call by viewModel.currentCall.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(call.state) {
        if (call.state == CallState.ENDED || call.state == CallState.FAILED || call.state == CallState.IDLE) {
            onBack()
        }
    }

    val minutes = call.durationSeconds / 60
    val seconds = call.durationSeconds % 60
    val durationText = "%02d:%02d".format(minutes, seconds)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1E121B)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.call_active_title),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = call.peerName.ifEmpty { deviceId },
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = RonovaPinkSecondary,
                        fontWeight = FontWeight.Bold
                    )
                )

                if (!call.hasMicPermission) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = RonovaError.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { PermissionManager.openAppSettings(context) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = RonovaPinkLight, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.call_mic_denied),
                                color = RonovaPinkLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Real-time Audio Waveform Visualizer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AudioWaveformVisualizer(
                    amplitude = call.currentAudioAmplitude,
                    isMuted = call.isMuted,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = when {
                        !call.hasMicPermission -> stringResource(R.string.call_mic_denied)
                        call.isMuted -> stringResource(R.string.call_mic_muted)
                        else -> stringResource(R.string.call_mic_live)
                    },
                    color = when {
                        !call.hasMicPermission -> RonovaError
                        call.isMuted -> Color.Gray
                        else -> RonovaSuccess
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Live Call Stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1927)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.call_sent_packets), fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("${call.packetsSent}", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.call_recv_packets), fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("${call.packetsReceived}", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.call_est_latency), fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("${call.estimatedLatencyMs} ms", fontWeight = FontWeight.Bold, color = RonovaPinkSecondary)
                    }
                }
            }

            // Control Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.toggleMute() },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (call.isMuted) Color.White else Color(0xFF3B2433))
                        .testTag("toggle_mute_button")
                ) {
                    Icon(
                        imageVector = if (call.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (call.isMuted) stringResource(R.string.call_unmute) else stringResource(R.string.call_mute),
                        tint = if (call.isMuted) RonovaPinkDark else Color.White
                    )
                }

                IconButton(
                    onClick = {
                        viewModel.hangupVoiceCall()
                        onBack()
                    },
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(RonovaError)
                        .testTag("end_active_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = stringResource(R.string.call_end),
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleSpeaker() },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (call.isSpeakerOn) RonovaPinkPrimary else Color(0xFF3B2433))
                        .testTag("toggle_speaker_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = stringResource(R.string.call_speaker),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CallHistoryScreen(
    viewModel: RonovaViewModel,
    onBack: () -> Unit,
    onRedial: (String) -> Unit,
    onMenuClick: (() -> Unit)? = null
) {
    val callHistory by viewModel.callHistory.collectAsState()

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
                title = stringResource(R.string.call_history_title),
                subtitle = stringResource(R.string.call_history_subtitle),
                onBack = if (onMenuClick == null) onBack else null,
                onMenuClick = onMenuClick,
                actions = {
                    if (callHistory.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.call_clear_history),
                            color = RonovaPinkPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clickable { viewModel.clearCallHistory() }
                                .testTag("clear_call_history_button")
                        )
                    }
                }
            )

            if (callHistory.isEmpty()) {
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
                                .background(RonovaPinkLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = RonovaPinkPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = stringResource(R.string.call_history_empty),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.call_history_desc),
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
                    items(callHistory) { call ->
                        CallHistoryRow(
                            call = call,
                            onRedial = { onRedial(call.peerDeviceId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CallHistoryRow(
    call: CallHistoryEntity,
    onRedial: () -> Unit
) {
    val timeFormatted = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
        .format(Date(call.startTime))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, RonovaCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (call.callType) {
                    "INCOMING" -> Icons.AutoMirrored.Filled.PhoneCallback
                    "MISSED" -> Icons.AutoMirrored.Filled.PhoneMissed
                    else -> Icons.AutoMirrored.Filled.PhoneForwarded
                }
                val iconColor = if (call.callType == "MISSED") RonovaError else RonovaSuccess

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = call.peerName,
                        fontWeight = FontWeight.Bold,
                        color = RonovaTextPrimary
                    )
                    Text(
                        text = "$timeFormatted • ${call.durationSeconds}s • ${call.callResult}",
                        fontSize = 11.sp,
                        color = RonovaTextMuted
                    )
                }
            }

            IconButton(onClick = onRedial) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = stringResource(R.string.call_redial),
                    tint = RonovaPinkPrimary
                )
            }
        }
    }
}
