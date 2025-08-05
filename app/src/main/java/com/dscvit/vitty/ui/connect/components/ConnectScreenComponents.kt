package com.dscvit.vitty.ui.connect.components

import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dscvit.vitty.R
import com.dscvit.vitty.network.api.community.responses.requests.RequestsResponse
import com.dscvit.vitty.network.api.community.responses.user.CircleItem
import com.dscvit.vitty.network.api.community.responses.user.CircleResponse
import com.dscvit.vitty.network.api.community.responses.user.FriendResponse
import com.dscvit.vitty.network.api.community.responses.user.UserResponse
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.ui.connect.ConnectViewModel
import com.dscvit.vitty.util.Constants
import com.dscvit.vitty.util.urlDecode
import java.util.Locale

@Composable
fun ConnectHeader(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    friendsFilter: Int,
    onFriendsFilterChange: (Int) -> Unit,
    friendRequests: RequestsResponse? = null,
    onFriendRequestsClick: () -> Unit = {},
    circleRequests: Int = 0,
    onCircleRequestsClick: () -> Unit = {},
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Background)
                .padding(bottom = 16.dp),
    ) {
        TabRow(
            modifier = Modifier.padding(horizontal = 20.dp),
            selectedTabIndex = selectedTab,
            containerColor = Background,
            contentColor = TextColor,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier =
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .height(3.dp),
                    color = TextColor,
                )
            },
            divider = {
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .background(Secondary),
                )
            },
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    text = {
                        Text(
                            text = tab,
                            style =
                                if (selectedTab == index) {
                                    MaterialTheme.typography.titleLarge
                                } else {
                                    MaterialTheme.typography.titleMedium
                                },
                            color = if (selectedTab == index) TextColor else Accent,
                        )
                    },
                    selectedContentColor = TextColor,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column {
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
                        onValueChange = onSearchQueryChange,
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
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Accent,
                            )
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row {
                        val options = listOf("Available", "View All")
                        options.forEachIndexed { index, label ->
                            FilterChip(
                                label = label,
                                isSelected = friendsFilter == index,
                                onClick = { onFriendsFilterChange(index) },
                            )
                            if (index < options.lastIndex) {
                                Spacer(Modifier.width(12.dp))
                            }
                        }
                    }

                    val requestCount = friendRequests?.size ?: 0
                    if (requestCount > 0) {
                        Box(
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Secondary)
                                    .clickable { onFriendRequestsClick() }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = "$requestCount new request${if (requestCount > 1) "s" else ""}",
                                color = Accent,
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            if (selectedTab == 1) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Secondary)
                                .clickable { onCircleRequestsClick() }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = "$circleRequests ${if (circleRequests == 0) "new" else "circle"} request${if (circleRequests > 1 || circleRequests == 0) "s" else ""}",
                            color = Accent,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Secondary)
                .border(
                    1.dp,
                    if (isSelected) Accent else Color.Transparent,
                    RoundedCornerShape(24.dp),
                ).clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = label,
                color = if (isSelected) Accent else TextColor.copy(alpha = 0.5f),
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ConnectTabContent(
    tabIndex: Int,
    searchQuery: String,
    friendsFilter: Int,
    friendList: FriendResponse? = null,
    circleList: CircleResponse? = null,
    isLoading: Boolean = false,
    isRefreshing: Boolean = false,
    isCircleLoading: Boolean = false,
    isCircleRefreshing: Boolean = false,
    viewModel: ConnectViewModel,
    onFriendClick: (UserResponse) -> Unit = {},
    onCircleClick: (CircleItem, FriendResponse?) -> Unit = { _: CircleItem, _: FriendResponse? -> },
    onFriendsRefresh: () -> Unit = {},
    onCirclesRefresh: () -> Unit = {},
) {
    val allFriends = friendList?.data ?: emptyList()
    val displayedFriends =
        allFriends.filter { friend ->
            val matchesSearch =
                searchQuery.isBlank() ||
                    friend.name.contains(searchQuery, ignoreCase = true) ||
                    friend.username.contains(searchQuery, ignoreCase = true)

            val matchesFilter =
                when (friendsFilter) {
                    0 -> friend.current_status?.status?.lowercase(Locale.ROOT) == "free"
                    else -> true
                }

            matchesSearch && matchesFilter
        }

    val apiCircles = circleList?.data ?: emptyList()
    val filteredCircles =
        apiCircles.filter { circle ->
            val matchesSearch =
                searchQuery.isBlank() ||
                    circle.circle_name.contains(searchQuery, ignoreCase = true)

            matchesSearch
        }

    when (tabIndex) {
        0 -> {
            val pullToRefreshState =
                rememberPullRefreshState(
                    refreshing = isRefreshing,
                    onRefresh = onFriendsRefresh,
                )

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .pullRefresh(pullToRefreshState),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding =
                        PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 144.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (isLoading) {
                        items(4) {
                            ShimmerListItem()
                        }
                    } else if (displayedFriends.isEmpty()) {
                        item {
                            EmptyStateContent(
                                title =
                                    if (searchQuery.isNotBlank()) {
                                        "No friends found"
                                    } else {
                                        if (friendsFilter == 0) {
                                            "No friends available"
                                        } else {
                                            "No friends added"
                                        }
                                    },
                                subtitle =
                                    if (searchQuery.isNotBlank()) {
                                        "Try a different search term"
                                    } else {
                                        if (friendsFilter == 0) {
                                            "Your friends are currently in class"
                                        } else {
                                            "Start connecting with your classmates"
                                        }
                                    },
                                icon = R.drawable.ic_community_outline,
                            )
                        }
                    } else {
                        items(displayedFriends) { friend ->
                            FriendCard(
                                friend = friend,
                                onClick = { onFriendClick(friend) },
                            )
                        }
                    }
                }
                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullToRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surface,
                )
            }
        }
        1 -> {
            val pullToRefreshState =
                rememberPullRefreshState(
                    refreshing = isCircleRefreshing,
                    onRefresh = onCirclesRefresh,
                )

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .pullRefresh(pullToRefreshState),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding =
                        PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 144.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (isCircleLoading) {
                        items(4) {
                            ShimmerListItem()
                        }
                    } else if (filteredCircles.isEmpty()) {
                        item {
                            EmptyStateContent(
                                title = if (searchQuery.isNotBlank()) "No circles found" else "No circles joined",
                                subtitle = if (searchQuery.isNotBlank()) "Try a different search term" else "Join or create study circles",
                                icon = R.drawable.ic_group_add,
                            )
                        }
                    } else {
                        items(filteredCircles) { circle ->
                            CircleCard(
                                circle = circle,
                                viewModel = viewModel,
                                onClick = onCircleClick,
                            )
                        }
                    }
                }
                PullRefreshIndicator(
                    refreshing = isCircleRefreshing,
                    state = pullToRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surface,
                )
            }
        }
    }
}

