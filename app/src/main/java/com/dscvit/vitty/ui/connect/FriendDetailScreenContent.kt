package com.dscvit.vitty.ui.connect

import android.content.Context
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.dscvit.vitty.R
import com.dscvit.vitty.model.PeriodDetails
import com.dscvit.vitty.network.api.community.responses.user.UserResponse
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.Poppins
import com.dscvit.vitty.theme.Red
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.ui.schedule.ScheduleViewModel
import com.dscvit.vitty.util.Constants
import com.dscvit.vitty.util.Quote
import com.dscvit.vitty.util.UtilFunctions
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailScreenContent(
    friend: UserResponse,
    onBackClick: () -> Unit = {},
    connectViewModel: ConnectViewModel,
) {
    val context = LocalContext.current
    val scheduleViewModel: ScheduleViewModel = viewModel()
    val scope = rememberCoroutineScope()
    var quote by remember { mutableStateOf("") }
    var friendTimetableData by remember { mutableStateOf<Map<Int, List<PeriodDetails>>>(emptyMap()) }
    var isLoadingTimetable by remember { mutableStateOf(false) }
    var hasLoadedData by remember { mutableStateOf(false) }
    var hasUnfriended by remember { mutableStateOf(false) }
    var isSendingRequest by remember { mutableStateOf(false) }
    var hasRequestSent by remember { mutableStateOf(false) }
    var showUnfriendDialog by remember { mutableStateOf(false) }

    val unfriendSuccess by connectViewModel.unfriendSuccess.observeAsState()
    val sendRequestResponse by connectViewModel.sendRequestResponse.observeAsState()

    LaunchedEffect(unfriendSuccess) {
        if (unfriendSuccess == friend.username) {
            hasUnfriended = true
            connectViewModel.clearUnfriendSuccess()
        }
    }

    LaunchedEffect(sendRequestResponse) {
        if (sendRequestResponse != null && isSendingRequest) {
            isSendingRequest = false
            if (sendRequestResponse!!.detail == "Friend request sent successfully") {
                hasRequestSent = true
                connectViewModel.clearSendRequestResponse()

                val sharedPreferences = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
                val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
                val username = sharedPreferences.getString(Constants.COMMUNITY_USERNAME, "") ?: ""
                if (token.isNotEmpty() && username.isNotEmpty()) {
                    connectViewModel.refreshFriendList(token, username)
                }
            } else {
                connectViewModel.clearSendRequestResponse()
            }
        }
    }

    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val currentDay =
        remember {
            when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }
        }

    val pagerState =
        rememberPagerState(
            initialPage = currentDay,
            pageCount = { 7 },
        )
    LaunchedEffect(friend.username) {
        if (hasLoadedData) return@LaunchedEffect

        quote = Quote.getLine(context)

        val sharedPreferences = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
        val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""

        if (token.isNotEmpty()) {
            isLoadingTimetable = true
            scheduleViewModel.getUserWithTimeTable(token, friend.username)
        } else {
            hasLoadedData = true
        }
    }

    val userResponse by scheduleViewModel.user.observeAsState()

    LaunchedEffect(userResponse) {
        userResponse?.let { response ->
            scope.launch {
                withContext(Dispatchers.Default) {
                    try {
                        val processedData = processFriendTimetableData(response)
                        withContext(Dispatchers.Main) {
                            friendTimetableData = processedData
                            isLoadingTimetable = false
                            hasLoadedData = true
                        }
                    } catch (e: Exception) {
                        Timber.e("Error processing friend timetable: ${e.message}")
                        withContext(Dispatchers.Main) {
                            isLoadingTimetable = false
                            hasLoadedData = true
                        }
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
                    text = "Friend Profile",
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

        FriendProfileCard(
            friend = friend,
            hasUnfriended = hasUnfriended,
            isSendingRequest = isSendingRequest,
            hasRequestSent = hasRequestSent,
            onUnfriendClick = {
                showUnfriendDialog = true
            },
            onSendRequestClick = {
                val sharedPreferences = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
                val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
                if (token.isNotEmpty()) {
                    isSendingRequest = true
                    connectViewModel.sendRequest(token, friend.username)
                }
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        
        if (!hasUnfriended) {
            Text(
                text = "Schedule",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextColor,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                divider = {},
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    if (pagerState.currentPage < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            color = TextColor,
                        )
                    }
                },
            ) {
                days.forEachIndexed { index, day ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Text(
                                text = day,
                                fontFamily = Poppins,
                                fontWeight = if (pagerState.currentPage == index) FontWeight.Medium else FontWeight.Normal,
                                fontSize = 20.sp,
                                lineHeight = (20 * 1.4).sp,
                                color = if (pagerState.currentPage == index) TextColor else Accent,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                    )
                }
            }

            Box(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (isLoadingTimetable) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                    ) { page ->
                        DayScheduleContent(
                            periods = friendTimetableData[page] ?: emptyList(),
                            quote = quote,
                        )
                    }
                }
            }
        } else {
            
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_community_outline),
                        contentDescription = "No Access",
                        modifier = Modifier.size(64.dp),
                        tint = Accent.copy(alpha = 0.5f),
                    )
                    Text(
                        text = "Schedule Not Available",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextColor,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "You can no longer view ${friend.name}'s schedule as they are not in your friends list.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Accent,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                    )
                }
            }
        }
    }

    
    if (showUnfriendDialog) {
        AlertDialog(
            onDismissRequest = { showUnfriendDialog = false },
            title = {
                Text(
                    text = "Unfriend ${friend.name}?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove ${friend.name} from your friends list? You won't be able to see their schedule anymore.",
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = Accent,
                        ),
                )
            },
            containerColor = Secondary,
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnfriendDialog = false
                        val sharedPreferences = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE)
                        val token = sharedPreferences.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
                        val username = sharedPreferences.getString(Constants.COMMUNITY_USERNAME, "") ?: ""
                        if (token.isNotEmpty()) {
                            connectViewModel.unfriend(token, friend.username)
                            connectViewModel.getFriendList(token, username)
                        }
                    },
                ) {
                    Text(
                        text = "Unfriend",
                        color = Red,
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUnfriendDialog = false },
                ) {
                    Text(
                        text = "Cancel",
                        color = TextColor,
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
        )
    }
}

