package com.dscvit.vitty.ui.academics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.ui.academics.components.AcademicsContent
import com.dscvit.vitty.ui.academics.components.AcademicsHeader
import com.dscvit.vitty.ui.academics.models.Course
import com.dscvit.vitty.ui.academics.models.sampleCourses
import com.dscvit.vitty.ui.coursepage.models.Reminder
import com.dscvit.vitty.util.SemesterUtils

@Composable
fun AcademicsScreenContent(
    modifier: Modifier = Modifier,
    userName: String = "Academics",
    profilePictureUrl: String?,
    allCourses: List<Course> = sampleCourses,
    onCourseClick: (Course) -> Unit = {},
    viewModel: AcademicsViewModel = viewModel(),
) {
    val tabs = listOf("Courses", "Reminders")
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var reminderSearchQuery by remember { mutableStateOf("") }
    var isCurrentSemester by remember { mutableStateOf(true) }
    var reminderStatus by remember { mutableIntStateOf(0) }

    val allReminders by viewModel.allReminders.collectAsStateWithLifecycle()

    val filteredCourses =
        remember(allCourses, searchQuery, isCurrentSemester) {
            allCourses.filter { course ->
                val matchesSearch =
                    searchQuery.isBlank() ||
                        course.title.contains(searchQuery, ignoreCase = true)

                if (isCurrentSemester) {
                    matchesSearch && course.semester == SemesterUtils.determineSemester()
                } else {
                    matchesSearch
                }
            }
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Background),
    ) {
        AcademicsHeader(
            userName = userName,
            profilePictureUrl = profilePictureUrl,
            tabs = tabs,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            isCurrentSemester = isCurrentSemester,
            onSemesterFilterChange = { isCurrentSemester = it },
            reminderStatus = reminderStatus,
            onReminderStatusChange = { reminderStatus = it },
            reminderSearchQuery = reminderSearchQuery,
            onReminderSearchQueryChange = { reminderSearchQuery = it },
        )

        AcademicsContent(
            selectedTab = selectedTab,
            courses = filteredCourses,
            reminders = allReminders,
            reminderStatus = reminderStatus,
            reminderSearchQuery = reminderSearchQuery,
            onCourseClick = onCourseClick,
            onToggleReminderComplete = { reminderId: Long, isCompleted: Boolean ->
                viewModel.updateReminderStatus(reminderId, isCompleted)
            },
            onDeleteReminder = { reminder: Reminder ->
                viewModel.deleteReminder(reminder)
            }
        )
    }
}
