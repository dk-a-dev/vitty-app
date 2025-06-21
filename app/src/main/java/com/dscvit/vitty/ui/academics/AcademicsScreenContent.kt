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

@Composable
fun AcademicsScreenContent(
    modifier: Modifier = Modifier,
    userName: String = "Academics",
    profilePictureUrl: String?,
    allCourses: List<Course> = sampleCourses,
) {
    val tabs = listOf("Courses", "Reminders")
    val semesters = listOf("Current Semester", "All Semesters")

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSemester by remember { mutableIntStateOf(0) }

    val courses =
        remember(allCourses, searchQuery, selectedSemester) {
            allCourses
                .filter { course ->
                    searchQuery.isBlank() ||
                        course.title.contains(searchQuery, ignoreCase = true) ||
                        course.details.contains(searchQuery, ignoreCase = true)
                }
        }

    Column(
        modifier
            .fillMaxSize()
            .background(Background)
            .padding(bottom = 70.dp),
    ) {
        Row(
            Modifier
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
                modifier =
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                placeholder = painterResource(R.drawable.ic_gdscvit),
                error = painterResource(R.drawable.ic_gdscvit),
            )
        }

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

        Spacer(Modifier.height(20.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .border(2.dp, Secondary, RoundedCornerShape(9999.dp))
                .background(Background, RoundedCornerShape(9999.dp)),
        ) {
            Row(
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
                            Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    "Search",
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

        Spacer(Modifier.height(16.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            semesters.forEachIndexed { index, label ->
                val selected = selectedSemester == index
                Box(
                    Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Secondary)
                        .border(
                            1.dp,
                            if (selected) Accent else Color.Transparent,
                            RoundedCornerShape(24.dp),
                        ).clickable { selectedSemester = index }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selected) {
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
                            color = if (selected) Accent else TextColor.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Normal,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                if (index == 0) Spacer(Modifier.width(12.dp))
            }
        }

        Spacer(Modifier.height(32.dp))

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                ) {
                    items(courses) { course ->
                        CourseCard(course)
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
            1 -> {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No reminders yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextColor.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}