@Composable
fun FriendProfileCard(
    friend: UserResponse,
    hasUnfriended: Boolean,
    isSendingRequest: Boolean,
    hasRequestSent: Boolean,
    onUnfriendClick: () -> Unit,
    onSendRequestClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Secondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (friend.picture.isNotEmpty()) {
                    AsyncImage(
                        model = friend.picture,
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(64.dp)
                                .clip(CircleShape),
                        placeholder = painterResource(R.drawable.ic_gdscvit),
                        error = painterResource(R.drawable.ic_gdscvit),
                    )
                } else {
                    Box(
                        modifier =
                            Modifier
                                .size(64.dp)
                                .background(Accent.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text =
                                friend.name
                                    .take(2)
                                    .map { it.uppercaseChar() }
                                    .joinToString(""),
                            color = TextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = friend.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 24.sp,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "@${friend.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (hasUnfriended) {
                if (hasRequestSent) {
                    Button(
                        onClick = {},
                        enabled = false,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Accent.copy(alpha = 0.1f),
                                contentColor = Accent.copy(alpha = 0.6f),
                            ),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text = "Request Sent",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                } else {
                    Button(
                        onClick = onSendRequestClick,
                        enabled = !isSendingRequest,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = if (isSendingRequest) Accent.copy(alpha = 0.1f) else Accent,
                                contentColor = if (isSendingRequest) Accent.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onPrimary,
                            ),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        if (isSendingRequest) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Accent.copy(alpha = 0.7f),
                                )
                                Text(
                                    text = "Sending Request...",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        } else {
                            Text(
                                text = "Send Friend Request",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            } else {
                Button(
                    onClick = onUnfriendClick,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Red.copy(alpha = 0.1f),
                            contentColor = Red,
                        ),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(
                                width = 1.dp,
                                color = Red,
                                shape = RoundedCornerShape(12.dp),
                            ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = "Unfriend",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun DayScheduleContent(
    periods: List<PeriodDetails>,
    quote: String = "Every day is a new opportunity to learn and grow.",
) {
    if (periods.isEmpty()) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_timetable_outline),
                    contentDescription = "No Classes",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No classes today!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = quote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                )
            }
        }
    } else {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 64.dp),
        ) {
            items(
                items = periods,
                key = { period -> "${period.courseCode}_${period.startTime.seconds}" },
            ) { period ->
                FriendPeriodCard(
                    period = period,
                )
            }
        }
    }
}

