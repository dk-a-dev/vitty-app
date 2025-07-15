package com.dscvit.vitty.ui.connect

import android.content.Context
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.dscvit.vitty.R
import com.dscvit.vitty.network.api.community.responses.user.UserResponse
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.ui.community.CommunityViewModel
import com.dscvit.vitty.util.Constants
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendScreenContent(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val communityViewModel: CommunityViewModel = viewModel()

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var sentRequests by remember { mutableStateOf(setOf<String>()) }
    var pendingRequest by remember { mutableStateOf<String?>(null) }

    
    val sharedPreferences =
        remember {
            context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
        }
    val currentUsername =
        remember {
            sharedPreferences.getString(Constants.COMMUNITY_USERNAME, "") ?: ""
        }
    val currentName =
        remember {
            sharedPreferences.getString(Constants.COMMUNITY_NAME, "") ?: ""
        }
    val token =
        remember {
            sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
        }

    val suggestedFriends by communityViewModel.suggestedFriends.observeAsState()
    val searchResults by communityViewModel.searchResult.observeAsState()
    val actionResponse by communityViewModel.actionResponse.observeAsState()

    LaunchedEffect(Unit) {
        if (token.isNotEmpty()) {
            communityViewModel.getSuggestedFriends(token)
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            isSearching = true
            delay(500)
            if (token.isNotEmpty()) {
                communityViewModel.getSearchResult(token, searchQuery)
            }
            isSearching = false
        }
    }

    
    LaunchedEffect(actionResponse?.detail, pendingRequest) {
        actionResponse?.let { response ->
            pendingRequest?.let { username ->
                if (response.detail == "Friend request sent successfully") {
                    sentRequests = sentRequests + username
                }
                
                pendingRequest = null
            }
        }
    }

    
    fun sendFriendRequest(user: UserResponse) {
        if (token.isNotEmpty() && pendingRequest == null) {
            pendingRequest = user.username
            communityViewModel.sendRequest(token, user.username)
        }
    }

    
    val filteredSuggestedFriends =
        suggestedFriends?.filter { user ->
            user.username != currentUsername && user.name != currentName
        }
    val filteredSearchResults =
        searchResults?.filter { user ->
            user.username != currentUsername && user.name != currentName
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
                    text = "Add Friends",
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

        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .border(2.dp, Secondary, RoundedCornerShape(9999.dp))
                .background(Background, RoundedCornerShape(9999.dp)),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    cursorBrush = SolidColor(Accent),
                    textStyle =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = TextColor,
                            fontSize = 16.sp,
                            lineHeight = 16.sp,
                        ),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search",
                                    color = Accent.copy(alpha = 0.3f),
                                    style =
                                        MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 16.sp,
                                            lineHeight = 16.sp,
                                        ),
                                )
                            }
                            innerTextField()
                        }
                    },
                )
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = Accent,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (searchQuery.isBlank()) {
            SuggestedFriendsContent(
                suggestedFriends = filteredSuggestedFriends,
                sentRequests = sentRequests,
                pendingRequest = pendingRequest,
                onSendRequest = ::sendFriendRequest,
            )
        } else {
            SearchResultsContent(
                searchResults = filteredSearchResults,
                isSearching = isSearching,
                sentRequests = sentRequests,
                pendingRequest = pendingRequest,
                onSendRequest = ::sendFriendRequest,
            )
        }
    }
}

@Composable
private fun SuggestedFriendsContent(
    suggestedFriends: List<UserResponse>?,
    sentRequests: Set<String>,
    pendingRequest: String?,
    onSendRequest: (UserResponse) -> Unit,
) {
    Column {
        Text(
            text = "Suggested Friends",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        if (suggestedFriends == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Accent)
            }
        } else if (suggestedFriends.isEmpty()) {
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
                        contentDescription = "No suggestions",
                        modifier = Modifier.size(64.dp),
                        tint = Accent.copy(alpha = 0.5f),
                    )
                    Text(
                        text = "No suggested friends",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Accent,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(suggestedFriends) { friend ->
                    UserCard(
                        user = friend,
                        sentRequests = sentRequests,
                        pendingRequest = pendingRequest,
                        onSendRequest = { onSendRequest(friend) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultsContent(
    searchResults: List<UserResponse>?,
    isSearching: Boolean,
    sentRequests: Set<String>,
    pendingRequest: String?,
    onSendRequest: (UserResponse) -> Unit,
) {
    if (isSearching) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = Accent)
        }
    } else if (searchResults == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Start typing to search for friends",
                style = MaterialTheme.typography.bodyLarge,
                color = Accent,
                textAlign = TextAlign.Center,
            )
        }
    } else if (searchResults.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = "No results",
                    modifier = Modifier.size(64.dp),
                    tint = Accent.copy(alpha = 0.5f),
                )
                Text(
                    text = "No users found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Accent,
                    textAlign = TextAlign.Center,
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(searchResults) { user ->
                UserCard(
                    user = user,
                    sentRequests = sentRequests,
                    pendingRequest = pendingRequest,
                    onSendRequest = { onSendRequest(user) },
                )
            }
        }
    }
}

@Composable
private fun UserCard(
    user: UserResponse,
    sentRequests: Set<String>,
    pendingRequest: String?,
    onSendRequest: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Secondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
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

            Spacer(modifier = Modifier.width(8.dp))

            when {
                sentRequests.contains(user.username) -> {
                    IconButton(
                        onClick = { /* Disabled - no action */ },
                        enabled = false,
                        modifier =
                            Modifier
                                .size(40.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_request_pending),
                            contentDescription = "Request Sent",
                            tint = Accent,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                user.friend_status.lowercase() in listOf("request_sent", "pending", "sent") -> {
                    IconButton(
                        onClick = { /* Disabled - no action */ },
                        enabled = false,
                        modifier =
                            Modifier
                                .size(40.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_request_pending),
                            contentDescription = "Request Pending",
                            tint = Accent,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                user.friend_status.lowercase() == "friends" -> {
                    IconButton(
                        onClick = { /* Disabled - already friends */ },
                        enabled = false,
                        modifier =
                            Modifier
                                .size(40.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_community_filled),
                            contentDescription = "Already Friends",
                            tint = Accent,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                else -> {
                    IconButton(
                        onClick = onSendRequest,
                        modifier =
                            Modifier
                                .size(40.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_add_friend),
                            contentDescription = "Add Friend",
                            tint = TextColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}
