package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.RonovaGlassButton
import com.example.ui.components.RonovaTopBar
import com.example.ui.theme.RonovaCardBorder
import com.example.ui.theme.RonovaError
import com.example.ui.theme.RonovaGoldDark
import com.example.ui.theme.RonovaGoldLight
import com.example.ui.theme.RonovaGoldPrimary
import com.example.ui.theme.RonovaGoldSecondary
import com.example.ui.theme.RonovaPinkContainer
import com.example.ui.theme.RonovaPinkDark
import com.example.ui.theme.RonovaPinkLight
import com.example.ui.theme.RonovaPinkPrimary
import com.example.ui.theme.RonovaPinkSecondary
import com.example.ui.theme.RonovaSuccess
import com.example.ui.theme.RonovaTextMuted
import com.example.ui.theme.RonovaTextPrimary
import com.example.ui.theme.RonovaTextSecondary
import com.example.viewmodel.RonovaViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * User Profile Screen (PRD v2.1 Section 6 & 7)
 * Displays user identity, Ronova ID, bio, and settings.
 */
@Composable
fun UserProfileScreen(
    viewModel: RonovaViewModel,
    onBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val myProfile by viewModel.myProfile.collectAsState()
    val customNickname by viewModel.customNickname.collectAsState()
    val radarVisibility by viewModel.radarVisibility.collectAsState()
    val showDeviceName by viewModel.showDeviceName.collectAsState()
    val allowIncomingCalls by viewModel.allowIncomingCalls.collectAsState()

    var nicknameInput by remember(customNickname) { mutableStateOf(customNickname) }

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
                title = stringResource(R.string.user_profile_title),
                subtitle = stringResource(R.string.user_profile_subtitle),
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Profile Card showing Firestore profile
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RonovaCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!myProfile?.photoUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = myProfile?.photoUrl,
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, Color(0xFFEAB308), CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFEF9C3))
                                            .border(2.dp, Color(0xFFEAB308), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (myProfile?.displayName ?: nicknameInput).take(1).ifBlank { "R" }.uppercase(),
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFCA8A04)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = myProfile?.displayName?.ifBlank { null } ?: nicknameInput.ifEmpty { "Ronova Member" },
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = RonovaTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = myProfile?.ronovaId ?: viewModel.myDeviceId,
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCA8A04), fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }

                            if (!myProfile?.bio.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = myProfile?.bio ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextPrimary, lineHeight = 18.sp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = onNavigateToEditProfile,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("user_profile_edit_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEAB308),
                                    contentColor = Color(0xFF1F2937)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit Profile & Photo", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            }
                        }
                    }
                }

                // Custom Nickname Edit
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RonovaCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.profile_nickname_title),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = nicknameInput,
                                onValueChange = { nicknameInput = it },
                                placeholder = { Text(stringResource(R.string.profile_nickname_hint)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            RonovaGlassButton(
                                text = stringResource(R.string.profile_save_btn),
                                onClick = {
                                    viewModel.setCustomNickname(nicknameInput.trim())
                                    Toast.makeText(context, context.getString(R.string.settings_saved_toast), Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "save_nickname_btn"
                            )
                        }
                    }
                }

                // Privacy & Radar Toggles
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RonovaCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.settings_security_section),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RonovaGoldDark
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Show device name toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.profile_show_name_toggle),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = stringResource(R.string.profile_show_name_desc),
                                        style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted)
                                    )
                                }
                                Switch(
                                    checked = showDeviceName,
                                    onCheckedChange = { viewModel.setShowDeviceName(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = RonovaTextPrimary,
                                        checkedTrackColor = RonovaGoldPrimary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Radar visibility toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.profile_radar_visibility_toggle),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = stringResource(R.string.profile_radar_visibility_desc),
                                        style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted)
                                    )
                                }
                                Switch(
                                    checked = radarVisibility,
                                    onCheckedChange = { viewModel.setRadarVisibility(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = RonovaTextPrimary,
                                        checkedTrackColor = RonovaGoldPrimary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Allow incoming calls toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.profile_allow_calls_toggle),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = stringResource(R.string.profile_allow_calls_desc),
                                        style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted)
                                    )
                                }
                                Switch(
                                    checked = allowIncomingCalls,
                                    onCheckedChange = { viewModel.setAllowIncomingCalls(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = RonovaTextPrimary,
                                        checkedTrackColor = RonovaGoldPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Profile Lock Screen (PRD v2.1 Section 6 & 7)
 * Enables profile locking and private authorization credential rotation.
 */
@Composable
fun ProfileLockScreen(
    viewModel: RonovaViewModel,
    onBack: () -> Unit,
    onNavigateToApprovedContacts: () -> Unit
) {
    val context = LocalContext.current
    val isLocked by viewModel.isProfileLocked.collectAsState()
    val authCredential by viewModel.authCredential.collectAsState()
    var showRotateDialog by remember { mutableStateOf(false) }

    if (showRotateDialog) {
        AlertDialog(
            onDismissRequest = { showRotateDialog = false },
            title = { Text(stringResource(R.string.profile_lock_rotate_confirm_title)) },
            text = { Text(stringResource(R.string.profile_lock_rotate_confirm_desc)) },
            confirmButton = {
                Button(
                    onClick = {
                        showRotateDialog = false
                        val newCode = viewModel.rotateAuthCredential()
                        Toast.makeText(context, "New Code: $newCode", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RonovaGoldPrimary, contentColor = RonovaTextPrimary)
                ) {
                    Text(stringResource(R.string.profile_lock_rotate_btn))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRotateDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

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
                title = stringResource(R.string.profile_lock_title),
                subtitle = stringResource(R.string.profile_lock_subtitle),
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Profile Lock Toggle Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RonovaCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.profile_lock_toggle),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.profile_lock_toggle_desc),
                                        style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted)
                                    )
                                }
                                Switch(
                                    checked = isLocked,
                                    onCheckedChange = { viewModel.setProfileLocked(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = RonovaTextPrimary,
                                        checkedTrackColor = RonovaGoldPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                // Private Authorization Code Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RonovaCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.profile_lock_credential_title),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.profile_lock_credential_desc),
                                style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                color = RonovaGoldLight,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = authCredential,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = RonovaTextPrimary
                                    )
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("AuthCredential", authCredential)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, context.getString(R.string.profile_lock_copied), Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = stringResource(R.string.profile_lock_copy_btn),
                                            tint = RonovaGoldDark
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = { showRotateDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.profile_lock_rotate_btn))
                            }
                        }
                    }
                }

                // Link to Approved Contacts
                item {
                    RonovaGlassButton(
                        text = stringResource(R.string.approved_contacts_title),
                        onClick = onNavigateToApprovedContacts,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "nav_approved_contacts_btn"
                    )
                }
            }
        }
    }
}

