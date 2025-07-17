package com.dscvit.vitty.ui.connect

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dscvit.vitty.R
import com.dscvit.vitty.network.api.community.responses.user.CircleItem
import com.dscvit.vitty.network.api.community.responses.user.FriendResponse
import com.dscvit.vitty.network.api.community.responses.user.UserResponse
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.ui.connect.components.CircleActionBottomSheet
import com.dscvit.vitty.ui.connect.components.ConnectHeader
import com.dscvit.vitty.ui.connect.components.ConnectTabContent
import com.dscvit.vitty.ui.connect.components.NoNetworkMessage
import com.dscvit.vitty.util.Constants
import com.dscvit.vitty.util.UtilFunctions.isNetworkAvailable
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectScreenContent(
    onSearchClick: () -> Unit = {},
    onFriendClick: (UserResponse) -> Unit = {},
    onCircleClick: (CircleItem, FriendResponse?) -> Unit = { _: CircleItem, _: FriendResponse? -> },
    onFriendRequestsClick: () -> Unit = {},
    connectViewModel: ConnectViewModel,
) {
    val context = LocalContext.current
    val friendList by connectViewModel.friendList.observeAsState()
    val circleList by connectViewModel.circleList.observeAsState()
    val createCircleResponse by connectViewModel.createCircleResponse.observeAsState()
    val circleMembers by connectViewModel.circleMembers.observeAsState(emptyMap())
    val isLoading by connectViewModel.isLoading.observeAsState(false)
    val isRefreshing by connectViewModel.isRefreshing.observeAsState(false)
    val isCircleLoading by connectViewModel.isCircleLoading.observeAsState(false)
    val isCircleRefreshing by connectViewModel.isCircleRefreshing.observeAsState(false)
    val friendRequests by connectViewModel.friendRequest.observeAsState()

    val isNetworkAvailable = remember { mutableStateOf(isNetworkAvailable(context)) }

    var isCircleActionSheetVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            isNetworkAvailable.value = isNetworkAvailable(context)
            kotlinx.coroutines.delay(5000)
        }
    }

    val tabs = listOf("Friends", "Circles")
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var friendsFilter by remember { mutableIntStateOf(0) }

    val onCreateCircleClick = {
        isCircleActionSheetVisible = false
    }

    val onJoinCircleClick = {
        isCircleActionSheetVisible = false
    }

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    val handleActionButtonClick = {
        if (selectedTab == 1) {
            isCircleActionSheetVisible = true
        } else {
            onSearchClick()
        }
    }

    val refreshFriendsData =
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

    val refreshCirclesData =
        remember {
            {
                val sharedPreferences = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
                val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""

                if (token.isNotEmpty()) {
                    connectViewModel.refreshCircleList(token)
                }
            }
        }

    CircleActionBottomSheet(
        isVisible = isCircleActionSheetVisible,
        onDismiss = { isCircleActionSheetVisible = false },
        onCreateCircleClick = onCreateCircleClick,
        onJoinCircleClick = onJoinCircleClick,
        connectViewModel = connectViewModel,
    )

    LaunchedEffect(isNetworkAvailable.value) {
        if (isNetworkAvailable.value && friendList == null && !isLoading) {
            val sharedPreferences = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
            val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
            val username = sharedPreferences.getString(Constants.COMMUNITY_USERNAME, "") ?: ""

            if (token.isNotEmpty()) {
                connectViewModel.getFriendList(token, username)
                connectViewModel.getCircleList(token)
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

    // Handle create circle response
    LaunchedEffect(createCircleResponse) {
        createCircleResponse?.let { response ->
            Toast.makeText(
                context,
                "Circle created successfully! Join code: ${response.join_code}",
                Toast.LENGTH_LONG
            ).show()
            
            // Refresh circle list to show the new circle
            val sharedPreferences = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
            val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
            if (token.isNotEmpty()) {
                connectViewModel.getCircleList(token)
            }
        }
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
                        onClick = { handleActionButtonClick() },
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
                beyondViewportPageCount = 1,
            ) { page ->
                ConnectTabContent(
                    tabIndex = page,
                    searchQuery = searchQuery,
                    friendsFilter = friendsFilter,
                    friendList = friendList,
                    circleList = circleList,
                    isLoading = isLoading,
                    isRefreshing = isRefreshing,
                    isCircleLoading = isCircleLoading,
                    isCircleRefreshing = isCircleRefreshing,
                    viewModel = connectViewModel,
                    onFriendClick = onFriendClick,
                    onCircleClick = onCircleClick,
                    onFriendsRefresh = refreshFriendsData,
                    onCirclesRefresh = refreshCirclesData,
                )
            }
        } else {
            NoNetworkMessage()
        }
    }
}
