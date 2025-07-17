package com.dscvit.vitty.ui.main

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.dscvit.vitty.R
import com.dscvit.vitty.activity.SettingsActivity
import com.dscvit.vitty.network.api.community.APICommunityRestClient
import com.dscvit.vitty.network.api.community.RetrofitUserActionListener
import com.dscvit.vitty.network.api.community.responses.user.PostResponse
import com.dscvit.vitty.network.api.community.responses.user.UserResponse
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.theme.VittyTheme
import com.dscvit.vitty.ui.academics.AcademicsScreenContent
import com.dscvit.vitty.ui.academics.models.Course
import com.dscvit.vitty.ui.connect.AddFriendScreenContent
import com.dscvit.vitty.ui.connect.ConnectScreenContent
import com.dscvit.vitty.ui.connect.ConnectViewModel
import com.dscvit.vitty.ui.connect.FriendDetailScreenContent
import com.dscvit.vitty.ui.connect.FriendRequestsScreenContent
import com.dscvit.vitty.ui.coursepage.CoursePageContent
import com.dscvit.vitty.ui.coursepage.CoursePageViewModel
import com.dscvit.vitty.ui.coursepage.models.Note
import com.dscvit.vitty.ui.emptyclassrooms.EmptyClassroomsContent
import com.dscvit.vitty.ui.notes.NoteScreenContent
import com.dscvit.vitty.ui.schedule.ScheduleScreenContent
import com.dscvit.vitty.ui.schedule.ScheduleViewModel
import com.dscvit.vitty.util.Constants
import com.dscvit.vitty.util.LogoutHelper
import com.dscvit.vitty.util.SemesterUtils
import com.dscvit.vitty.util.UtilFunctions
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun MainComposeApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val scope = rememberCoroutineScope()
    val connectViewModel: ConnectViewModel = viewModel()

    var bottomNavVisible by remember { mutableStateOf(true) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(currentRoute) {
        bottomNavVisible = currentRoute?.let { route ->
            !route.startsWith("course_page") &&
                !route.startsWith("note_screen") &&
                route != "empty_classrooms" &&
                !route.startsWith("friend_detail") &&
                route != "add_friend" &&
                route != "friend_requests"
        } ?: true
    }

    VittyTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(
                    navController = navController,
                    onCloseDrawer = {
                        scope.launch {
                            drawerState.close()
                        }
                    },
                )
            },
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "schedule",
                    modifier = Modifier.fillMaxSize(),
                ) {
                    composable(
                        "academics",
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec =
                                    spring(
                                        dampingRatio = 0.8f,
                                        stiffness = 300f,
                                    ),
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec =
                                    spring(
                                        dampingRatio = 0.8f,
                                        stiffness = 300f,
                                    ),
                            ) + fadeOut(animationSpec = tween(200))
                        },
                    ) {
                        AcademicsComposeScreen(
                            navController = navController,
                            onOpenDrawer = {
                                scope.launch {
                                    drawerState.open()
                                }
                            },
                        )
                    }
                    composable(
                        "schedule",
                        enterTransition = {
                            when (initialState.destination.route) {
                                "academics" ->
                                    slideInHorizontally(
                                        initialOffsetX = { it },
                                        animationSpec =
                                            spring(
                                                dampingRatio = 0.8f,
                                                stiffness = 300f,
                                            ),
                                    ) + fadeIn(animationSpec = tween(300))

                                "connect" ->
                                    slideInHorizontally(
                                        initialOffsetX = { -it },
                                        animationSpec =
                                            spring(
                                                dampingRatio = 0.8f,
                                                stiffness = 300f,
                                            ),
                                    ) + fadeIn(animationSpec = tween(300))

                                else -> fadeIn(animationSpec = tween(300))
                            }
                        },
                        exitTransition = {
                            when (targetState.destination.route) {
                                "academics" ->
                                    slideOutHorizontally(
                                        targetOffsetX = { it },
                                        animationSpec =
                                            spring(
                                                dampingRatio = 0.8f,
                                                stiffness = 300f,
                                            ),
                                    ) + fadeOut(animationSpec = tween(200))

                                "connect" ->
                                    slideOutHorizontally(
                                        targetOffsetX = { -it },
                                        animationSpec =
                                            spring(
                                                dampingRatio = 0.8f,
                                                stiffness = 300f,
                                            ),
                                    ) + fadeOut(animationSpec = tween(200))

                                else -> fadeOut(animationSpec = tween(200))
                            }
                        },
                    ) {
                        ScheduleComposeScreen(
                            onOpenDrawer = {
                                scope.launch {
                                    drawerState.open()
                                }
                            },
                        )
                    }
                    composable(
                        "connect",
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec =
                                    spring(
                                        dampingRatio = 0.8f,
                                        stiffness = 300f,
                                    ),
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec =
                                    spring(
                                        dampingRatio = 0.8f,
                                        stiffness = 300f,
                                    ),
                            ) + fadeOut(animationSpec = tween(200))
                        },
                    ) {
                        ConnectComposeScreen(
                            navController = navController,
                            connectViewModel = connectViewModel,
                        )
                    }
                    composable(
                        "friend_detail/{friendData}",
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec =
                                    spring(
                                        dampingRatio = 0.9f,
                                        stiffness = 400f,
                                    ),
                            ) + fadeIn(animationSpec = tween(250))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec =
                                    spring(
                                        dampingRatio = 0.9f,
                                        stiffness = 400f,
                                    ),
                            ) + fadeOut(animationSpec = tween(150))
                        },
                    ) { backStackEntry ->
                        val encodedFriendData =
                            backStackEntry.arguments?.getString("friendData") ?: ""
                        val friendData = URLDecoder.decode(encodedFriendData, StandardCharsets.UTF_8.toString())

                        val friend =
                            try {
                                Gson().fromJson(friendData, UserResponse::class.java)
                            } catch (e: Exception) {
                                null
                            }

                        if (friend != null) {
                            FriendDetailScreenContent(
                                friend = friend,
                                onBackClick = {
                                    navController.popBackStack()
                                },
                                connectViewModel = connectViewModel,
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                navController.popBackStack()
                            }
                        }
                    }
                    composable(
                        "add_friend",
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec =
                                    spring(
                                        dampingRatio = 0.9f,
                                        stiffness = 400f,
                                    ),
                            ) + fadeIn(animationSpec = tween(250))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec =
                                    spring(
                                        dampingRatio = 0.9f,
                                        stiffness = 400f,
                                    ),
                            ) + fadeOut(animationSpec = tween(150))
                        },
                    ) {
                        AddFriendScreenContent(
                            onBackClick = {
                                navController.popBackStack()
                            },
                        )
                    }
                    composable(
                        "friend_requests",
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec =
                                    spring(
                                        dampingRatio = 0.9f,
                                        stiffness = 400f,
                                    ),
                            ) + fadeIn(animationSpec = tween(250))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec =
                                    spring(
                                        dampingRatio = 0.9f,
                                        stiffness = 400f,
                                    ),
                            ) + fadeOut(animationSpec = tween(150))
                        },
                    ) {
                        FriendRequestsScreenContent(
                            onBackClick = {
                                navController.popBackStack()
                            },
                            connectViewModel = connectViewModel,
                        )
                    }
                    composable(
                        "course_page/{courseTitle}/{courseCode}",
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec =
                                    spring(
                                        dampingRatio = 0.9f,
                                        stiffness = 400f,
                                    ),
                            ) + fadeIn(animationSpec = tween(250))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec =
                                    spring(
                                        dampingRatio = 0.9f,
                                        stiffness = 400f,
                                    ),
                            ) + fadeOut(animationSpec = tween(150))
                        },
                    ) { backStackEntry ->
                        val encodedCourseTitle =
                            backStackEntry.arguments?.getString("courseTitle") ?: ""
                        val encodedCourseCode =
                            backStackEntry.arguments?.getString("courseCode") ?: ""
                        val courseTitle =
                            URLDecoder.decode(encodedCourseTitle, StandardCharsets.UTF_8.toString())
                        val courseCode =
                            URLDecoder.decode(encodedCourseCode, StandardCharsets.UTF_8.toString())
                        CoursePageContent(
                            courseTitle = courseTitle,
                            courseCode = courseCode,
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onNavigateToNote = { courseCodeParam, noteId, _ ->
                                val encodedCourseCodeParam =
                                    URLEncoder.encode(
                                        courseCodeParam,
                                        StandardCharsets.UTF_8.toString(),
                                    )
                                val encodedNoteId =
                                    noteId?.let {
                                        URLEncoder.encode(
                                            it,
                                            StandardCharsets.UTF_8.toString(),
                                        )
                                    } ?: "new"
                                navController.navigate("note_screen/$encodedCourseCodeParam/$encodedNoteId")
                            },
                        )
                    }
                    composable(
                        "note_screen/{courseCode}/{noteId}",
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec =
                                    spring(
                                        dampingRatio = 0.9f,
                                        stiffness = 400f,
                                    ),
                            ) + fadeIn(animationSpec = tween(250))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec =
                                    spring(
                                        dampingRatio = 0.9f,
                                        stiffness = 400f,
                                    ),
                            ) + fadeOut(animationSpec = tween(150))
                        },
                    ) { backStackEntry ->
                        val encodedCourseCode =
                            backStackEntry.arguments?.getString("courseCode") ?: ""
                        val encodedNoteId = backStackEntry.arguments?.getString("noteId") ?: "new"
                        val courseCode =
                            URLDecoder.decode(encodedCourseCode, StandardCharsets.UTF_8.toString())
                        val noteId =
                            if (encodedNoteId == "new") {
                                null
                            } else {
                                URLDecoder.decode(
                                    encodedNoteId,
                                    StandardCharsets.UTF_8.toString(),
                                )
                            }

                        val viewModel: CoursePageViewModel = viewModel()
                        var noteToEdit by remember { mutableStateOf<Note?>(null) }

                        LaunchedEffect(noteId) {
                            if (noteId != null) {
                                noteToEdit = viewModel.getNoteById(noteId)
                            }
                        }

                        NoteScreenContent(
                            onBackClick = {
                                navController.popBackStack()
                            },
                            noteToEdit = noteToEdit,
                            onSaveNote = { title, content ->
                                viewModel.setCourseId(courseCode)
                                if (noteToEdit != null) {
                                    viewModel.updateExistingNote(
                                        noteId = noteToEdit!!.id.toString(),
                                        title = title,
                                        content = content,
                                        isStarred = noteToEdit!!.isStarred,
                                    )
                                } else {
                                    viewModel.addTextNote(title, content)
                                }
                                navController.popBackStack()
                            },
                        )
                    }
                    composable(
                        "empty_classrooms",
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec =
                                    spring(
                                        dampingRatio = 0.9f,
                                        stiffness = 400f,
                                    ),
                            ) + fadeIn(animationSpec = tween(250))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec =
                                    spring(
                                        dampingRatio = 0.9f,
                                        stiffness = 400f,
                                    ),
                            ) + fadeOut(animationSpec = tween(150))
                        },
                    ) {
                        EmptyClassroomsContent(
                            onBackClick = {
                                navController.popBackStack()
                            },
                        )
                    }
                }

                AnimatedVisibility(
                    visible = bottomNavVisible,
                    enter =
                        slideInVertically(
                            initialOffsetY = { it },
                            animationSpec =
                                spring(
                                    dampingRatio = 0.8f,
                                    stiffness = 300f,
                                ),
                        ) +
                            fadeIn(
                                animationSpec =
                                    spring(
                                        dampingRatio = 0.8f,
                                        stiffness = 400f,
                                    ),
                            ) +
                            scaleIn(
                                initialScale = 0.6f,
                                animationSpec =
                                    spring(
                                        dampingRatio = 0.7f,
                                        stiffness = 350f,
                                    ),
                            ),
                    exit =
                        slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec =
                                spring(
                                    dampingRatio = 0.9f,
                                    stiffness = 500f,
                                ),
                        ) +
                            fadeOut(
                                animationSpec =
                                    spring(
                                        dampingRatio = 1.0f,
                                        stiffness = 600f,
                                    ),
                            ) +
                            scaleOut(
                                targetScale = 0.6f,
                                animationSpec =
                                    spring(
                                        dampingRatio = 0.8f,
                                        stiffness = 500f,
                                    ),
                            ),
                    modifier = Modifier.align(Alignment.BottomCenter),
                ) {
                    BottomNavigationBar(
                        currentRoute = currentRoute,
                        onDestinationClick = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(bottom = 48.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun AcademicsComposeScreen(
    navController: NavHostController,
    onOpenDrawer: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(Constants.USER_INFO, 0) }
    val scheduleViewModel: ScheduleViewModel = viewModel()

    var allCourses by remember { mutableStateOf<List<Course>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasLoadedData by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (hasLoadedData) return@LaunchedEffect

        allCourses = loadCachedCourses(prefs)

        val token = prefs.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
        val username = prefs.getString(Constants.COMMUNITY_USERNAME, null) ?: ""

        if (token.isNotEmpty() && username.isNotEmpty()) {
            isLoading = true
            scheduleViewModel.getUserWithTimeTable(token, username)
        }

        hasLoadedData = true
    }

    val userResponse by scheduleViewModel.user.observeAsState()

    LaunchedEffect(userResponse) {
        userResponse?.let { response ->
            allCourses =
                withContext(Dispatchers.Default) {
                    extractCoursesFromTimetable(response)
                }
            isLoading = false
        }
    }

    val profilePictureUrl = remember { prefs.getString(Constants.COMMUNITY_PICTURE, null) }

    AcademicsScreenContent(
        profilePictureUrl = profilePictureUrl,
        allCourses = allCourses,
        onCourseClick = { course ->
            val encodedTitle = URLEncoder.encode(course.title, StandardCharsets.UTF_8.toString())
            val encodedCode = URLEncoder.encode(course.code, StandardCharsets.UTF_8.toString())
            navController.navigate("course_page/$encodedTitle/$encodedCode")
        },
        onOpenDrawer = onOpenDrawer,
    )
}

@Composable
fun ScheduleComposeScreen(onOpenDrawer: () -> Unit) {
    ScheduleScreenContent(onOpenDrawer = onOpenDrawer)
}

@Composable
fun ConnectComposeScreen(
    navController: NavHostController,
    connectViewModel: ConnectViewModel,
) {
    ConnectScreenContent(
        onSearchClick = {
            navController.navigate("add_friend")
        },
        onFriendClick = { friend ->
            val friendJson = Gson().toJson(friend)
            val encodedFriendData = URLEncoder.encode(friendJson, StandardCharsets.UTF_8.toString())
            navController.navigate("friend_detail/$encodedFriendData")
        },
        onFriendRequestsClick = {
            navController.navigate("friend_requests")
        },
        connectViewModel = connectViewModel,
    )
}

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onDestinationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalHapticFeedback.current

    val cardScale by animateFloatAsState(
        targetValue = 1.0f,
        animationSpec =
            spring(
                dampingRatio = 0.6f,
                stiffness = 200f,
            ),
        label = "cardScale",
    )

    Card(
        modifier =
            modifier
                .padding(horizontal = 16.dp)
                .scale(cardScale),
        shape = RoundedCornerShape(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = Background,
            ),
        border = BorderStroke(width = 1.dp, color = Secondary),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavigationItem(
                icon = R.drawable.ic_academics_outline,
                text = "Academics",
                isSelected = currentRoute == "academics",
                onClick = {
                    hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onDestinationClick("academics")
                },
            )

            NavigationItem(
                icon = R.drawable.ic_timetable_outline,
                text = "Schedule",
                isSelected = currentRoute == "schedule",
                onClick = {
                    hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onDestinationClick("schedule")
                },
            )

            NavigationItem(
                icon = R.drawable.ic_community_outline,
                text = "Connect",
                isSelected = currentRoute == "connect",
                onClick = {
                    hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onDestinationClick("connect")
                },
            )
        }
    }
}

