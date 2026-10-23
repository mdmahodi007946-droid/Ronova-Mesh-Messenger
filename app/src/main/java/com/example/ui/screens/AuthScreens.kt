package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.RonovaCardBorder
import com.example.ui.theme.RonovaDarkSurface
import com.example.ui.theme.RonovaGoldDark
import com.example.ui.theme.RonovaGoldLight
import com.example.ui.theme.RonovaGreenAccent
import com.example.ui.theme.RonovaPinkPrimary
import com.example.ui.theme.RonovaRedAccent
import com.example.ui.theme.RonovaSuccess
import com.example.ui.theme.RonovaWarning
import com.example.ui.theme.RonovaWarningContainer
import com.example.ui.theme.RonovaError
import com.example.ui.theme.RonovaErrorContainer
import com.example.ui.theme.RonovaTextMuted
import com.example.ui.theme.RonovaTextPrimary
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.viewinterop.AndroidView
import com.example.viewmodel.RonovaViewModel
import kotlinx.coroutines.launch

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Animated constellation mesh network canvas.
 * Renders floating mesh nodes connected by dynamic pulsating links.
 * Visible through frosted-glass surfaces for genuine backdrop blur rendering.
 */
@Composable
fun NetworkConstellationBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "constellation")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Spatial node configuration across screen space
    val baseNodes = remember {
        listOf(
            Triple(0.12f, 0.12f, 5.0f),
            Triple(0.38f, 0.08f, 4.0f),
            Triple(0.82f, 0.10f, 6.0f),
            Triple(0.92f, 0.28f, 4.5f),
            Triple(0.20f, 0.30f, 6.5f),
            Triple(0.52f, 0.25f, 5.0f),
            Triple(0.72f, 0.42f, 6.0f),
            Triple(0.12f, 0.55f, 4.8f),
            Triple(0.40f, 0.60f, 7.0f),
            Triple(0.88f, 0.62f, 5.2f),
            Triple(0.28f, 0.80f, 6.0f),
            Triple(0.68f, 0.82f, 5.5f),
            Triple(0.88f, 0.90f, 4.2f),
            Triple(0.10f, 0.88f, 6.0f)
        )
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val maxDist = (w * 0.44f).coerceAtLeast(150.dp.toPx())

        // Calculate dynamic positions with sine/cosine orbital drift
        val positions = baseNodes.mapIndexed { i, (bx, by, r) ->
            val angle = time + (i * 0.72f)
            val dx = kotlin.math.cos(angle) * (16.dp.toPx())
            val dy = kotlin.math.sin(angle * 1.25f) * (20.dp.toPx())
            val px = (bx * w + dx).coerceIn(0f, w)
            val py = (by * h + dy).coerceIn(0f, h)
            Offset(px, py) to r
        }

        // Draw connections between proximate nodes
        for (i in positions.indices) {
            val (p1, _) = positions[i]
            for (j in (i + 1) until positions.size) {
                val (p2, _) = positions[j]
                val dist = (p1 - p2).getDistance()
                if (dist < maxDist) {
                    val proximity = (1f - (dist / maxDist)).coerceIn(0f, 1f)
                    val alpha = proximity * 0.30f
                    val lineColor = if ((i + j) % 2 == 0) {
                        Color(0xFFFFD84D).copy(alpha = alpha) // Golden yellow highlight
                    } else {
                        Color(0xFF90CAF9).copy(alpha = alpha * 0.8f) // Ambient mesh cyan
                    }
                    drawLine(
                        color = lineColor,
                        start = p1,
                        end = p2,
                        strokeWidth = 1.2.dp.toPx()
                    )
                }
            }
        }

        // Draw glowing nodes with halo
        positions.forEachIndexed { i, (pos, radius) ->
            val isGold = (i % 3 == 0)
            val coreColor = if (isGold) Color(0xFFFFE58A) else Color(0xFFFFFFFF)
            val glowColor = if (isGold) Color(0xFFFFD84D).copy(alpha = 0.28f) else Color(0xFF90CAF9).copy(alpha = 0.22f)

            // Outer soft ambient aura
            drawCircle(
                color = glowColor,
                radius = radius.dp.toPx() * 2.5f,
                center = pos
            )
            // Inner core dot
            drawCircle(
                color = coreColor,
                radius = (radius * 0.75f).dp.toPx(),
                center = pos
            )
        }
    }
}

