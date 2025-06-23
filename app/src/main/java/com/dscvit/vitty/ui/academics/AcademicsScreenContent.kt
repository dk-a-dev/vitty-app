package com.dscvit.vitty.ui.academics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.dscvit.vitty.R
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.ui.academics.component.CourseCard
import com.dscvit.vitty.ui.academics.model.Course
import com.dscvit.vitty.ui.academics.model.sampleCourses
import com.dscvit.vitty.util.SemesterUtils

@Composable
fun AcademicsScreenContent(
    modifier: Modifier = Modifier,
    userName: String = "Academics",
    profilePictureUrl: String?,
    allCourses: List<Course> = sampleCourses,
    onCourseClick: (Course) -> Unit = {}
) {
    val tabs = listOf("Courses", "Reminders")
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var isCurrentSemester by remember { mutableStateOf(true) }
    var reminderStatus by remember { mutableIntStateOf(0) }

    val filteredCourses = remember(allCourses, searchQuery, isCurrentSemester) {
        allCourses.filter { course ->
            val matchesSearch = searchQuery.isBlank() || 
                course.title.contains(searchQuery, ignoreCase = true)
            
            if (isCurrentSemester) {
                matchesSearch && course.semester == SemesterUtils.determineSemester()
            } else {
                matchesSearch
            }
        }
    }

    Column(
        modifier = modifier
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
            onReminderStatusChange = { reminderStatus = it }
        )

        
        AcademicsContent(
            selectedTab = selectedTab,
            courses = filteredCourses,
            reminderStatus = reminderStatus,
            onCourseClick = onCourseClick
        )
    }
}

@Composable
private fun AcademicsHeader(
    userName: String,
    profilePictureUrl: String?,
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isCurrentSemester: Boolean,
    onSemesterFilterChange: (Boolean) -> Unit,
    reminderStatus: Int,
    onReminderStatusChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(bottom = 16.dp),
    ) {
        
        ProfileHeader(
            userName = userName,
            profilePictureUrl = profilePictureUrl
        )

        
        AcademicsTabRow(
            tabs = tabs,
            selectedTab = selectedTab,
            onTabSelected = onTabSelected
        )

        Spacer(Modifier.height(20.dp))

        
        when (selectedTab) {
            0 -> CoursesTabFilters(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                isCurrentSemester = isCurrentSemester,
                onSemesterFilterChange = onSemesterFilterChange
            )
            1 -> RemindersTabFilters(
                reminderStatus = reminderStatus,
                onReminderStatusChange = onReminderStatusChange
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    userName: String,
    profilePictureUrl: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineLarge,
            color = TextColor,
            modifier = Modifier.weight(1f),
        )
        AsyncImage(
            model = profilePictureUrl,
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape),
            placeholder = painterResource(R.drawable.ic_gdscvit),
            error = painterResource(R.drawable.ic_gdscvit),
        )
    }
}

@Composable
private fun AcademicsTabRow(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        modifier = Modifier.padding(horizontal = 20.dp),
        selectedTabIndex = selectedTab,
        containerColor = Background,
        contentColor = TextColor,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier
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
                        style = if (selectedTab == index) {
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
}

@Composable
private fun CoursesTabFilters(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isCurrentSemester: Boolean,
    onSemesterFilterChange: (Boolean) -> Unit
) {
    Column {
        
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            placeholder = "Search"
        )

        Spacer(Modifier.height(16.dp))

        
        FilterChipRow(
            options = listOf("Current Semester", "All Semesters"),
            selectedIndex = if (isCurrentSemester) 0 else 1,
            onSelectionChange = { index -> onSemesterFilterChange(index == 0) }
        )
    }
}

@Composable
private fun RemindersTabFilters(
    reminderStatus: Int,
    onReminderStatusChange: (Int) -> Unit
) {
    Column {
        Spacer(Modifier.height(16.dp))
        
        
        FilterChipRow(
            options = listOf("Pending", "Completed"),
            selectedIndex = reminderStatus,
            onSelectionChange = onReminderStatusChange
        )
    }
}

@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    placeholder: String
) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(2.dp, Secondary, RoundedCornerShape(9999.dp))
            .background(Background, RoundedCornerShape(9999.dp)),
    ) {
        Row(
            modifier = Modifier
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
                textStyle = MaterialTheme.typography.bodyMedium.copy(
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
                                text = placeholder,
                                color = Accent.copy(alpha = 0.3f),
                                style = MaterialTheme.typography.bodyMedium.copy(
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
}

@Composable
private fun FilterChipRow(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        options.forEachIndexed { index, label ->
            FilterChip(
                label = label,
                isSelected = selectedIndex == index,
                onClick = { onSelectionChange(index) }
            )
            if (index < options.lastIndex) {
                Spacer(Modifier.width(12.dp))
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Secondary)
            .border(
                1.dp,
                if (isSelected) Accent else Color.Transparent,
                RoundedCornerShape(24.dp),
            )
            .clickable { onClick() }
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

@Composable
private fun AcademicsContent(
    selectedTab: Int,
    courses: List<Course>,
    reminderStatus: Int,
    onCourseClick: (Course) -> Unit
) {
    when (selectedTab) {
        0 -> CoursesContent(courses = courses, onCourseClick = onCourseClick)
        1 -> RemindersContent(reminderStatus = reminderStatus)
    }
}

@Composable
private fun CoursesContent(
    courses: List<Course>,
    onCourseClick: (Course) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        item {
            Spacer(Modifier.height(16.dp))
        }
        items(courses) { course ->
            CourseCard(
                course = course,
                onClick = { onCourseClick(course) }
            )
            Spacer(Modifier.height(16.dp))
        }
        item {
            Spacer(Modifier.height(120.dp))
        }
    }
}

@Composable
private fun RemindersContent(reminderStatus: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 70.dp),
        contentAlignment = Alignment.Center,
    ) {
        val statusText = when (reminderStatus) {
            0 -> "No pending reminders."
            1 -> "No completed reminders."
            else -> "No reminders yet."
        }
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyLarge,
            color = TextColor.copy(alpha = 0.7f),
        )
    }
}