@Composable
fun EmptyStateContent(
    title: String,
    subtitle: String,
    icon: Int,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = Accent,
            modifier = Modifier.size(64.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextColor,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Accent,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun FriendCard(
    friend: UserResponse,
    onClick: () -> Unit = {},
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Secondary, RoundedCornerShape(16.dp))
                .clickable { onClick() }
                .padding(vertical = 28.dp, horizontal = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(52.dp)
                        .background(Accent.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (friend.picture.isNotEmpty()) {
                    AsyncImage(
                        model = friend.picture,
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(52.dp)
                                .clip(CircleShape),
                    )
                } else {
                    Text(
                        text =
                            friend.name
                                .take(2)
                                .map { it.uppercaseChar() }
                                .joinToString(""),
                        color = TextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.name,
                    color = TextColor,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                )

                Spacer(modifier = Modifier.height(4.dp))

                val currentStatus = friend.current_status
                val statusIcon =
                    when {
                        currentStatus?.status?.lowercase() == "free" -> R.drawable.ic_free
                        currentStatus?.status?.lowercase() == "busy" -> R.drawable.ic_busy
                        else -> R.drawable.ic_busy
                    }
                val statusText =
                    when {
                        currentStatus?.venue?.isNotEmpty() == true -> currentStatus.venue
                        currentStatus?.status?.lowercase(Locale.ROOT) == "free" -> "Available"
                        else -> "Not in a class right now"
                    }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Image(
                        painter = painterResource(id = statusIcon),
                        contentDescription = "Status Icon",
                        modifier = Modifier.size(12.dp),
                    )

                    Text(
                        text = statusText,
                        color = Accent,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun CircleCard(
    circle: CircleItem,
    viewModel: ConnectViewModel,
    onClick: (CircleItem, FriendResponse?) -> Unit = { _: CircleItem, _: FriendResponse? -> },
) {
    val context = LocalContext.current
    val circleMembers by viewModel.circleMembers.observeAsState()
    val circleMembersLoading by viewModel.circleMembersLoading.observeAsState()

    val circleData = circleMembers?.get(circle.circle_id)
    val isLoadingMembers = circleMembersLoading?.contains(circle.circle_id) == true

    LaunchedEffect(circle.circle_id, circleData, isLoadingMembers) {
        if (circleData == null && !isLoadingMembers) {
            val sharedPreferences = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
            val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""

            if (token.isNotEmpty()) {
                viewModel.getCircleDetails(token, circle.circle_id)
            }
        }
    }

    val availableCount =
        circleData?.data?.count { member ->
            member.current_status?.status?.lowercase(Locale.ROOT) == "free"
        } ?: 0

    val totalMembers = circleData?.data?.size ?: 0
    val busyCount = totalMembers - availableCount

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Secondary, RoundedCornerShape(16.dp))
                .clickable(enabled = !isLoadingMembers) {
                    onClick(circle, circleData)
                }.padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(52.dp)
                            .background(Accent.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text =
                            circle.circle_name
                                .take(2)
                                .map { it.uppercaseChar() }
                                .joinToString(""),
                        color = TextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = circle.circle_name.urlDecode(),
                        color = TextColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (isLoadingMembers) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(12.dp)
                                        .background(
                                            shimmerBrush(),
                                            CircleShape,
                                        ),
                            )
                            Box(
                                modifier =
                                    Modifier
                                        .width(80.dp)
                                        .height(14.dp)
                                        .background(
                                            shimmerBrush(),
                                            RoundedCornerShape(4.dp),
                                        ),
                            )
                        }
                    } else if (circleData != null && totalMembers > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            if (busyCount > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_busy),
                                        contentDescription = "Busy Icon",
                                        modifier = Modifier.size(12.dp),
                                    )
                                    Text(
                                        text = "$busyCount busy",
                                        color = Accent,
                                        fontSize = 14.sp,
                                    )
                                }
                            }
                            if (availableCount > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_free),
                                        contentDescription = "Available Icon",
                                        modifier = Modifier.size(12.dp),
                                    )
                                    Text(
                                        text = "$availableCount available",
                                        color = Accent,
                                        fontSize = 14.sp,
                                    )
                                }
                            }
                        }
                    } else if (circleData != null && totalMembers == 0) {
                        Text(
                            text = "No members in this circle",
                            color = Accent.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(12.dp)
                                        .background(
                                            shimmerBrush(),
                                            CircleShape,
                                        ),
                            )
                            Box(
                                modifier =
                                    Modifier
                                        .width(80.dp)
                                        .height(14.dp)
                                        .background(
                                            shimmerBrush(),
                                            RoundedCornerShape(4.dp),
                                        ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun shimmerBrush(): Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslateAnim =
        infiniteTransition.animateFloat(
            initialValue = -200f,
            targetValue = 1000f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = 1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "shimmerTranslateAnim",
        )

    return Brush.linearGradient(
        colors =
            listOf(
                Background.copy(alpha = 0.9f),
                Background.copy(alpha = 0.3f),
                Background.copy(alpha = 0.9f),
            ),
        start = Offset(shimmerTranslateAnim.value - 200f, 0f),
        end = Offset(shimmerTranslateAnim.value, 0f),
    )
}

@Composable
fun ShimmerListItem() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslateAnim =
        infiniteTransition.animateFloat(
            initialValue = -200f,
            targetValue = 1000f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = 1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "shimmerTranslateAnim",
        )

    val shimmerBrush =
        Brush.linearGradient(
            colors =
                listOf(
                    Background.copy(alpha = 0.9f),
                    Background.copy(alpha = 0.3f),
                    Background.copy(alpha = 0.9f),
                ),
            start = Offset(shimmerTranslateAnim.value - 200f, 0f),
            end = Offset(shimmerTranslateAnim.value, 0f),
        )

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Secondary, RoundedCornerShape(16.dp))
                .padding(vertical = 28.dp, horizontal = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(52.dp)
                        .background(shimmerBrush, CircleShape),
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(0.6f)
                            .height(16.dp)
                            .background(shimmerBrush, RoundedCornerShape(8.dp)),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(0.4f)
                            .height(14.dp)
                            .background(shimmerBrush, RoundedCornerShape(7.dp)),
                )
            }
        }
    }
}

@Composable
fun NoNetworkMessage() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Accent,
            modifier = Modifier.size(64.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Internet Connection",
            style = MaterialTheme.typography.titleLarge,
            color = TextColor,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Please check your internet settings and try again.",
            style = MaterialTheme.typography.bodyMedium,
            color = Accent,
            textAlign = TextAlign.Center,
        )
    }
}
