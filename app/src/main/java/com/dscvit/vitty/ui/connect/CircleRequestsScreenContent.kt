package com.dscvit.vitty.ui.connect

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dscvit.vitty.R
import com.dscvit.vitty.network.api.community.responses.circle.CircleRequestItem
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.Green
import com.dscvit.vitty.theme.Red
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.util.Constants
import com.dscvit.vitty.util.urlDecode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircleRequestsScreenContent(
    onBackClick: () -> Unit = {},
    connectViewModel: ConnectViewModel,
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var processedRequests by remember { mutableStateOf(setOf<String>()) }
    val tabs = listOf("Received", "Sent")

    val receivedCircleRequests by connectViewModel.receivedCircleRequests.observeAsState()
    val sentCircleRequests by connectViewModel.sentCircleRequests.observeAsState()
    val isLoading by connectViewModel.isCircleRequestsLoading.observeAsState(false)
    val circleActionResponse by connectViewModel.circleActionResponse.observeAsState()

    LaunchedEffect(receivedCircleRequests, sentCircleRequests) {
        processedRequests = setOf()
    }

    LaunchedEffect(circleActionResponse) {
        circleActionResponse?.let { response ->
            when (response.detail) {
                "request accepted successfully" -> {
                    Toast.makeText(context, "Circle request accepted", Toast.LENGTH_SHORT).show()
                    connectViewModel.clearCircleActionResponse()

                    val sharedPreferences = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
                    val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
                    if (token.isNotEmpty()) {
                        connectViewModel.refreshCircleRequests(token)
                        connectViewModel.getCircleList(token)
                    }
                }
                "request declined successfully" -> {
                    Toast.makeText(context, "Circle request declined", Toast.LENGTH_SHORT).show()
                    connectViewModel.clearCircleActionResponse()

                    val sharedPreferences = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
                    val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
                    if (token.isNotEmpty()) {
                        connectViewModel.refreshCircleRequests(token)
                    }
                }
                "request unsent successfully" -> {
                    Toast.makeText(context, "Circle request unsent", Toast.LENGTH_SHORT).show()
                    connectViewModel.clearCircleActionResponse()

                    val sharedPreferences = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
                    val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
                    if (token.isNotEmpty()) {
                        connectViewModel.refreshCircleRequests(token)
                    }
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
                    text = "Circle Requests",
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
                    onClick = { selectedTab = index },
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

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Accent)
            }
        } else {
            when (selectedTab) {
                0 -> {
                    val requests = receivedCircleRequests?.data ?: emptyList()
                    val pendingRequests = requests.filter { !processedRequests.contains(it.circle_id) }
                    CircleRequestsList(
                        requests = pendingRequests,
                        isReceived = true,
                        emptyTitle = "No circle requests",
                        emptySubtitle = "You have no pending circle invitations",
                        connectViewModel = connectViewModel,
                        processedRequests = processedRequests,
                        onRequestProcessed = { circleId ->
                            processedRequests = processedRequests + circleId
                        },
                    )
                }
                1 -> {
                    val requests = sentCircleRequests?.data ?: emptyList()
                    val pendingRequests = requests.filter { !processedRequests.contains(it.circle_id) }
                    CircleRequestsList(
                        requests = pendingRequests,
                        isReceived = false,
                        emptyTitle = "No sent requests",
                        emptySubtitle = "You haven't sent any circle requests",
                        connectViewModel = connectViewModel,
                        processedRequests = processedRequests,
                        onRequestProcessed = { circleId ->
                            processedRequests = processedRequests + circleId
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CircleRequestsList(
    requests: List<CircleRequestItem>,
    isReceived: Boolean,
    emptyTitle: String,
    emptySubtitle: String,
    connectViewModel: ConnectViewModel,
    processedRequests: Set<String>,
    onRequestProcessed: (String) -> Unit,
) {
    if (requests.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_group_add),
                    contentDescription = "No requests",
                    modifier = Modifier.size(64.dp),
                    tint = Accent.copy(alpha = 0.5f),
                )
                Text(
                    text = emptyTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Accent,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = emptySubtitle,
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
            items(requests) { request ->
                CircleRequestCard(
                    request = request,
                    isReceived = isReceived,
                    connectViewModel = connectViewModel,
                    processedRequests = processedRequests,
                    onRequestProcessed = onRequestProcessed,
                )
            }
        }
    }
}

@Composable
private fun CircleRequestCard(
    request: CircleRequestItem,
    isReceived: Boolean,
    connectViewModel: ConnectViewModel,
    processedRequests: Set<String>,
    onRequestProcessed: (String) -> Unit,
) {
    val context = LocalContext.current

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
                Box(
                    modifier =
                        Modifier
                            .size(48.dp)
                            .background(Accent.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text =
                            request.circle_name
                                .take(2)
                                .map { it.uppercaseChar() }
                                .joinToString(""),
                        color = TextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.circle_name.urlDecode(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val displayText =
                        if (isReceived) {
                            "Invitation from @${request.from_username}"
                        } else {
                            "Sent to @${request.to_username}"
                        }

                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (isReceived) {
                    IconButton(
                        onClick = {
                            val sharedPreferences = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
                            val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
                            if (token.isNotEmpty()) {
                                onRequestProcessed(request.circle_id)
                                connectViewModel.declineCircleRequest(token, request.circle_id)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Decline",
                            tint = Red,
                        )
                    }

                    IconButton(
                        onClick = {
                            val sharedPreferences = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
                            val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
                            if (token.isNotEmpty()) {
                                onRequestProcessed(request.circle_id)
                                connectViewModel.acceptCircleRequest(token, request.circle_id)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Accept",
                            tint = Green,
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            val sharedPreferences = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
                            val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
                            if (token.isNotEmpty()) {
                                onRequestProcessed(request.circle_id)
                                connectViewModel.unsendCircleRequest(token, request.circle_id, request.to_username)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Unsend",
                            tint = Red,
                        )
                    }
                }
            }
        }
    }
}
