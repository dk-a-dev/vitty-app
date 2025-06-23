package com.dscvit.vitty.ui.academics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dscvit.vitty.R
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.Green
import com.dscvit.vitty.theme.Red
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.theme.Yellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursePageContent(
    courseTitle: String,
    courseSlot: String,
    courseCode: String,
    onBackClick: () -> Unit,
    onNavigateToNote: () -> Unit = {},
) {
    var searchQuery by remember { mutableStateOf("") }
    var showBottomModal by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()

    val reminders =
        listOf(
            Reminders("DA I", "24 May", ReminderStatus.UPCOMING),
            Reminders("DA II", "2 June", ReminderStatus.UPCOMING),
            Reminders("Assignment 1", "15 June", ReminderStatus.CAN_WAIT),
            Reminders("Project Report", "20 June", ReminderStatus.CAN_WAIT),
            Reminders("Quiz I", "2 Jan", ReminderStatus.COMPLETED),
            Reminders("Quiz II", "10 Jan", ReminderStatus.COMPLETED),
        )

    Scaffold(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background),
        containerColor = Background,
        topBar = {
            CoursePageHeader(
                onBackClick = onBackClick,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomModal = true },
                containerColor = Secondary,
                contentColor = TextColor,
                elevation =
                    FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp,
                        hoveredElevation = 8.dp,
                        focusedElevation = 8.dp,
                    ),
                shape = RoundedCornerShape(80.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add content",
                    modifier = Modifier.size(28.dp),
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 20.dp),
        ) {
            item {
                SearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                )

                Spacer(Modifier.height(20.dp))
            }

            item {
                CourseInfoSection(
                    courseTitle = courseTitle,
                    reminders = reminders,
                )
            }
        }
    }


    if (showBottomModal) {
        ModalBottomSheet(
            onDismissRequest = { showBottomModal = false },
            sheetState = bottomSheetState,
            containerColor = Background,
            contentColor = TextColor,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = Accent.copy(alpha = 0.4f),
                    width = 100.dp,
                    height = 7.dp,
                )
            },
        ) {
            AddContentBottomSheet(
                onDismiss = { showBottomModal = false },
                onNavigateToNote = onNavigateToNote,
            )
        }
    }
}

@Composable
private fun CoursePageHeader(onBackClick: () -> Unit) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 16.dp),
    ) {
    
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_round_chevron_left),
                contentDescription = "Back",
                tint = TextColor,
            )
        }

    
        Text(
            text = "Course Page",
            style = MaterialTheme.typography.headlineSmall,
            color = TextColor,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    placeholder: String = "Search",
) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp)
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
                                text = placeholder,
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
}

@Composable
private fun CourseInfoSection(
    courseTitle: String,
    reminders: List<Reminders>,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = courseTitle,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 20.sp, lineHeight = 20.sp),
                color = TextColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(12.dp))

        val upcomingReminders = reminders.filter { it.status == ReminderStatus.UPCOMING }
        val canWaitReminders = reminders.filter { it.status == ReminderStatus.CAN_WAIT }
        val completedReminders = reminders.filter { it.status == ReminderStatus.COMPLETED }

        val prioritizedReminders =
            mutableListOf<Reminders>().apply {
                addAll(upcomingReminders)
                addAll(canWaitReminders)
                addAll(completedReminders)
            }

        val totalReminders = prioritizedReminders.size
        val displayedReminders = prioritizedReminders.take(3)
        val remainingCount = totalReminders - 3
        if (displayedReminders.isNotEmpty()) {
            when {
                totalReminders == 0 -> {
                    Text(
                        text = "No reminders available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextColor.copy(alpha = 0.5f),
                    )
                }
                totalReminders <= 3 -> {
                    when (displayedReminders.size) {
                        1 -> {
                            Row {
                                RemindersChip(
                                    text = "${displayedReminders[0].title} by ${displayedReminders[0].dueDate}",
                                    reminder = displayedReminders[0],
                                )
                            }
                        }
                        2 -> {
                            Row {
                                displayedReminders.forEach { reminder ->
                                    RemindersChip(
                                        text = "${reminder.title} by ${reminder.dueDate}",
                                        reminder = reminder,
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                            }
                        }
                        3 -> {
                            Column {
                                Row {
                                    displayedReminders.take(2).forEach { reminder ->
                                        RemindersChip(
                                            text = "${reminder.title} by ${reminder.dueDate}",
                                            reminder = reminder,
                                        )
                                        Spacer(Modifier.width(8.dp))
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Row {
                                    RemindersChip(
                                        text = "${displayedReminders[2].title} by ${displayedReminders[2].dueDate}",
                                        reminder = displayedReminders[2],
                                    )
                                }
                            }
                        }
                    }
                }
                else -> {
                    Column {
                        Row {
                            displayedReminders.take(2).forEach { reminder ->
                                RemindersChip(
                                    text = "${reminder.title} by ${reminder.dueDate}",
                                    reminder = reminder,
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row {
                            RemindersChip(
                                text = "${displayedReminders[2].title} by ${displayedReminders[2].dueDate}",
                                reminder = displayedReminders[2],
                            )
                            Spacer(Modifier.width(8.dp))
                            RemindersChip(
                                text = "+$remainingCount",
                                reminder = null,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun RemindersChip(
    text: String,
    reminder: Reminders?,
) {
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Secondary)
                .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                reminder == null -> {
                    Text(
                        text = text,
                        color = TextColor,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                    )
                }
                reminder.status == ReminderStatus.COMPLETED -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Green,
                        modifier = Modifier.size(16.dp),
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = text,
                        color = TextColor,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                            ),
                    )
                }
                else -> {
                    Box(
                        modifier =
                            Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when (reminder.status) {
                                        ReminderStatus.UPCOMING -> Red
                                        ReminderStatus.CAN_WAIT -> Yellow
                                        else -> Secondary
                                    },
                                ),
                    )
                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = text,
                        color = TextColor,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                            ),
                    )
                }
            }
        }
    }
}

data class Reminders(
    val title: String,
    val dueDate: String,
    val status: ReminderStatus,
)

enum class ReminderStatus {
    UPCOMING,
    CAN_WAIT,
    COMPLETED,
}

@Composable
private fun AddContentBottomSheet(
    onDismiss: () -> Unit,
    onNavigateToNote: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 32.dp),
    ) {
        val addOptions =
            listOf(
                AddOption("Write Note", R.drawable.ic_edit_document),
                AddOption("Upload File", R.drawable.ic_upload),
                AddOption("Set Reminder", R.drawable.ic_clock),
            )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            addOptions.forEach { option ->
                AddOptionItem(
                    option = option,
                    onClick = {
                        onDismiss()
                        if (option.title == "Write Note") {
                            onNavigateToNote()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun AddOptionItem(
    option: AddOption,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onClick() }
                .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(60.dp))
                    .background(TextColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = option.icon),
                contentDescription = option.title,
                tint = Secondary,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = option.title,
            style = MaterialTheme.typography.bodyMedium,
            color = TextColor,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
    }
}

data class AddOption(
    val title: String,
    val icon: Int,
)
