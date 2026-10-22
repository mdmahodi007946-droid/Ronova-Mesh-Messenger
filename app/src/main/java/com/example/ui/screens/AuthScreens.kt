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
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.viewinterop.AndroidView
import com.example.viewmodel.RonovaViewModel
import kotlinx.coroutines.launch

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
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
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Background Live Video Player
            AuthVideoBackground(modifier = Modifier.fillMaxSize())

            // Atmospheric gradient overlay for optimal legibility and contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.40f),
                                Color.Black.copy(alpha = 0.55f),
                                Color.Black.copy(alpha = 0.72f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 26.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Glassmorphism App Icon Container
                Box(
                    modifier = Modifier
                        .size(98.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.22f),
                                    Color.White.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .border(
                            BorderStroke(
                                1.5.dp,
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFDE047).copy(alpha = 0.85f),
                                        Color.White.copy(alpha = 0.30f)
                                    )
                                )
                            ),
                            RoundedCornerShape(26.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_image),
                        contentDescription = "Ronova Mesh App Icon",
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Ronova Mesh",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
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
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Offline connectivity warning banner if internet is unavailable
                if (!isInternetAvailable) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFD97706).copy(alpha = 0.25f))
                            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = Color(0xFFFDE047),
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
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFDC2626).copy(alpha = 0.25f))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
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

                // Features overview card with true glass blur effect
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.16f),
                                    Color.White.copy(alpha = 0.07f)
                                )
                            )
                        )
                        .border(
                            BorderStroke(
                                1.2.dp,
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.45f),
                                        Color.White.copy(alpha = 0.10f)
                                    )
                                )
                            ),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.18f))
                                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = Color(0xFFFDE047),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Google Account Identity",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.5.sp
                                )
                                Text(
                                    text = "Fast, passwordless sign-in with your Google account",
                                    color = Color.White.copy(alpha = 0.82f),
                                    fontSize = 11.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.18f))
                                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = Color(0xFF4ADE80),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Cloud Profile & Directory",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.5.sp
                                )
                                Text(
                                    text = "Persistent user search and friend requests across devices",
                                    color = Color.White.copy(alpha = 0.82f),
                                    fontSize = 11.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.18f))
                                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Bluetooth,
                                    contentDescription = null,
                                    tint = Color(0xFF60A5FA),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Decentralized Bluetooth Mesh",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.5.sp
                                )
                                Text(
                                    text = "Off-grid messaging and voice calls when disconnected",
                                    color = Color.White.copy(alpha = 0.82f),
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Glassmorphic "Continue with Google" Button
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
                        .testTag("google_sign_in_button"),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.08f),
                        disabledContentColor = Color.White.copy(alpha = 0.4f)
                    ),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.28f),
                                        Color.White.copy(alpha = 0.14f)
                                    )
                                )
                            )
                            .border(
                                BorderStroke(
                                    1.5.dp,
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.65f),
                                            Color.White.copy(alpha = 0.25f)
                                        )
                                    )
                                ),
                                RoundedCornerShape(18.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
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
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.92f)),
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
