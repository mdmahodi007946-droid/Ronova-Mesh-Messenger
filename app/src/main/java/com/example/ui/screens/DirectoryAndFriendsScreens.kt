package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import com.example.viewmodel.DirectoryUiState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.core.firebase.FriendRequest
import com.example.core.firebase.PresenceStatus
import com.example.core.firebase.UserProfile
import com.example.ui.theme.RonovaCardBorder
import com.example.ui.theme.RonovaDarkSurface
import com.example.ui.theme.RonovaGoldDark
import com.example.ui.theme.RonovaGoldLight
import com.example.ui.theme.RonovaGreenAccent
import com.example.ui.theme.RonovaPinkPrimary
import com.example.ui.theme.RonovaRedAccent
import com.example.ui.theme.RonovaSuccess
import com.example.ui.theme.RonovaTextMuted
import com.example.ui.theme.RonovaTextPrimary
import com.example.viewmodel.RonovaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 1. DirectoryScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectoryScreen(
    viewModel: RonovaViewModel,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToRequests: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val directoryUiState by viewModel.directoryUiState.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val sentRequests by viewModel.sentRequests.collectAsState()
    val receivedRequests by viewModel.receivedRequests.collectAsState()
    val myProfile by viewModel.myProfile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.directory_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = RonovaTextPrimary
                        )
                        Text(
                            text = stringResource(R.string.directory_subtitle),
                            fontSize = 11.5.sp,
                            color = RonovaTextMuted
                        )
                    }
                },
                navigationIcon = if (onBack != null) {
                    {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = RonovaTextPrimary
                            )
                        }
                    }
                } else {
                    {}
                },
                actions = {
                    if (onNavigateToRequests != null) {
                        IconButton(
                            onClick = onNavigateToRequests,
                            modifier = Modifier.testTag("action_directory_requests")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (receivedRequests.isNotEmpty()) {
                                        Badge(
                                            containerColor = RonovaPinkPrimary,
                                            contentColor = Color.White
                                        ) {
                                            Text(
                                                text = "${receivedRequests.size}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassTop,
                                    contentDescription = stringResource(R.string.requests_title),
                                    tint = RonovaGoldDark
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar with leading icon & trailing clear button
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setDirectorySearchQuery(it) },
                placeholder = { Text(stringResource(R.string.directory_search_hint), fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = RonovaTextMuted)
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { viewModel.clearDirectorySearchQuery() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = RonovaTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("directory_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RonovaPinkPrimary,
                    unfocusedBorderColor = RonovaCardBorder,
                    focusedTextColor = RonovaTextPrimary,
                    unfocusedTextColor = RonovaTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (val state = directoryUiState) {
                is DirectoryUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = RonovaPinkPrimary,
                                modifier = Modifier.size(38.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Searching member directory...",
                                color = RonovaTextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                is DirectoryUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = RonovaRedAccent,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Unable to load directory",
                                fontWeight = FontWeight.Bold,
                                color = RonovaTextPrimary,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.message,
                                color = RonovaTextMuted,
                                fontSize = 12.5.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.retryDirectorySearch() },
                                colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is DirectoryUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Icon(
                                imageVector = if (state.isSearch) Icons.Default.Search else Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = RonovaTextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (state.isSearch) stringResource(R.string.directory_empty) else "No members available yet",
                                fontWeight = FontWeight.Bold,
                                color = RonovaTextPrimary,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (state.isSearch)
                                    stringResource(R.string.directory_empty_desc)
                                else
                                    "Registered Ronova Mesh members will appear here automatically.",
                                color = RonovaTextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            if (state.isSearch) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { viewModel.clearDirectorySearchQuery() },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Clear Search", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                is DirectoryUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.users, key = { it.uid }) { user ->
                            val isMe = (user.uid == myProfile?.uid)
                            val isFriend = friends.any { it.uid == user.uid }
                            val sentReq = sentRequests.find { it.recipientUid == user.uid }
                            val receivedReq = receivedRequests.find { it.senderUid == user.uid }

                            DirectoryUserCard(
                                user = user,
                                isMe = isMe,
                                isFriend = isFriend,
                                isRequestSent = (sentReq != null),
                                sentRequestId = sentReq?.id,
                                receivedRequest = receivedReq,
                                onCardClick = { onNavigateToProfile(user.uid) },
                                onAddFriend = { viewModel.sendFriendRequest(user) },
                                onCancelRequest = { reqId -> viewModel.cancelFriendRequest(reqId) },
                                onAcceptFriend = { req -> viewModel.acceptFriendRequest(req) },
                                onChat = { onNavigateToChat(user.uid) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DirectoryUserCard(
    user: UserProfile,
    isMe: Boolean = false,
    isFriend: Boolean,
    isRequestSent: Boolean,
    sentRequestId: String? = null,
    receivedRequest: FriendRequest?,
    onCardClick: () -> Unit,
    onAddFriend: () -> Unit,
    onCancelRequest: ((String) -> Unit)? = null,
    onAcceptFriend: (FriendRequest) -> Unit,
    onChat: () -> Unit
) {
    val (presenceStatus, presenceText) = user.resolvePresence()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("directory_card_${user.ronovaId}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, RonovaCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with online status dot
            Box {
                if (user.photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .border(1.dp, RonovaPinkPrimary, CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(RonovaPinkPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.displayName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = RonovaPinkPrimary,
                            fontSize = 18.sp
                        )
                    }
                }

                // Presence dot
                if (user.presencePrivacy != "nobody") {
                    val dotColor = when (presenceStatus) {
                        PresenceStatus.ONLINE -> RonovaSuccess
                        PresenceStatus.OFFLINE -> RonovaRedAccent
                        PresenceStatus.UNKNOWN -> RonovaTextMuted
                    }
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(dotColor)
                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.displayName,
                    fontWeight = FontWeight.Bold,
                    color = RonovaTextPrimary,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.ronovaId,
                        color = RonovaGoldDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (user.presencePrivacy != "nobody") {
                        Text(
                            text = " • $presenceText",
                            color = RonovaTextMuted,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
                if (user.bio.isNotEmpty()) {
                    Text(
                        text = user.bio,
                        color = RonovaTextMuted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Button
            when {
                isMe -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF9C3),
                        border = BorderStroke(1.dp, Color(0xFFEAB308))
                    ) {
                        Text(
                            text = "You",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCA8A04),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                isFriend -> {
                    IconButton(
                        onClick = onChat,
                        modifier = Modifier.testTag("action_chat_${user.ronovaId}")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = stringResource(R.string.action_chat),
                            tint = RonovaPinkPrimary
                        )
                    }
                }
                receivedRequest != null -> {
                    Button(
                        onClick = { onAcceptFriend(receivedRequest) },
                        colors = ButtonDefaults.buttonColors(containerColor = RonovaSuccess),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("action_accept_${user.ronovaId}")
                    ) {
                        Text(stringResource(R.string.friend_request_accept), fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                isRequestSent -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = stringResource(R.string.friend_request_sent),
                                fontSize = 11.sp,
                                color = RonovaTextMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                        if (sentRequestId != null && onCancelRequest != null) {
                            IconButton(
                                onClick = { onCancelRequest(sentRequestId) },
                                modifier = Modifier
                                    .size(28.dp)
                                    .padding(start = 2.dp)
                                    .testTag("cancel_req_${user.ronovaId}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel request",
                                    tint = RonovaRedAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                else -> {
                    Button(
                        onClick = onAddFriend,
                        colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("action_add_${user.ronovaId}")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.friend_request_send), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// 2. FriendsListScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsListScreen(
    viewModel: RonovaViewModel,
    onNavigateToChat: (String) -> Unit,
    onVoiceCall: (String) -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val friends by viewModel.friends.collectAsState()
    var friendToRemove by remember { mutableStateOf<UserProfile?>(null) }

    if (friendToRemove != null) {
        val target = friendToRemove!!
        AlertDialog(
            onDismissRequest = { friendToRemove = null },
            title = { Text(stringResource(R.string.action_unfriend)) },
            text = { Text(stringResource(R.string.unfriend_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeFriend(target.uid)
                        friendToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RonovaRedAccent)
                ) {
                    Text(stringResource(R.string.action_unfriend))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { friendToRemove = null }) {
                    Text(stringResource(R.string.dialog_close))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.friends_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = RonovaTextPrimary
                        )
                        Text(
                            text = "${friends.size} ${stringResource(R.string.friends_subtitle)}",
                            fontSize = 11.5.sp,
                            color = RonovaTextMuted
                        )
                    }
                },
                navigationIcon = if (onBack != null) {
                    {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = RonovaTextPrimary
                            )
                        }
                    }
                } else {
                    {}
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (friends.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = RonovaTextMuted,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.friends_empty),
                        fontWeight = FontWeight.Bold,
                        color = RonovaTextPrimary,
                        fontSize = 16.sp
                    )
                    Text(
                        text = stringResource(R.string.friends_empty_desc),
                        color = RonovaTextMuted,
                        fontSize = 12.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 36.dp, vertical = 6.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(friends, key = { it.uid }) { friend ->
                    val (presenceStatus, presenceLabel) = friend.resolvePresence()
                    val presenceColor = when (presenceStatus) {
                        PresenceStatus.ONLINE -> RonovaSuccess
                        PresenceStatus.OFFLINE -> RonovaRedAccent
                        PresenceStatus.UNKNOWN -> RonovaTextMuted
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToProfile(friend.uid) }
                            .testTag("friend_card_${friend.ronovaId}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, RonovaCardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box {
                                if (friend.photoUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = friend.photoUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, RonovaPinkPrimary, CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(RonovaPinkPrimary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = friend.displayName.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = RonovaPinkPrimary,
                                            fontSize = 20.sp
                                        )
                                    }
                                }

                                // Online presence indicator
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(presenceColor)
                                        .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = friend.displayName,
                                    fontWeight = FontWeight.Bold,
                                    color = RonovaTextPrimary,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = friend.ronovaId,
                                    color = RonovaGoldDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text = presenceLabel,
                                    color = presenceColor,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (presenceStatus == PresenceStatus.ONLINE) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Chat action
                            IconButton(
                                onClick = { onNavigateToChat(friend.uid) },
                                modifier = Modifier.testTag("friend_chat_${friend.ronovaId}")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = stringResource(R.string.action_chat),
                                    tint = RonovaPinkPrimary
                                )
                            }

                            // Voice call action
                            IconButton(
                                onClick = { onVoiceCall(friend.uid) },
                                modifier = Modifier.testTag("friend_call_${friend.ronovaId}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = stringResource(R.string.action_call),
                                    tint = RonovaGreenAccent
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 3. FriendRequestsScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRequestsScreen(
    viewModel: RonovaViewModel,
    onBack: (() -> Unit)? = null
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val receivedRequests by viewModel.receivedRequests.collectAsState()
    val sentRequests by viewModel.sentRequests.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.requests_title),
                        fontWeight = FontWeight.Bold,
                        color = RonovaTextPrimary
                    )
                },
                navigationIcon = if (onBack != null) {
                    {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = RonovaTextPrimary
                            )
                        }
                    }
                } else {
                    {}
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = RonovaPinkPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "${stringResource(R.string.requests_tab_received)} (${receivedRequests.size})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "${stringResource(R.string.requests_tab_sent)} (${sentRequests.size})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val currentList = if (selectedTab == 0) receivedRequests else sentRequests

            if (currentList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = RonovaTextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.requests_empty),
                            fontWeight = FontWeight.Bold,
                            color = RonovaTextPrimary
                        )
                        Text(
                            text = stringResource(R.string.requests_empty_desc),
                            color = RonovaTextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currentList, key = { it.id }) { req ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("request_card_${req.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, RonovaCardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val name = if (selectedTab == 0) req.senderDisplayName else req.recipientDisplayName
                                val ronovaId = if (selectedTab == 0) req.senderRonovaId else req.recipientRonovaId
                                val photoUrl = if (selectedTab == 0) req.senderPhotoUrl else ""

                                if (photoUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = photoUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(RonovaPinkPrimary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = name.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = RonovaPinkPrimary,
                                            fontSize = 18.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = name,
                                        fontWeight = FontWeight.Bold,
                                        color = RonovaTextPrimary,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = ronovaId,
                                        color = RonovaGoldDark,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (selectedTab == 0) {
                                    // Accept & Reject
                                    Row {
                                        IconButton(
                                            onClick = { viewModel.acceptFriendRequest(req) },
                                            modifier = Modifier.testTag("accept_req_${req.id}")
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.friend_request_accept), tint = RonovaGreenAccent)
                                        }
                                        IconButton(
                                            onClick = { viewModel.rejectFriendRequest(req.id) },
                                            modifier = Modifier.testTag("reject_req_${req.id}")
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.friend_request_reject), tint = RonovaRedAccent)
                                        }
                                    }
                                } else {
                                    // Cancel sent request
                                    OutlinedButton(
                                        onClick = { viewModel.cancelFriendRequest(req.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("cancel_req_${req.id}")
                                    ) {
                                        Text(stringResource(R.string.friend_request_cancel), fontSize = 11.5.sp, color = RonovaRedAccent)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 4. PublicUserProfileScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicUserProfileScreen(
    targetUid: String,
    viewModel: RonovaViewModel,
    onNavigateToChat: (String) -> Unit,
    onNavigateToVoiceCall: (String) -> Unit = {},
    onBack: () -> Unit
) {
    val directoryUsers by viewModel.directoryUsers.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val sentRequests by viewModel.sentRequests.collectAsState()
    val receivedRequests by viewModel.receivedRequests.collectAsState()

    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showRemoveDialog by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }

    LaunchedEffect(targetUid) {
        isLoading = true
        val inFriends = friends.find { it.uid == targetUid }
        val inDirectory = directoryUsers.find { it.uid == targetUid }
        userProfile = inFriends ?: inDirectory ?: viewModel.getUserProfile(targetUid)
        isLoading = false
    }

    val isFriend = friends.any { it.uid == targetUid }
    val incomingReq = receivedRequests.find { it.senderUid == targetUid }
    val outgoingReq = sentRequests.find { it.recipientUid == targetUid }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Remove Friend") },
            text = { Text("Are you sure you want to remove ${userProfile?.displayName ?: "this user"} from your friends list?") },
            confirmButton = {
                Button(
                    onClick = {
                        showRemoveDialog = false
                        viewModel.removeFriend(targetUid)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RonovaRedAccent)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRemoveDialog = false }) {
                    Text(stringResource(R.string.dialog_close))
                }
            }
        )
    }

    if (showBlockDialog) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            title = { Text("Block User") },
            text = { Text("Are you sure you want to block ${userProfile?.displayName ?: "this user"}? You will no longer receive messages or calls from them.") },
            confirmButton = {
                Button(
                    onClick = {
                        showBlockDialog = false
                        viewModel.blockUser(targetUid)
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RonovaRedAccent)
                ) {
                    Text("Block")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBlockDialog = false }) {
                    Text(stringResource(R.string.dialog_close))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(userProfile?.displayName ?: "User Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = RonovaGoldDark)
            }
        } else {
            val user = userProfile
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                if (!user?.photoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = user?.photoUrl,
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .size(108.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color(0xFFEAB308), CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(108.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF9C3))
                            .border(3.dp, Color(0xFFEAB308), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.displayName?.take(1)?.uppercase() ?: "R",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCA8A04)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = user?.displayName ?: "Ronova Member",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = RonovaTextPrimary)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = user?.ronovaId ?: "",
                    style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFFCA8A04), letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Real presence badge
                if (user != null && user.presencePrivacy != "nobody") {
                    val isOnline = user.isOnline
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isOnline) Color(0xFFECFDF5) else Color(0xFFF3F4F6))
                            .border(1.dp, if (isOnline) Color(0xFF10B981) else Color(0xFFD1D5DB), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) Color(0xFF10B981) else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOnline) "Active Now" else "Offline",
                            color = if (isOnline) Color(0xFF047857) else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bio
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "About",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFFCA8A04), fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (!user?.bio.isNullOrBlank()) user?.bio ?: "" else "No bio provided.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = RonovaTextPrimary, lineHeight = 20.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Dynamic Action Buttons
                if (isFriend) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onNavigateToChat(targetUid) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("profile_chat_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAB308), contentColor = Color(0xFF1F2937)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Message", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onNavigateToVoiceCall(targetUid) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("profile_call_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Voice Call", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showRemoveDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("profile_remove_friend_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RonovaRedAccent),
                        border = BorderStroke(1.dp, RonovaRedAccent.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PersonRemove, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Remove Friend", fontWeight = FontWeight.SemiBold)
                    }
                } else if (incomingReq != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.acceptFriendRequest(incomingReq) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("profile_accept_req_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Accept Request", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.rejectFriendRequest(incomingReq.id) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Decline", color = RonovaRedAccent)
                        }
                    }
                } else if (outgoingReq != null) {
                    OutlinedButton(
                        onClick = { viewModel.cancelFriendRequest(outgoingReq.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("profile_cancel_req_btn"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFEAB308))
                    ) {
                        Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Color(0xFFCA8A04))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Friend Request Sent • Cancel", color = Color(0xFFCA8A04), fontWeight = FontWeight.SemiBold)
                    }
                } else if (user != null) {
                    Button(
                        onClick = { viewModel.sendFriendRequest(user) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("profile_add_friend_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAB308), contentColor = Color(0xFF1F2937)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Friend", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Block Option
                OutlinedButton(
                    onClick = { showBlockDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("profile_block_user_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                    border = BorderStroke(1.dp, Color.LightGray),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Block User", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

// 5. EditProfileScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: RonovaViewModel,
    onBack: () -> Unit
) {
    val myProfile by viewModel.myProfile.collectAsState()
    val isInternetAvailable by viewModel.isInternetAvailable.collectAsState()
    val context = LocalContext.current

    var displayName by remember(myProfile) {
        mutableStateOf(myProfile?.displayName ?: "")
    }
    var photoUrl by remember(myProfile) {
        mutableStateOf(myProfile?.photoUrl ?: "")
    }
    var bio by remember(myProfile) {
        mutableStateOf(myProfile?.bio ?: "")
    }
    var profileVisibility by remember(myProfile) {
        mutableStateOf(myProfile?.profileVisibility ?: "public")
    }
    var presencePrivacy by remember(myProfile) {
        mutableStateOf(myProfile?.presencePrivacy ?: "public")
    }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            photoUrl = uri.toString()
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out of Ronova Mesh on this device?") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        viewModel.signOut(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RonovaRedAccent)
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSignOutDialog = false }) {
                    Text(stringResource(R.string.dialog_close))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_edit_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Photo with Picker
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                if (photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFFEAB308), CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF9C3))
                            .border(2.dp, Color(0xFFEAB308), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayName.take(1).ifBlank { "R" }.uppercase(),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCA8A04)
                        )
                    }
                }

                Surface(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = CircleShape,
                    color = Color(0xFFEAB308),
                    shadowElevation = 3.dp,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Change Photo",
                            tint = Color(0xFF1F2937),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Offline Sync Notice
            if (!isInternetAvailable) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Device is offline. Changes will be saved locally and synced to the cloud when connected.",
                        fontSize = 12.sp,
                        color = Color(0xFF78350F),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Ronova ID Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.profile_init_ronova_id_label),
                        style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFFCA8A04), fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = myProfile?.ronovaId ?: "",
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
                    .testTag("edit_profile_name_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFEAB308),
                    unfocusedBorderColor = Color(0xFFD1D5DB)
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
                    .testTag("edit_profile_bio_input"),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFEAB308),
                    unfocusedBorderColor = Color(0xFFD1D5DB)
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.profile_visibility_label),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth()
            )
            listOf(
                "public" to stringResource(R.string.opt_visibility_public),
                "friends_only" to stringResource(R.string.opt_visibility_friends),
                "private" to stringResource(R.string.opt_visibility_private)
            ).forEach { (key, label) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (profileVisibility == key),
                        onClick = { profileVisibility = key },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFEAB308))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label, color = RonovaTextPrimary, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.presence_privacy_label),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth()
            )
            listOf(
                "public" to stringResource(R.string.opt_presence_public),
                "friends_only" to stringResource(R.string.opt_presence_friends),
                "nobody" to stringResource(R.string.opt_presence_nobody)
            ).forEach { (key, label) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (presencePrivacy == key),
                        onClick = { presencePrivacy = key },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFEAB308))
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

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    isSaving = true
                    saveError = null
                    viewModel.updateUserProfile(
                        displayName = displayName.ifBlank { "Ronova Member" },
                        photoUrl = photoUrl,
                        bio = bio,
                        profileVisibility = profileVisibility,
                        presencePrivacy = presencePrivacy,
                        onSuccess = {
                            isSaving = false
                            onBack()
                        },
                        onError = { err ->
                            isSaving = false
                            saveError = err
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("edit_profile_save_btn"),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEAB308),
                    contentColor = Color(0xFF1F2937)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF1F2937), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.profile_edit_save), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Working Sign Out Button in Profile
            OutlinedButton(
                onClick = { showSignOutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("profile_sign_out_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, RonovaRedAccent)
            ) {
                Text(stringResource(R.string.sign_out_btn), color = RonovaRedAccent, fontWeight = FontWeight.Bold)
            }
        }
    }
}
