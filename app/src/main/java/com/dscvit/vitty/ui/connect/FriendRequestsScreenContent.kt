package com.dscvit.vitty.ui.connect

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dscvit.vitty.R
import com.dscvit.vitty.network.api.community.responses.user.UserResponse
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.Green
import com.dscvit.vitty.theme.Red
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRequestsScreenContent(
    onBackClick: () -> Unit = {},
    connectViewModel: ConnectViewModel,
) {
    val context = LocalContext.current

    var processedRequests by remember { mutableStateOf(setOf<String>()) }
    var isLoading by remember { mutableStateOf(true) }

    val friendRequests by connectViewModel.friendRequest.observeAsState()

    val requestActionResponse by connectViewModel.requestActionResponse.observeAsState()

    LaunchedEffect(friendRequests) {
        isLoading = false
    }

    LaunchedEffect(requestActionResponse?.detail) {
        requestActionResponse?.let { response ->
            if (response.detail == "Friend request accepted successfully!" ||
                response.detail == "Friend request rejected successfully"
            ) {
                connectViewModel.clearRequestActionResponse()

                val sharedPreferences = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
                val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
                val username = sharedPreferences.getString(Constants.COMMUNITY_USERNAME, "") ?: ""
                if (token.isNotEmpty()) {
                    connectViewModel.getFriendRequest(token)
                    connectViewModel.getFriendList(token, username)
                }
            }
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background),
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Friend Requests",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_round_chevron_left),
                        contentDescription = "Back",
                        tint = TextColor,
                    )
                }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
        )

        val requests = friendRequests ?: emptyList()
        val pendingRequests = requests.filter { !processedRequests.contains(it.from.username) }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Accent)
            }
        } else if (pendingRequests.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_community_outline),
                        contentDescription = "No requests",
                        modifier = Modifier.size(64.dp),
                        tint = Accent.copy(alpha = 0.5f),
                    )
                    Text(
                        text = "No friend requests",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Accent,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "You're all caught up!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Accent.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(pendingRequests) { request ->
                    FriendRequestCard(
                        user = request.from,
                        onAccept = { user ->
                            val sharedPreferences = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
                            val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
                            if (token.isNotEmpty()) {
                                processedRequests = processedRequests + user.username
                                connectViewModel.acceptRequest(token, user.username)
                            }
                        },
                        onReject = { user ->
                            val sharedPreferences = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
                            val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
                            if (token.isNotEmpty()) {
                                processedRequests = processedRequests + user.username
                                connectViewModel.rejectRequest(token, user.username)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendRequestCard(
    user: UserResponse,
    onAccept: (UserResponse) -> Unit,
    onReject: (UserResponse) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Secondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 20.dp, end = 16.dp, bottom = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (user.picture.isNotEmpty()) {
                    AsyncImage(
                        model = user.picture,
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                        placeholder = painterResource(R.drawable.ic_gdscvit),
                        error = painterResource(R.drawable.ic_gdscvit),
                    )
                } else {
                    Box(
                        modifier =
                            Modifier
                                .size(48.dp)
                                .background(Accent.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text =
                                user.name
                                    .take(2)
                                    .map { it.uppercaseChar() }
                                    .joinToString(""),
                            color = TextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                IconButton(
                    onClick = { onReject(user) },
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Reject",
                        tint = Red,
                    )
                }

                IconButton(
                    onClick = { onAccept(user) },
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Accept",
                        tint = Green,
                    )
                }
            }
        }
    }
}