/**
 * Approved Contacts Screen (PRD v2.1 Section 6 & 7)
 * Displays list of authorized nodes and allows adding or revoking peers.
 */
@Composable
fun ApprovedContactsScreen(
    viewModel: RonovaViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val approvedContacts by viewModel.approvedContacts.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var peerIdInput by remember { mutableStateOf("") }
    var codeInput by remember { mutableStateOf("") }
    var nicknameInput by remember { mutableStateOf("") }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(stringResource(R.string.approved_contacts_add_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.approved_contacts_add_dialog_desc),
                        style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted)
                    )
                    OutlinedTextField(
                        value = peerIdInput,
                        onValueChange = { peerIdInput = it },
                        label = { Text(stringResource(R.string.approved_contacts_peer_id_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it },
                        label = { Text(stringResource(R.string.approved_contacts_code_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = nicknameInput,
                        onValueChange = { nicknameInput = it },
                        label = { Text(stringResource(R.string.approved_contacts_nickname_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pid = peerIdInput.trim()
                        val code = codeInput.trim()
                        val nick = nicknameInput.trim().ifEmpty { pid }
                        if (pid.isNotEmpty() && code.isNotEmpty()) {
                            coroutineScope.launch {
                                viewModel.repository.saveApprovedContact(pid, nick, code)
                            }
                            showAddDialog = false
                            peerIdInput = ""
                            codeInput = ""
                            nicknameInput = ""
                            Toast.makeText(context, context.getString(R.string.settings_saved_toast), Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RonovaGoldPrimary, contentColor = RonovaTextPrimary)
                ) {
                    Text(stringResource(R.string.dialog_confirm))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

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
                title = stringResource(R.string.approved_contacts_title),
                subtitle = stringResource(R.string.approved_contacts_subtitle),
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.approved_contacts_add_btn))
                    }
                }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (approvedContacts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RonovaCardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = RonovaGoldDark,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = stringResource(R.string.approved_contacts_empty),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.approved_contacts_empty_desc),
                                    style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                RonovaGlassButton(
                                    text = stringResource(R.string.approved_contacts_add_btn),
                                    onClick = { showAddDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "add_first_approved_contact_btn"
                                )
                            }
                        }
                    }
                } else {
                    items(approvedContacts, key = { it.contactId }) { contact ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RonovaCardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(RonovaGoldLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = RonovaGoldDark,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = contact.nickname,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = contact.contactId,
                                        style = MaterialTheme.typography.bodySmall.copy(color = RonovaTextMuted)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.repository.revokeApprovedContact(contact.contactId)
                                            Toast.makeText(context, context.getString(R.string.approved_contacts_revoked_toast), Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.approved_contacts_revoke_btn),
                                        tint = RonovaError
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

/**
 * Terms and Conditions Screen (PRD v2.1 Section 10)
 */
@Composable
fun TermsAndConditionsScreen(
    onBack: () -> Unit
) {
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
                title = stringResource(R.string.terms_title),
                subtitle = stringResource(R.string.terms_subtitle),
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RonovaCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Ronova Mesh Terms of Service (v2.1)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "1. Decentralized & Sovereign Service:\nRonova Mesh operates peer-to-peer over Bluetooth mesh and optional mobile internet. Communication is end-to-end encrypted and does not rely on centralized accounts.\n\n" +
                                        "2. No Subscription & Zero Ads:\nThis application is provided completely free of charge. No advertising trackers, paid APIs, or commercial subscriptions are included.\n\n" +
                                        "3. Multi-hop Relaying:\nWhen enabled, participating devices forward encrypted packets on behalf of peers up to 5 hops without being able to decrypt content.\n\n" +
                                        "4. Ownership & Contact:\nDeveloped and maintained by Md Rony Ahmed. Support email: ronovaltd@gmail.com.",
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp, color = RonovaTextSecondary)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Rules of Use Screen (PRD v2.1 Section 10)
 */
@Composable
fun RulesOfUseScreen(
    onBack: () -> Unit
) {
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
                title = stringResource(R.string.rules_title),
                subtitle = stringResource(R.string.rules_subtitle),
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RonovaCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Community Guidelines & Responsible Use",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "1. Respect for Peer Nodes:\nDo not attempt denial-of-service, flood packets, or degrade intermediate mesh nodes.\n\n" +
                                        "2. Privacy & Consent:\nUse the Profile Lock feature to protect your communications. Only share authorization credentials with trusted parties.\n\n" +
                                        "3. Safety and Emergency Usage:\nWhile Ronova Mesh is resilient during network outages, users should also maintain standard emergency contact mechanisms where available.\n\n" +
                                        "4. Zero Harassment:\nBlocked devices are permanently enforced at packet, relay, and call levels.",
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp, color = RonovaTextSecondary)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Help and Support Screen (PRD v2.1 Section 10)
 */
@Composable
fun HelpAndSupportScreen(
    onBack: () -> Unit
) {
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
                title = stringResource(R.string.help_title),
                subtitle = stringResource(R.string.help_subtitle),
                onBack = onBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // FAQ 1: Offline messaging
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RonovaCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.help_faq_offline_title),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.help_faq_offline_desc),
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp, color = RonovaTextSecondary)
                            )
                        }
                    }
                }

                // FAQ 2: Multi-hop relay
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RonovaCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.help_faq_relay_title),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.help_faq_relay_desc),
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp, color = RonovaTextSecondary)
                            )
                        }
                    }
                }

                // FAQ 3: Internet transport
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RonovaCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.help_faq_internet_title),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.help_faq_internet_desc),
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp, color = RonovaTextSecondary)
                            )
                        }
                    }
                }

                // FAQ 4: Profile lock
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RonovaCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.help_faq_profile_lock_title),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.help_faq_profile_lock_desc),
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp, color = RonovaTextSecondary)
                            )
                        }
                    }
                }

                // Developer & Support Info
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RonovaCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.help_contact_support_title),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RonovaGoldDark
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.help_contact_support_desc),
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp, color = RonovaTextSecondary)
                            )
                        }
                    }
                }
            }
        }
    }
}