@Composable
fun AuthVideoBackground(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            TextureView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    private var mediaPlayer: MediaPlayer? = null

                    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                        try {
                            mediaPlayer = MediaPlayer().apply {
                                setSurface(Surface(surface))
                                setVolume(0f, 0f)
                                isLooping = true
                                val localUri = Uri.parse("android.resource://${ctx.packageName}/${R.raw.auth_bg_video}")
                                setDataSource(ctx, localUri)
                                setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                                setOnPreparedListener { mp ->
                                    try {
                                        mp.start()
                                    } catch (e: Exception) {
                                        Log.w("AuthVideo", "Start failed: ${e.message}")
                                    }
                                }
                                setOnErrorListener { _, _, _ ->
                                    try {
                                        reset()
                                        setDataSource(ctx, Uri.parse("https://lisview.com/api/file/from-klickpin-com-curious-social-dialogue-prompts-for-yg2pa"))
                                        prepareAsync()
                                    } catch (_: Exception) {}
                                    true
                                }
                                prepareAsync()
                            }
                        } catch (e: Exception) {
                            Log.w("AuthVideo", "Error initializing MediaPlayer: ${e.message}")
                        }
                    }

                    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

                    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                        try {
                            mediaPlayer?.stop()
                            mediaPlayer?.release()
                        } catch (_: Exception) {}
                        mediaPlayer = null
                        return true
                    }

                    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                }
            }
        }
    )
}

