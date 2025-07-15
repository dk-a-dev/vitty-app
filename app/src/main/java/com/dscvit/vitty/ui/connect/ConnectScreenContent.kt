package com.dscvit.vitty.ui.connect

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.dscvit.vitty.network.api.community.responses.user.FriendResponse
import com.dscvit.vitty.network.api.community.responses.user.UserResponse
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.util.Constants
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectScreenContent(
    onSearchClick: () -> Unit = {},
    onFriendClick: (UserResponse) -> Unit = {},
    onFriendRequestsClick: () -> Unit = {},
    connectViewModel: ConnectViewModel,
) {
    val context = LocalContext.current
    val friendList by connectViewModel.friendList.observeAsState()
    val isLoading by connectViewModel.isLoading.observeAsState(false)
    val isRefreshing by connectViewModel.isRefreshing.observeAsState(false)
    val friendRequests by connectViewModel.friendRequest.observeAsState()

    val isNetworkAvailable = remember { mutableStateOf(isNetworkAvailable(context)) }

    LaunchedEffect(Unit) {
        while (true) {
            isNetworkAvailable.value = isNetworkAvailable(context)
            kotlinx.coroutines.delay(5000)
        }
    }

    val tabs = listOf("Friends", "Circles")
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var friendsFilter by remember { mutableIntStateOf(0) }

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    val refreshData =
        remember {
            {
                val sharedPreferences = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
                val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
                val username = sharedPreferences.getString(Constants.COMMUNITY_USERNAME, "") ?: ""

                if (token.isNotEmpty()) {
                    connectViewModel.refreshFriendList(token, username)
                    connectViewModel.getFriendRequest(token)
                }
            }
        }

    LaunchedEffect(isNetworkAvailable.value) {
        if (isNetworkAvailable.value && friendList == null && !isLoading) {
            val sharedPreferences = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
            val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
            val username = sharedPreferences.getString(Constants.COMMUNITY_USERNAME, "") ?: ""

            if (token.isNotEmpty()) {
                connectViewModel.getFriendList(token, username)
                connectViewModel.getFriendRequest(token)
            }
        }
    }

    LaunchedEffect(selectedTab) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(selectedTab)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        selectedTab = pagerState.currentPage
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Connect",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            },
            actions = {
                if (isNetworkAvailable.value) {
                    IconButton(
                        modifier = Modifier.padding(end = 4.dp),
                        onClick = onSearchClick,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_group_add),
                            contentDescription = "Add",
                            tint = TextColor,
                        )
                    }
                }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
        )

        if (isNetworkAvailable.value) {
            ConnectHeader(
                tabs = tabs,
                selectedTab = selectedTab,
                onTabSelected = { index ->
                    selectedTab = index
                },
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                friendsFilter = friendsFilter,
                onFriendsFilterChange = { friendsFilter = it },
                friendRequests = friendRequests,
                onFriendRequestsClick = onFriendRequestsClick,
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                ConnectTabContent(
                    tabIndex = page,
                    searchQuery = searchQuery,
                    friendsFilter = friendsFilter,
                    friendList = friendList,
                    isLoading = isLoading,
                    isRefreshing = isRefreshing,
                    onFriendClick = onFriendClick,
                    onRefresh = refreshData,
                )
            }
        } else {
            NoNetworkMessage()
        }
    }
}

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
                ) {
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
                        Spacer(Modifier.width(12.dp))
                    }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectTabContent(
    tabIndex: Int,
    searchQuery: String,
    friendsFilter: Int,
    friendList: FriendResponse? = null,
    isLoading: Boolean = false,
    isRefreshing: Boolean = false,
    onFriendClick: (UserResponse) -> Unit = {},
    onRefresh: () -> Unit = {},
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

    val sampleCircles = getSampleCircles()
    val filteredCircles =
        sampleCircles.filter { circle ->
            val matchesSearch =
                searchQuery.isBlank() ||
                    circle.name.contains(searchQuery, ignoreCase = true) ||
                    circle.description.contains(searchQuery, ignoreCase = true) ||
                    circle.subject.contains(searchQuery, ignoreCase = true)

            matchesSearch
        }

    when (tabIndex) {
        0 -> {
            val pullToRefreshState = rememberPullToRefreshState()

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = pullToRefreshState,
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding =
                        PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 144.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (isLoading) {
                        items(3) {
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
            }
        }
        1 -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 144.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (filteredCircles.isEmpty()) {
                    item {
                        EmptyStateContent(
                            title = "No circles found",
                            subtitle = "Join or create study circles",
                            icon = R.drawable.ic_group_add,
                        )
                    }
                } else {
                    items(filteredCircles) { circle ->
                        CircleCard(circle = circle)
                    }
                }
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

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)
    return capabilities != null &&
        (
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        )
}

data class Circle(
    val id: String,
    val name: String,
    val description: String,
    val members: Int,
    val isActive: Boolean,
    val subject: String,
)

@Composable
fun getSampleCircles(): List<Circle> =
    remember {
        listOf(
            Circle("1", "Data Structures Study Group", "\uD83C\uDFDB\uFE0F 2 busy \uD83C\uDF34 1 Available", 12, true, "DSA"),
            Circle("2", "Machine Learning Research", "\uD83C\uDFDB\uFE0F 2 busy \uD83C\uDF34 1 Available", 8, true, "AI/ML"),
            Circle("3", "Web Development Circle", "\uD83C\uDFDB\uFE0F 2 busy \uD83C\uDF34 1 Available", 15, false, "Web Dev"),
            Circle("4", "Database Design Workshop", "\uD83C\uDFDB\uFE0F 2 busy \uD83C\uDF34 1 Available", 10, true, "DBMS"),
            Circle("5", "Competitive Programming", "\uD83C\uDFDB\uFE0F 2 busy \uD83C\uDF34 1 Available", 20, true, "CP"),
        )
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
fun CircleCard(circle: Circle) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Secondary, RoundedCornerShape(16.dp))
                .clickable { /* Handle circle click */ }
                .padding(horizontal = 24.dp, vertical = 28.dp),
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
                            circle.name
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
                        text = circle.name,
                        color = TextColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = circle.description,
                        color = Accent,
                        fontSize = 14.sp,
                        maxLines = 2,
                    )
                }
            }
        }
    }
}

@Composable
fun ShimmerListItem() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha =
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "alpha",
        )

    val shimmerBrush =
        Brush.linearGradient(
            colors =
                listOf(
                    Secondary.copy(alpha = alpha.value),
                    Secondary.copy(alpha = 0.5f),
                    Secondary.copy(alpha = alpha.value),
                ),
            start = Offset.Zero,
            end = Offset(x = 300f, y = 300f),
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

            Box(
                modifier =
                    Modifier
                        .size(width = 60.dp, height = 24.dp)
                        .background(shimmerBrush, RoundedCornerShape(6.dp)),
            )
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
