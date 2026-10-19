package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.PhoneForwarded
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.audio.CallState
import com.example.core.database.CallHistoryEntity
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.RonovaTopBar
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
                    text = "Outgoing Offline Voice Call",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = RonovaPinkDark,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = device?.displayName ?: deviceId,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = RonovaTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = deviceId,
                    style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Signalling over Bluetooth RFCOMM...",
                    style = MaterialTheme.typography.bodyMedium.copy(color = RonovaPinkPrimary)
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
                        .background(RonovaPinkContainer.copy(alpha = 0.5f))
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(RonovaPinkPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
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
                        contentDescription = "Cancel Call",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "End Call",
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

    LaunchedEffect(call.state) {
        if (call.state == CallState.ENDED || call.state == CallState.FAILED || call.state == CallState.IDLE) {
            onBack()
        }
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
                    text = "Incoming Offline Voice Call",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = RonovaPinkDark,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = call.peerName.ifEmpty { deviceId },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = RonovaTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = deviceId,
                    style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted)
                )
            }

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(RonovaPinkLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = RonovaPinkPrimary,
                    modifier = Modifier.size(52.dp)
                )
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
                            contentDescription = "Decline",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Decline", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            viewModel.acceptVoiceCall()
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(RonovaSuccess)
                            .testTag("accept_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Accept",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Accept", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                    text = "Encrypted Voice Stream (16kHz)",
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
                    text = if (call.isMuted) "Microphone Muted" else "Microphone Live",
                    color = if (call.isMuted) Color.Gray else RonovaSuccess,
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
                        Text("Sent", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("${call.packetsSent}", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Received", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("${call.packetsReceived}", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Est. Latency", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
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
                        contentDescription = "Mute",
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
                        contentDescription = "End Call",
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
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Speaker",
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
    onRedial: (String) -> Unit
) {
    val callHistory by viewModel.callHistory.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAFAFA)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            RonovaTopBar(
                title = "Call History",
                subtitle = "Offline Bluetooth Voice Records",
                onBack = onBack,
                actions = {
                    if (callHistory.isNotEmpty()) {
                        Text(
                            text = "Clear",
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
                            text = "No call logs yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Direct Bluetooth calls will appear here once placed.",
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (call.callType) {
                    "INCOMING" -> Icons.Default.PhoneCallback
                    "MISSED" -> Icons.Default.PhoneMissed
                    else -> Icons.Default.PhoneForwarded
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
                    contentDescription = "Redial",
                    tint = RonovaPinkPrimary
                )
            }
        }
    }
}
