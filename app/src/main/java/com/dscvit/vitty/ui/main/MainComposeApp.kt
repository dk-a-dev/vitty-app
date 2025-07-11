package com.dscvit.vitty.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dscvit.vitty.R
import com.dscvit.vitty.network.api.community.responses.user.UserResponse
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.theme.VittyTheme
import com.dscvit.vitty.ui.academics.AcademicsScreenContent
import com.dscvit.vitty.ui.academics.models.Course
import com.dscvit.vitty.ui.connect.ConnectScreenContent
import com.dscvit.vitty.ui.coursepage.CoursePageContent
import com.dscvit.vitty.ui.coursepage.CoursePageViewModel
import com.dscvit.vitty.ui.coursepage.models.Note
import com.dscvit.vitty.ui.notes.NoteScreenContent
import com.dscvit.vitty.ui.schedule.ScheduleScreenContent
import com.dscvit.vitty.ui.schedule.ScheduleViewModel
import com.dscvit.vitty.util.Constants
import com.dscvit.vitty.util.SemesterUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun MainComposeApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var bottomNavVisible by remember { mutableStateOf(true) }
    
    LaunchedEffect(currentRoute) {
        bottomNavVisible = currentRoute?.let { route ->
            !route.startsWith("course_page") && !route.startsWith("note_screen")
        } ?: true
    }

    VittyTheme {
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
                composable("academics") {
                    AcademicsComposeScreen(navController = navController)
                }
                composable("schedule") {
                    ScheduleComposeScreen()
                }
                composable("connect") {
                    ConnectComposeScreen()
                }
                composable("course_page/{courseTitle}/{courseCode}") { backStackEntry ->
                    val encodedCourseTitle = backStackEntry.arguments?.getString("courseTitle") ?: ""
                    val encodedCourseCode = backStackEntry.arguments?.getString("courseCode") ?: ""
                    val courseTitle = URLDecoder.decode(encodedCourseTitle, StandardCharsets.UTF_8.toString())
                    val courseCode = URLDecoder.decode(encodedCourseCode, StandardCharsets.UTF_8.toString())
                    CoursePageContent(
                        courseTitle = courseTitle,
                        courseCode = courseCode,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onNavigateToNote = { courseCodeParam, noteId, _ ->
                            val encodedCourseCodeParam = URLEncoder.encode(courseCodeParam, StandardCharsets.UTF_8.toString())
                            val encodedNoteId = noteId?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: "new"
                            navController.navigate("note_screen/$encodedCourseCodeParam/$encodedNoteId")
                        }
                    )
                }
                composable("note_screen/{courseCode}/{noteId}") { backStackEntry ->
                    val encodedCourseCode = backStackEntry.arguments?.getString("courseCode") ?: ""
                    val encodedNoteId = backStackEntry.arguments?.getString("noteId") ?: "new"
                    val courseCode = URLDecoder.decode(encodedCourseCode, StandardCharsets.UTF_8.toString())
                    val noteId = if (encodedNoteId == "new") null else URLDecoder.decode(encodedNoteId, StandardCharsets.UTF_8.toString())
                    
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
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = bottomNavVisible,
                enter =
                    slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                    ) + fadeIn(animationSpec = tween(200)),
                exit =
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(250),
                    ) + fadeOut(animationSpec = tween(200)),
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

@Composable
fun AcademicsComposeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences(Constants.USER_INFO, 0)
    val scheduleViewModel: ScheduleViewModel = viewModel()

    var allCourses by remember { mutableStateOf<List<Course>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var hasLoadedData by remember { mutableStateOf(false) }

    LaunchedEffect(hasLoadedData) {
        if (hasLoadedData) return@LaunchedEffect

        val token = prefs.getString(Constants.COMMUNITY_TOKEN, "") ?: ""
        val username = prefs.getString(Constants.COMMUNITY_USERNAME, null) ?: ""

        allCourses = loadCachedCourses(prefs)

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

    AcademicsScreenContent(
        profilePictureUrl = prefs.getString(Constants.COMMUNITY_PICTURE, null),
        allCourses = allCourses,
        onCourseClick = { course ->
            val encodedTitle = URLEncoder.encode(course.title, StandardCharsets.UTF_8.toString())
            val encodedCode = URLEncoder.encode(course.code, StandardCharsets.UTF_8.toString())
            navController.navigate("course_page/$encodedTitle/$encodedCode")
        },
    )
}

@Composable
fun ScheduleComposeScreen() {
    ScheduleScreenContent()
}

@Composable
fun ConnectComposeScreen() {
    ConnectScreenContent(
        onSearchClick = {
        },
        onRequestsClick = {
        },
    )
}

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onDestinationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalHapticFeedback.current

    Card(
        modifier =
            modifier
                .padding(horizontal = 16.dp),
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
                .clickable { onClick() }
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