@Composable
private fun FriendPeriodCard(period: PeriodDetails) {
    val context = LocalContext.current
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val startTimeStr =
        remember(period.startTime) {
            timeFormat.format(period.startTime.toDate()).uppercase()
        }
    val endTimeStr =
        remember(period.endTime) {
            timeFormat.format(period.endTime.toDate()).uppercase()
        }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    UtilFunctions.copyItem(
                        context,
                        "Class Details",
                        "CLASS_DETAILS",
                        "${period.courseName} - ${period.courseCode}\n$startTimeStr - $endTimeStr\n${period.slot}\n${period.roomNo}",
                    )
                },
        colors = CardDefaults.cardColors(containerColor = Secondary),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = period.courseName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$startTimeStr - $endTimeStr | ${period.slot}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Accent,
                    fontWeight = FontWeight.Medium,
                )

                if (period.roomNo.isNotEmpty()) {
                    Box(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .border(
                                    width = 1.dp,
                                    color = Accent,
                                    shape = RoundedCornerShape(9999.dp),
                                ).padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_compass),
                                contentDescription = "Compass icon",
                                modifier = Modifier.size(12.dp),
                                alignment = Alignment.Center,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = period.roomNo,
                                style =
                                    MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 12.sp,
                                        lineHeight = 12.sp,
                                        letterSpacing = (-0.12).sp,
                                        textAlign = TextAlign.Center,
                                    ),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }
    }
}

private suspend fun processFriendTimetableData(friend: UserResponse): Map<Int, List<PeriodDetails>> {
    return withContext(Dispatchers.Default) {
        val timetableData = friend.timetable?.data ?: return@withContext emptyMap()
        val dayNames = listOf("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")

        val result = mutableMapOf<Int, List<PeriodDetails>>()

        dayNames.forEachIndexed { index, dayName ->
            val courses =
                when (dayName) {
                    "monday" -> timetableData.Monday
                    "tuesday" -> timetableData.Tuesday
                    "wednesday" -> timetableData.Wednesday
                    "thursday" -> timetableData.Thursday
                    "friday" -> timetableData.Friday
                    "saturday" -> timetableData.Saturday
                    "sunday" -> timetableData.Sunday
                    else -> emptyList()
                }

            val periods =
                courses
                    ?.map { course ->
                        PeriodDetails(
                            courseCode = course.code,
                            courseName = course.name,
                            startTime = parseTimeToTimestamp(course.start_time),
                            endTime = parseTimeToTimestamp(course.end_time),
                            slot = course.slot,
                            roomNo = course.venue,
                        )
                    }?.sortedBy { it.startTime.toDate() } ?: emptyList()

            result[index] = periods
        }

        result
    }
}

private fun parseTimeToTimestamp(timeString: String): Timestamp =
    try {
        val time = replaceYearIfZero(timeString)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val date = dateFormat.parse(time)
        if (date != null) {
            Timestamp(date)
        } else {
            Timestamp.now()
        }
    } catch (e: Exception) {
        Timber.d("Date parsing error: ${e.message}")
        Timestamp.now()
    }

private fun replaceYearIfZero(dateStr: String): String =
    if (dateStr.startsWith("0")) {
        "2023" + dateStr.substring(4)
    } else {
        dateStr
    }