@Composable
fun NavigationItem(
    icon: Int,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.95f,
        animationSpec = tween(150),
        label = "scale",
    )

    val backgroundColor =
        if (isSelected) {
            Secondary
        } else {
            Color.Transparent
        }

    Row(
        modifier =
            modifier
                .scale(scale)
                .clip(RoundedCornerShape(120.dp))
                .background(backgroundColor)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onClick() }
                .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = text,
            tint = TextColor,
            modifier = Modifier.size(24.dp),
        )

        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(100)),
        ) {
            Text(
                text = text,
                color = TextColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun DrawerContent(
    navController: NavHostController,
    onCloseDrawer: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(Constants.USER_INFO, 0) }
    val profilePictureUrl = remember { prefs.getString(Constants.COMMUNITY_PICTURE, "") }
    val username = remember { prefs.getString(Constants.COMMUNITY_USERNAME, "") ?: "User" }
    val name = remember { prefs.getString(Constants.COMMUNITY_NAME, "") ?: "Name" }
    val campus = remember { prefs.getString(Constants.COMMUNITY_CAMPUS, "") ?: "Campus" }

    var isGhostModeEnabled by remember { mutableStateOf(prefs.getBoolean(Constants.GHOST_MODE, false)) }

    DisposableEffect(Unit) {
        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == Constants.GHOST_MODE) {
                    isGhostModeEnabled = prefs.getBoolean(Constants.GHOST_MODE, false)
                }
            }
        prefs.registerOnSharedPreferenceChangeListener(listener)

        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    ModalDrawerSheet(
        drawerContainerColor = Secondary,
        drawerContentColor = TextColor,
        modifier = Modifier.fillMaxWidth(0.8f),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp, vertical = 28.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = profilePictureUrl,
                    contentDescription = "Profile Picture",
                    modifier =
                        Modifier
                            .size(52.dp)
                            .clip(CircleShape),
                    placeholder = painterResource(R.drawable.ic_gdscvit),
                    error = painterResource(R.drawable.ic_gdscvit),
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Normal,
                    ),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "@$username",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style =
                    TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-.14).sp,
                        color = Accent,
                    ),
            )

            Spacer(modifier = Modifier.height(32.dp))

            HorizontalDivider(
                color = Accent,
                thickness = 1.dp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (campus.lowercase() == "vellore") {
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_empty_classroom),
                            contentDescription = "Find Empty Classroom",
                            tint = TextColor,
                        )
                    },
                    label = {
                        Text(
                            modifier = Modifier.padding(start = 24.dp),
                            text = "Find Empty Classroom",
                            color = TextColor,
                            style =
                                MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Normal,
                                ),
                        )
                    },
                    selected = false,
                    onClick = {
                        onCloseDrawer()
                        navController.navigate("empty_classrooms")
                    },
                    colors =
                        NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            selectedContainerColor = Secondary,
                        ),
                )
            }

            NavigationDrawerItem(
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings),
                        contentDescription = "Settings",
                        tint = TextColor,
                    )
                },
                label = {
                    Text(
                        modifier = Modifier.padding(start = 24.dp),
                        style =
                            MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Normal,
                            ),
                        text = "Settings",
                        color = TextColor,
                    )
                },
                selected = false,
                onClick = {
                    onCloseDrawer()
                    context.startActivity(Intent(context, SettingsActivity::class.java))
                },
                colors =
                    NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = Secondary,
                    ),
            )

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(
                color = Accent,
                thickness = 1.dp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            NavigationDrawerItem(
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_share),
                        contentDescription = "Share",
                        tint = TextColor,
                    )
                },
                label = {
                    Text(
                        modifier = Modifier.padding(start = 24.dp),
                        style =
                            MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Normal,
                            ),
                        text = "Share",
                        color = TextColor,
                    )
                },
                selected = false,
                onClick = {
                    onCloseDrawer()
                    val shareIntent =
                        Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_text))
                        }
                    context.startActivity(Intent.createChooser(shareIntent, null))
                },
                colors =
                    NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = Secondary,
                    ),
            )

            NavigationDrawerItem(
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_support),
                        contentDescription = "Support",
                        tint = TextColor,
                    )
                },
                label = {
                    Text(
                        modifier = Modifier.padding(start = 24.dp),
                        style =
                            MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Normal,
                            ),
                        text = "Support",
                        color = TextColor,
                    )
                },
                selected = false,
                onClick = {
                    onCloseDrawer()
                    UtilFunctions.openLink(context, context.getString(R.string.telegram_link))
                },
                colors =
                    NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = Secondary,
                    ),
            )

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(
                color = Accent,
                thickness = 1.dp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text =
                            buildAnnotatedString {
                                append("Ghost Mode ")
                                addStyle(
                                    style =
                                        SpanStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            letterSpacing = (-0.12).sp,
                                            color = Accent,
                                        ),
                                    start = 0,
                                    end = "Ghost Mode".length,
                                )

                                append("(your timetable will be visible only to you)")
                                addStyle(
                                    style =
                                        SpanStyle(
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp,
                                            letterSpacing = (-0.12).sp,
                                            color = Accent,
                                        ),
                                    start = "Ghost Mode ".length,
                                    end = "Ghost Mode (your timetable will be visible only to you)".length,
                                )
                            },
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Switch(
                    checked = isGhostModeEnabled,
                    onCheckedChange = { isChecked ->
                        val token = prefs.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
                        val currentUsername = prefs.getString(Constants.COMMUNITY_USERNAME, "") ?: ""

                        if (token.isNotEmpty() && currentUsername.isNotEmpty()) {
                            if (isChecked) {
                                APICommunityRestClient.instance.enableGhostMode(
                                    token = token,
                                    username = currentUsername,
                                    retrofitUserActionListener =
                                        object : RetrofitUserActionListener {
                                            override fun onSuccess(
                                                call: Call<PostResponse>?,
                                                response: PostResponse?,
                                            ) {
                                                isGhostModeEnabled = true
                                                prefs.edit { putBoolean(Constants.GHOST_MODE, true) }
                                                Toast.makeText(context, "Ghost mode enabled", Toast.LENGTH_SHORT).show()
                                            }

                                            override fun onError(
                                                call: Call<PostResponse>?,
                                                t: Throwable?,
                                            ) {
                                                isGhostModeEnabled = false
                                                Toast.makeText(context, "Failed to enable ghost mode", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                )
                            } else {
                                APICommunityRestClient.instance.disableGhostMode(
                                    token = token,
                                    username = currentUsername,
                                    retrofitUserActionListener =
                                        object : RetrofitUserActionListener {
                                            override fun onSuccess(
                                                call: Call<PostResponse>?,
                                                response: PostResponse?,
                                            ) {
                                                isGhostModeEnabled = false
                                                prefs.edit { putBoolean(Constants.GHOST_MODE, false) }
                                                Toast.makeText(context, "Ghost mode disabled", Toast.LENGTH_SHORT).show()
                                            }

                                            override fun onError(
                                                call: Call<PostResponse>?,
                                                t: Throwable?,
                                            ) {
                                                isGhostModeEnabled = true
                                                Toast.makeText(context, "Failed to disable ghost mode", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                )
                            }
                        } else {
                            isGhostModeEnabled = isChecked
                            prefs.edit { putBoolean(Constants.GHOST_MODE, isChecked) }
                            Toast.makeText(context, "Ghost mode updated locally", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors =
                        SwitchDefaults.colors(
                            checkedThumbColor = Background,
                            checkedTrackColor = Accent,
                            uncheckedThumbColor = Color(0xff768EA4),
                            uncheckedTrackColor = Color(0x33475985),
                            uncheckedBorderColor = Color.Transparent,
                        ),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            NavigationDrawerItem(
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_logout_2),
                        contentDescription = "log out",
                        tint = Color(0xffFF0000),
                    )
                },
                label = {
                    Text(
                        style =
                            MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Normal,
                            ),
                        text = "log out",
                        color = Color(0xffFF0000),
                    )
                },
                selected = false,
                onClick = {
                    onCloseDrawer()

                    val activity = context as? Activity
                    if (activity != null) {
                        LogoutHelper.logout(context, activity, prefs)
                    }
                },
                colors =
                    NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = Secondary,
                    ),
            )
        }
    }
}

private suspend fun loadCachedCourses(prefs: android.content.SharedPreferences): List<Course> =
    withContext(Dispatchers.IO) {
        try {
            val cachedData =
                prefs.getString(Constants.CACHE_COMMUNITY_TIMETABLE, null)
                    ?: return@withContext emptyList()

            val userResponse =
                Gson().fromJson(cachedData, UserResponse::class.java)
                    ?: return@withContext emptyList()

            extractCoursesFromTimetable(userResponse)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

private fun extractCoursesFromTimetable(userResponse: UserResponse): List<Course> {
    val timetableData = userResponse.timetable?.data ?: return emptyList()

    val allLectures =
        sequenceOf(
            timetableData.Monday,
            timetableData.Tuesday,
            timetableData.Wednesday,
            timetableData.Thursday,
            timetableData.Friday,
            timetableData.Saturday,
            timetableData.Sunday,
        ).filterNotNull().flatten()

    val currentSemester = SemesterUtils.determineSemester()

    return allLectures
        .groupBy { it.name }
        .mapNotNull { (title, lectures) ->
            if (title.isNullOrBlank()) return@mapNotNull null

            val uniqueSlots =
                lectures
                    .mapTo(LinkedHashSet()) { it.slot }
                    .sorted()
                    .joinToString(" + ")

            val uniqueCodes =
                lectures
                    .mapTo(LinkedHashSet()) { it.code }
                    .sorted()
                    .joinToString(" / ")

            Course(
                title = title,
                slot = uniqueSlots,
                code = uniqueCodes,
                semester = currentSemester,
                isStarred = false,
            )
        }.sortedBy { it.title }
}
