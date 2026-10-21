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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.example.core.firebase.UserProfile
import com.example.ui.theme.RonovaCardBorder
import com.example.ui.theme.RonovaDarkSurface
import com.example.ui.theme.RonovaGoldLight
import com.example.ui.theme.RonovaGreenAccent
import com.example.ui.theme.RonovaPinkPrimary
import com.example.ui.theme.RonovaRedAccent
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
    onNavigateToChat: (String) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val users by viewModel.directoryUsers.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val sentRequests by viewModel.sentRequests.collectAsState()
    val receivedRequests by viewModel.receivedRequests.collectAsState()

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

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setDirectorySearchQuery(it) },
                placeholder = { Text(stringResource(R.string.directory_search_hint), fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = RonovaTextMuted)
                },
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

            if (users.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = RonovaTextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.directory_empty),
                            fontWeight = FontWeight.Bold,
                            color = RonovaTextPrimary
                        )
                        Text(
                            text = stringResource(R.string.directory_empty_desc),
                            color = RonovaTextMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users, key = { it.uid }) { user ->
                        val isFriend = friends.any { it.uid == user.uid }
                        val isSent = sentRequests.any { it.recipientUid == user.uid }
                        val receivedReq = receivedRequests.find { it.senderUid == user.uid }

                        DirectoryUserCard(
                            user = user,
                            isFriend = isFriend,
                            isRequestSent = isSent,
                            receivedRequest = receivedReq,
                            onCardClick = { onNavigateToProfile(user.uid) },
                            onAddFriend = { viewModel.sendFriendRequest(user) },
                            onAcceptFriend = { req -> viewModel.acceptFriendRequest(req) },
                            onChat = { onNavigateToChat(user.uid) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DirectoryUserCard(
    user: UserProfile,
    isFriend: Boolean,
    isRequestSent: Boolean,
    receivedRequest: FriendRequest?,
    onCardClick: () -> Unit,
    onAddFriend: () -> Unit,
    onAcceptFriend: (FriendRequest) -> Unit,
    onChat: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("directory_card_${user.ronovaId}"),
        colors = CardDefaults.cardColors(containerColor = RonovaDarkSurface),
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
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(if (user.isOnline) RonovaGreenAccent else RonovaTextMuted)
                            .border(1.5.dp, RonovaDarkSurface, CircleShape)
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
                Text(
                    text = user.ronovaId,
                    color = RonovaGoldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
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
                        colors = ButtonDefaults.buttonColors(containerColor = RonovaGreenAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("action_accept_${user.ronovaId}")
                    ) {
                        Text(stringResource(R.string.friend_request_accept), fontSize = 12.sp, color = RonovaDarkSurface, fontWeight = FontWeight.Bold)
                    }
                }
                isRequestSent -> {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("status_sent_${user.ronovaId}")
                    ) {
                        Text(stringResource(R.string.friend_request_sent), fontSize = 11.sp, color = RonovaTextMuted)
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
    onNavigateToProfile: (String) -> Unit
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToProfile(friend.uid) }
                            .testTag("friend_card_${friend.ronovaId}"),
                        colors = CardDefaults.cardColors(containerColor = RonovaDarkSurface),
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
                                        .background(if (friend.isOnline) RonovaGreenAccent else RonovaTextMuted)
                                        .border(1.5.dp, RonovaDarkSurface, CircleShape)
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
                                    color = RonovaGoldLight,
                                    fontSize = 12.sp
                                )

                                val presenceText = if (friend.isOnline) {
                                    stringResource(R.string.presence_status_online)
                                } else if (friend.presenceUpdatedAt != null) {
                                    val date = friend.presenceUpdatedAt.toDate()
                                    val formatted = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(date)
                                    stringResource(R.string.presence_status_last_seen, formatted)
                                } else {
                                    stringResource(R.string.presence_status_offline)
                                }

                                Text(
                                    text = presenceText,
                                    color = if (friend.isOnline) RonovaGreenAccent else RonovaTextMuted,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (friend.isOnline) FontWeight.SemiBold else FontWeight.Normal
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
    viewModel: RonovaViewModel
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
                containerColor = RonovaDarkSurface,
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
                            colors = CardDefaults.cardColors(containerColor = RonovaDarkSurface),
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
                                        color = RonovaGoldLight,
                                        fontSize = 12.sp
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
    onBack: () -> Unit
) {
    val directoryUsers by viewModel.directoryUsers.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val targetUser = directoryUsers.find { it.uid == targetUid } ?: friends.find { it.uid == targetUid }

    val isFriend = friends.any { it.uid == targetUid }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(targetUser?.displayName ?: "Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
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

            if (!targetUser?.photoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = targetUser?.photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(2.dp, RonovaPinkPrimary, CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(RonovaDarkSurface)
                        .border(2.dp, RonovaPinkPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = targetUser?.displayName?.take(1)?.uppercase() ?: "R",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = RonovaPinkPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = targetUser?.displayName ?: "Ronova Member",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = RonovaTextPrimary)
            )

            Text(
                text = targetUser?.ronovaId ?: "",
                style = MaterialTheme.typography.titleMedium.copy(color = RonovaGoldLight, letterSpacing = 1.sp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Presence indicator
            if (targetUser != null && targetUser.presencePrivacy != "nobody") {
                val isOnline = targetUser.isOnline
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(RonovaDarkSurface)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isOnline) RonovaGreenAccent else RonovaTextMuted)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOnline) stringResource(R.string.presence_status_online) else stringResource(R.string.presence_status_offline),
                        color = if (isOnline) RonovaGreenAccent else RonovaTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bio
            if (!targetUser?.bio.isNullOrBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = RonovaDarkSurface),
                    border = BorderStroke(1.dp, RonovaCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.profile_init_bio_label),
                            style = MaterialTheme.typography.labelSmall.copy(color = RonovaGoldLight)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = targetUser?.bio ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(color = RonovaTextPrimary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (isFriend) {
                Button(
                    onClick = { onNavigateToChat(targetUid) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_chat), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            } else if (targetUser != null) {
                Button(
                    onClick = { viewModel.sendFriendRequest(targetUser) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.friend_request_send), fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
    val context = LocalContext.current

    var displayName by remember(myProfile) {
        mutableStateOf(myProfile?.displayName ?: "")
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
    var showSignOutDialog by remember { mutableStateOf(false) }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(stringResource(R.string.sign_out_dialog_title)) },
            text = { Text(stringResource(R.string.sign_out_dialog_desc)) },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        viewModel.signOut(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RonovaRedAccent)
                ) {
                    Text(stringResource(R.string.sign_out_btn))
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
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Ronova ID Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RonovaDarkSurface),
                border = BorderStroke(1.dp, RonovaCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.profile_init_ronova_id_label),
                        style = MaterialTheme.typography.labelMedium.copy(color = RonovaGoldLight)
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

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text(stringResource(R.string.profile_init_display_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RonovaPinkPrimary,
                    unfocusedBorderColor = RonovaCardBorder
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text(stringResource(R.string.profile_init_bio_label)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RonovaPinkPrimary,
                    unfocusedBorderColor = RonovaCardBorder
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.profile_visibility_label),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
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
                        colors = RadioButtonDefaults.colors(selectedColor = RonovaPinkPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label, color = RonovaTextPrimary, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.presence_privacy_label),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
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
                        colors = RadioButtonDefaults.colors(selectedColor = RonovaPinkPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = label, color = RonovaTextPrimary, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    isSaving = true
                    viewModel.updateUserProfile(
                        displayName = displayName.ifBlank { "Ronova Member" },
                        bio = bio,
                        profileVisibility = profileVisibility,
                        presencePrivacy = presencePrivacy,
                        onSuccess = {
                            isSaving = false
                            onBack()
                        },
                        onError = {
                            isSaving = false
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = RonovaPinkPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.profile_edit_save), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sign Out Option
            OutlinedButton(
                onClick = { showSignOutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, RonovaRedAccent)
            ) {
                Text(stringResource(R.string.sign_out_btn), color = RonovaRedAccent, fontWeight = FontWeight.Bold)
            }
        }
    }
}