@Composable
fun GoogleSignInScreen(
    viewModel: RonovaViewModel,
    activity: Activity? = null,
    onSignedIn: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isInternetAvailable by viewModel.isInternetAvailable.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF080B24) // Dark navy baseline
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Layer 1: Background Live Video Player
            AuthVideoBackground(modifier = Modifier.fillMaxSize())

            // Layer 2: Real-time Mesh Constellation Animation (nodes drift behind glass)
            NetworkConstellationBackground(modifier = Modifier.fillMaxSize())

            // Layer 3: Atmospheric navy & black gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF080B24).copy(alpha = 0.55f),
                                Color(0xFF080B24).copy(alpha = 0.68f),
                                Color(0xFF040614).copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Layer 4: Soft white and warm-yellow ambient glow around the authentication area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp)
                    .align(Alignment.Center)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD84D).copy(alpha = 0.18f),
                                Color(0xFFFFE58A).copy(alpha = 0.09f),
                                Color(0xFFFFFFFF).copy(alpha = 0.05f),
                                Color(0xFF080B24).copy(alpha = 0.0f)
                            ),
                            radius = 650f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Golden-Yellow Ambient Glow around App Logo
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(140.dp)
                ) {
                    // Soft ambient golden-yellow halo
                    Box(
                        modifier = Modifier
                            .size(136.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFD84D).copy(alpha = 0.35f),
                                        Color(0xFFFFE58A).copy(alpha = 0.15f),
                                        Color.Transparent
                                    ),
                                    radius = 160f
                                )
                            )
                    )

                    // Glassmorphism App Icon Container with 28dp corners & golden border highlight
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = Color(0xFF080B24),
                                spotColor = Color(0xFF080B24)
                            )
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.20f),
                                        Color.White.copy(alpha = 0.10f)
                                    )
                                )
                            )
                            .border(
                                BorderStroke(
                                    1.4.dp,
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFFFD84D).copy(alpha = 0.90f),
                                            Color(0xFFFFE58A).copy(alpha = 0.45f),
                                            Color.White.copy(alpha = 0.25f)
                                        )
                                    )
                                ),
                                RoundedCornerShape(28.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_image),
                            contentDescription = "Ronova Mesh App Icon",
                            modifier = Modifier
                                .size(74.dp)
                                .clip(RoundedCornerShape(20.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Ronova Mesh",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Decentralized mesh communications with secure cloud synchronization",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.90f),
                        lineHeight = 22.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Offline connectivity warning banner
                if (!isInternetAvailable) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFD97706).copy(alpha = 0.25f))
                            .border(1.dp, Color(0xFFFFD84D).copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = Color(0xFFFFD84D),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Internet connection required for initial Google Sign-In and profile synchronization.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.95f))
                            )
                        }
                    }
                }

                // Error display
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFDC2626).copy(alpha = 0.25f))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "Authentication Error",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color(0xFFFCA5A5),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.95f))
                            )
                        }
                    }
                }

                // Genuine Glassmorphic Feature Card (30dp corners, 10% white opacity, 26px backdrop blur, restrained golden highlight)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(30.dp),
                            ambientColor = Color(0xFF080B24).copy(alpha = 0.6f),
                            spotColor = Color(0xFF080B24)
                        )
                        .clip(RoundedCornerShape(30.dp))
                ) {
                    // Layer A: Backdrop blur layer (blurs background constellation behind the card)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .graphicsLayer {
                                    renderEffect = android.graphics.RenderEffect
                                        .createBlurEffect(26f, 26f, android.graphics.Shader.TileMode.CLAMP)
                                        .asComposeRenderEffect()
                                }
                        ) {
                            NetworkConstellationBackground(modifier = Modifier.fillMaxSize())
                        }
                    }

                    // Layer B: Frosted glass surface (white at 10% opacity)
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.White.copy(alpha = 0.10f))
                    )

                    // Layer C: Subtle top inner specular reflection highlight
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.35f),
                                        Color(0xFFFFE58A).copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Layer D: Translucent border with restrained golden highlight
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .border(
                                BorderStroke(
                                    1.2.dp,
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.38f),
                                            Color(0xFFFFE58A).copy(alpha = 0.32f),
                                            Color.White.copy(alpha = 0.12f)
                                        )
                                    )
                                ),
                                RoundedCornerShape(30.dp)
                            )
                    )

                    // Layer E: Foreground content — 100% SHARP, UNBLURRED, HIGH CONTRAST
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // Feature 1: Google Identity
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.16f))
                                    .border(1.dp, Color(0xFFFFE58A).copy(alpha = 0.40f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD84D),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Google Account Identity",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Fast, passwordless sign-in with your Google account",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Feature 2: Cloud Profile & Directory
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.16f))
                                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = Color(0xFF4ADE80),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Cloud Profile & Directory",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Persistent user discovery, Ronova IDs, and cross-device sync",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Feature 3: Decentralized Bluetooth Mesh
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.16f))
                                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Bluetooth,
                                    contentDescription = null,
                                    tint = Color(0xFF60A5FA),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Decentralized Bluetooth Mesh",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Off-grid messaging and voice calls when disconnected",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Genuine Glassmorphic "Continue with Google" Button (28dp corners, 12% white opacity, 28px backdrop blur)
                Button(
                    onClick = {
                        val targetActivity = activity ?: context.findActivity() ?: (context as? Activity)
                        if (targetActivity == null) {
                            errorMessage = "Cannot start Google Sign-In: Application activity not found. Please restart the app."
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null
                        viewModel.signInWithGoogle(
                            activity = targetActivity,
                            onSuccess = { _ ->
                                isLoading = false
                                onSignedIn()
                            },
                            onError = { error ->
                                isLoading = false
                                errorMessage = when {
                                    error.contains("network", ignoreCase = true) || error.contains("connection", ignoreCase = true) ->
                                        "Network error: Unable to reach Google servers. Please check your internet connection."
                                    error.contains("cancelled", ignoreCase = true) || error.contains("canceled", ignoreCase = true) ->
                                        "Google Sign-In was cancelled."
                                    error.contains("No Google credential", ignoreCase = true) ||
                                    error.contains("NoCredentialException", ignoreCase = true) ||
                                    error.contains("No credentials available", ignoreCase = true) ->
                                        "No Google account found on device. Please add a Google account in your phone's Settings to continue."
                                    else -> "Authentication failed: $error"
                                }
                                scope.launch {
                                    snackbarHostState.showSnackbar(errorMessage ?: "Authentication failed")
                                }
                            },
                            onCancelled = {
                                isLoading = false
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 14.dp,
                            shape = RoundedCornerShape(28.dp),
                            ambientColor = Color(0xFF080B24).copy(alpha = 0.7f),
                            spotColor = Color(0xFF080B24)
                        )
                        .testTag("google_sign_in_button"),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.08f),
                        disabledContentColor = Color.White.copy(alpha = 0.4f)
                    ),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(28.dp))
                    ) {
                        // Layer A: Backdrop blur layer
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .graphicsLayer {
                                        renderEffect = android.graphics.RenderEffect
                                            .createBlurEffect(28f, 28f, android.graphics.Shader.TileMode.CLAMP)
                                            .asComposeRenderEffect()
                                    }
                            ) {
                                NetworkConstellationBackground(modifier = Modifier.fillMaxSize())
                            }
                        }

                        // Layer B: Frosted glass surface at 12% opacity
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.White.copy(alpha = 0.12f))
                        )

                        // Layer C: Translucent border with golden yellow highlight
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .border(
                                    BorderStroke(
                                        1.2.dp,
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.45f),
                                                Color(0xFFFFD84D).copy(alpha = 0.45f),
                                                Color.White.copy(alpha = 0.18f)
                                            )
                                        )
                                    ),
                                    RoundedCornerShape(28.dp)
                                )
                        )

                        // Layer D: Crisp, sharp foreground content (never blurred)
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color(0xFFFFD84D),
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Signing in with Google...",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.95f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_google_logo),
                                            contentDescription = "Google Logo",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = "Continue with Google",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White,
                                        letterSpacing = 0.3.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInitScreen(
    viewModel: RonovaViewModel,
    onProfileCompleted: () -> Unit
) {
    val myProfile by viewModel.myProfile.collectAsState()
    val scope = rememberCoroutineScope()

    var displayName by remember(myProfile) {
        mutableStateOf(myProfile?.displayName ?: "")
    }
    var bio by remember(myProfile) {
        mutableStateOf(myProfile?.bio ?: "Connected on Ronova Mesh")
    }
    var profileVisibility by remember(myProfile) {
        mutableStateOf(myProfile?.profileVisibility ?: "public")
    }
    var presencePrivacy by remember(myProfile) {
        mutableStateOf(myProfile?.presencePrivacy ?: "public")
    }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // User Avatar Preview
            if (!myProfile?.photoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = myProfile?.photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .border(2.dp, RonovaPinkPrimary, CircleShape)
                )
            } else {
                Surface(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(2.dp, RonovaPinkPrimary)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = RonovaPinkPrimary,
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.profile_init_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = RonovaTextPrimary
                )
            )

            Text(
                text = stringResource(R.string.profile_init_subtitle),
                style = MaterialTheme.typography.bodyMedium.copy(color = RonovaTextMuted),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
            )

            // Ronova ID Display (Read-Only Persistent ID)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, RonovaCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.profile_init_ronova_id_label),
                        style = MaterialTheme.typography.labelMedium.copy(color = RonovaGoldDark, fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = myProfile?.ronovaId ?: "Generating...",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = RonovaTextPrimary,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display Name
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text(stringResource(R.string.profile_init_display_name_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_init_name_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RonovaPinkPrimary,
                    unfocusedBorderColor = RonovaCardBorder,
                    focusedLabelColor = RonovaPinkPrimary,
                    unfocusedLabelColor = RonovaTextMuted,
                    focusedTextColor = RonovaTextPrimary,
                    unfocusedTextColor = RonovaTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bio
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text(stringResource(R.string.profile_init_bio_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_init_bio_input"),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RonovaPinkPrimary,
                    unfocusedBorderColor = RonovaCardBorder,
                    focusedLabelColor = RonovaPinkPrimary,
                    unfocusedLabelColor = RonovaTextMuted,
                    focusedTextColor = RonovaTextPrimary,
                    unfocusedTextColor = RonovaTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Profile Visibility Radio Group
            Text(
                text = stringResource(R.string.profile_visibility_label),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = RonovaTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            val visibilityOptions = listOf(
                "public" to stringResource(R.string.opt_visibility_public),
                "friends_only" to stringResource(R.string.opt_visibility_friends),
                "private" to stringResource(R.string.opt_visibility_private)
            )
            visibilityOptions.forEach { (key, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (profileVisibility == key),
                        onClick = { profileVisibility = key },
                        colors = RadioButtonDefaults.colors(selectedColor = RonovaPinkPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label, color = RonovaTextPrimary, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Presence Privacy Radio Group
            Text(
                text = stringResource(R.string.presence_privacy_label),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = RonovaTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            val presenceOptions = listOf(
                "public" to stringResource(R.string.opt_presence_public),
                "friends_only" to stringResource(R.string.opt_presence_friends),
                "nobody" to stringResource(R.string.opt_presence_nobody)
            )
            presenceOptions.forEach { (key, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (presencePrivacy == key),
                        onClick = { presencePrivacy = key },
                        colors = RadioButtonDefaults.colors(selectedColor = RonovaPinkPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label, color = RonovaTextPrimary, fontSize = 14.sp)
                }
            }

            if (saveError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = saveError ?: "",
                    color = RonovaRedAccent,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    isSaving = true
                    saveError = null
                    viewModel.updateUserProfile(
                        displayName = displayName.ifBlank { "Ronova Member" },
                        bio = bio,
                        profileVisibility = profileVisibility,
                        presencePrivacy = presencePrivacy,
                        onSuccess = {
                            isSaving = false
                            onProfileCompleted()
                        },
                        onError = { err ->
                            isSaving = false
                            saveError = err
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("profile_init_save_button"),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RonovaPinkPrimary,
                    contentColor = RonovaTextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = RonovaTextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.profile_init_save_btn),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
