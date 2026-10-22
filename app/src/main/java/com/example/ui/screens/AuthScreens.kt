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
import com.example.viewmodel.RonovaViewModel
import kotlinx.coroutines.launch

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
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
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ronova Mesh Emblem: Bluetooth + Wireless Router + Social Mesh Chat
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEF9C3))
                    .border(2.dp, Color(0xFFEAB308), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_mesh_antenna_chat),
                    contentDescription = "Ronova Mesh Chat Emblem",
                    modifier = Modifier.size(62.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Ronova Mesh",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = RonovaTextPrimary
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Decentralized mesh communications with secure cloud synchronization",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = RonovaTextMuted,
                    lineHeight = 22.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Offline connectivity banner if internet is unavailable
            if (!isInternetAvailable) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Internet connection required for initial Google Sign-In and profile synchronization.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF78350F))
                        )
                    }
                }
            }

            // Error display
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Authentication Error",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color(0xFFB91C1C),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF7F1D1D))
                        )
                    }
                }
            }

            // Features overview card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF9C3)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color(0xFFCA8A04), modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Google Account Identity", fontWeight = FontWeight.SemiBold, color = RonovaTextPrimary, fontSize = 13.sp)
                            Text("Fast, passwordless sign-in with your Google account", color = RonovaTextMuted, fontSize = 11.5.sp)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFECFDF5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Cloud Profile & Directory", fontWeight = FontWeight.SemiBold, color = RonovaTextPrimary, fontSize = 13.sp)
                            Text("Persistent user search and friend requests across devices", color = RonovaTextMuted, fontSize = 11.5.sp)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bluetooth, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Decentralized Bluetooth Mesh", fontWeight = FontWeight.SemiBold, color = RonovaTextPrimary, fontSize = 13.sp)
                            Text("Off-grid messaging and voice calls when disconnected", color = RonovaTextMuted, fontSize = 11.5.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Prominent, Modern "Continue with Google" Button
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
                    .height(54.dp)
                    .testTag("google_sign_in_button"),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1F2937),
                    disabledContainerColor = Color(0xFFF3F4F6),
                    disabledContentColor = Color(0xFF9CA3AF)
                ),
                border = BorderStroke(1.5.dp, Color(0xFFE5E7EB)),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 1.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color(0xFF1F2937),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Signing in with Google...",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color(0xFF1F2937)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continue with Google",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.5.sp,
                        color = Color(0xFF1F2937)
                    )
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
