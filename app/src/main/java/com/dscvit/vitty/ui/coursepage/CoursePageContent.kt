package com.dscvit.vitty.ui.coursepage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.dscvit.vitty.R
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.DividerColor
import com.dscvit.vitty.theme.Green
import com.dscvit.vitty.theme.Red
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.theme.Yellow
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursePageContent(
    courseTitle: String,
    courseSlot: String,
    courseCode: String,
    onBackClick: () -> Unit,
    onNavigateToNote: (courseCode: String, noteId: String?, onSaveNote: (String, String) -> Unit) -> Unit = { _, _, _ -> },
    viewModel: CoursePageViewModel = viewModel(),
) {
    var showBottomModal by remember { mutableStateOf(false) }
    var showSetReminderModal by remember { mutableStateOf(false) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    val setReminderSheetState = rememberModalBottomSheetState()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()

    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri ->
            uri?.let {
                viewModel.addImageNote(it.toString())
            }
        }

    LaunchedEffect(courseCode) {
        viewModel.setCourseId(courseCode)
    }

    val reminders =
        remember {
            listOf(
                Reminders("DA I", "24 May", ReminderStatus.UPCOMING),
                Reminders("DA II", "2 June", ReminderStatus.UPCOMING),
                Reminders("Assignment 1", "15 June", ReminderStatus.CAN_WAIT),
                Reminders("Project Report", "20 June", ReminderStatus.CAN_WAIT),
                Reminders("Quiz I", "2 Jan", ReminderStatus.COMPLETED),
                Reminders("Quiz II", "10 Jan", ReminderStatus.COMPLETED),
            )
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background),
    ) {
        val backgroundAlpha by animateFloatAsState(
            targetValue = if (showBottomModal) 0.4f else 1f,
            animationSpec = tween(durationMillis = 300),
            label = "background_alpha",
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .alpha(backgroundAlpha),
        ) {
            CoursePageHeader(onBackClick = onBackClick)
            SearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
            )
            Spacer(Modifier.height(20.dp))
            CourseInfoSection(
                courseTitle = courseTitle,
                reminders = reminders,
            )
            NoteList(
                notes = notes,
                onImageClick = { imagePath -> fullScreenImageUrl = imagePath },
                onStarClick = { note -> viewModel.toggleStarredStatus(note) },
                onNoteClick = { note ->

                    onNavigateToNote(courseCode, note.id.toString()) { title, content ->
                        viewModel.updateNote(note.copy(title = title, content = content), note.id)
                    }
                },
                onDeleteNote = { note ->
                    viewModel.deleteNote(note, note.id)
                },
            )
        }

        if (showBottomModal) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) {
                            showBottomModal = false
                        },
            )
        }

        AnimatedFabGroup(
            showExpanded = showBottomModal,
            onToggleExpanded = { showBottomModal = !showBottomModal },
            onWriteNote = {
                showBottomModal = false
                onNavigateToNote(courseCode, null) { title, content ->
                    viewModel.addTextNote(title, content)
                }
            },
            onSetReminder = {
                showBottomModal = false
                showSetReminderModal = true
            },
            onUploadFile = {
                showBottomModal = false
                imagePickerLauncher.launch("image/*")
            },
        )
    }

    if (showSetReminderModal) {
        ModalBottomSheet(
            onDismissRequest = { showSetReminderModal = false },
            sheetState = setReminderSheetState,
            containerColor = Background,
            contentColor = TextColor,
            dragHandle = { },
        ) {
            SetReminderBottomSheet(
                onDismiss = { showSetReminderModal = false },
                courseTitle = courseTitle,
            )
        }
    }

    fullScreenImageUrl?.let { imageUrl ->
        FullScreenImageDialog(
            imageUrl = imageUrl,
            onDismiss = { fullScreenImageUrl = null },
        )
    }
}

enum class NoteType {
    TEXT,
    IMAGE,
}

data class Note(
    val id: Long = 0,
    val title: String,
    val content: String,
    val type: NoteType,
    val isStarred: Boolean,
    val imagePath: String? = null,
)

@Composable
private fun NoteList(
    notes: List<Note>,
    onImageClick: (String) -> Unit = {},
    onStarClick: (Note) -> Unit = {},
    onNoteClick: (Note) -> Unit = {},
    onDeleteNote: (Note) -> Unit = {},
) {
    if (notes.isEmpty()) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit_document),
                    contentDescription = "No notes",
                    tint = TextColor.copy(alpha = 0.3f),
                    modifier = Modifier.size(64.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No notes yet",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextColor.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap the + button to add your first note",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextColor.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    } else {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
        ) {
            items(notes.size) { index ->
                SwipeToDismissNote(
                    note = notes[index],
                    onNoteClick = { onNoteClick(notes[index]) },
                    onImageClick = onImageClick,
                    onStarClick = onStarClick,
                    onDelete = { onDeleteNote(notes[index]) },
                )
            }
        }
    }
}

@Composable
private fun NoteItem(
    note: Note,
    onNoteClick: () -> Unit = {},
    onImageClick: (String) -> Unit = {},
    onStarClick: (Note) -> Unit = {},
) {
    when (note.type) {
        NoteType.IMAGE -> {
            note.imagePath?.let { imagePath ->
                AsyncImage(
                    model = imagePath,
                    contentDescription = "Image note",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onImageClick(imagePath) },
                    contentScale = ContentScale.Crop,
                )
            }
        }
        NoteType.TEXT -> {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNoteClick() }
                        .background(Secondary)
                        .padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextColor,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (note.isStarred) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_star),
                            contentDescription = "Starred",
                            tint = Accent,
                            modifier =
                                Modifier
                                    .padding(bottom = 4.dp)
                                    .clickable { onStarClick(note) },
                        )
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_star),
                            contentDescription = "Not Starred",
                            tint = TextColor.copy(alpha = 0.3f),
                            modifier =
                                Modifier
                                    .padding(bottom = 4.dp)
                                    .clickable { onStarClick(note) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                MarkdownText(
                    markdown = note.content,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextColor),
                    truncateOnTextOverflow = true,
                    maxLines = 3,
                )
            }
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
        Text(
            text = courseTitle,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 20.sp, lineHeight = 20.sp),
            color = TextColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        val prioritizedReminders =
            remember(reminders) {
                mutableListOf<Reminders>().apply {
                    addAll(reminders.filter { it.status == ReminderStatus.UPCOMING })
                    addAll(reminders.filter { it.status == ReminderStatus.CAN_WAIT })
                    addAll(reminders.filter { it.status == ReminderStatus.COMPLETED })
                }
            }

        val displayedReminders = prioritizedReminders.take(3)
        val remainingCount = prioritizedReminders.size - 3

        if (displayedReminders.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 0.dp),
            ) {
                items(displayedReminders.size) { index ->
                    RemindersChip(
                        text = "${displayedReminders[index].title} by ${displayedReminders[index].dueDate}",
                        reminder = displayedReminders[index],
                    )
                }

                if (remainingCount > 0) {
                    item {
                        RemindersChip(
                            text = "+$remainingCount",
                            reminder = null,
                        )
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
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
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
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
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
private fun SetReminderBottomSheet(
    onDismiss: () -> Unit,
    courseTitle: String,
) {
    var currentPage by remember { mutableStateOf(0) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var isAllDay by remember { mutableStateOf(false) }
    var fromTime by remember { mutableStateOf("7:00AM") }
    var toTime by remember { mutableStateOf("6:00AM") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var attachmentUrl by remember { mutableStateOf("") }

    when (currentPage) {
        0 ->
            FirstPage(
                selectedDateMillis = selectedDateMillis,
                onDateSelected = { selectedDateMillis = it },
                isAllDay = isAllDay,
                onAllDayToggle = { isAllDay = it },
                fromTime = fromTime,
                onFromTimeChange = { fromTime = it },
                toTime = toTime,
                onToTimeChange = { toTime = it },
                onDismiss = onDismiss,
                onNext = { currentPage = 1 },
            )
        1 ->
            SecondPage(
                title = title,
                onTitleChange = { title = it },
                description = description,
                onDescriptionChange = { description = it },
                subject = courseTitle,
                attachmentUrl = attachmentUrl,
                onAttachmentUrlChange = { attachmentUrl = it },
                onBack = { currentPage = 0 },
                onAdd = {
                    onDismiss()
                },
            )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FirstPage(
    selectedDateMillis: Long?,
    onDateSelected: (Long?) -> Unit,
    isAllDay: Boolean,
    onAllDayToggle: (Boolean) -> Unit,
    fromTime: String,
    onFromTimeChange: (String) -> Unit,
    toTime: String,
    onToTimeChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onNext: () -> Unit,
) {
    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis,
        )

    LaunchedEffect(datePickerState.selectedDateMillis) {
        onDateSelected(datePickerState.selectedDateMillis)
    }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Red),
            ) {
                Text("Cancel", style = MaterialTheme.typography.bodySmall)
            }

            TextButton(
                onClick = onNext,
                enabled = selectedDateMillis != null,
                colors = ButtonDefaults.textButtonColors(contentColor = Accent),
            ) {
                Text("Next", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        DatePicker(
            state = datePickerState,
            modifier =
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
            colors =
                DatePickerDefaults.colors(
                    containerColor = Secondary,
                    titleContentColor = TextColor,
                    headlineContentColor = TextColor,
                    weekdayContentColor = TextColor,
                    subheadContentColor = TextColor,
                    navigationContentColor = TextColor,
                    yearContentColor = TextColor,
                    disabledYearContentColor = TextColor.copy(alpha = 0.38f),
                    currentYearContentColor = Accent,
                    selectedYearContentColor = Background,
                    disabledSelectedYearContentColor = Background.copy(alpha = 0.38f),
                    selectedYearContainerColor = Accent,
                    disabledSelectedYearContainerColor = Accent.copy(alpha = 0.12f),
                    dayContentColor = TextColor,
                    disabledDayContentColor = TextColor.copy(alpha = 0.38f),
                    selectedDayContentColor = Background,
                    disabledSelectedDayContentColor = Background.copy(alpha = 0.38f),
                    selectedDayContainerColor = Accent,
                    disabledSelectedDayContainerColor = Accent.copy(alpha = 0.12f),
                    todayContentColor = Accent,
                    todayDateBorderColor = Accent,
                    dayInSelectionRangeContentColor = TextColor,
                    dayInSelectionRangeContainerColor = Secondary,
                    dividerColor = DividerColor,
                ),
        )

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Secondary)
                    .padding(horizontal = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "All-day",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextColor,
                    fontWeight = FontWeight.Medium,
                )
                Switch(
                    checked = isAllDay,
                    onCheckedChange = onAllDayToggle,
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
        }

        if (!isAllDay) {
            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Secondary)
                        .padding(horizontal = 8.dp),
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "To",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xff566A7B),
                        )
                        Box(
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x33475985))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = toTime,
                                style =
                                    MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Light,
                                    ),
                                color = TextColor,
                            )
                        }
                    }

                    HorizontalDivider(
                        color = DividerColor,
                        thickness = 1.dp,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "From",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xff566A7B),
                        )
                        Box(
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x33475985))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = fromTime,
                                style =
                                    MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Light,
                                    ),
                                color = TextColor,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SecondPage(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    subject: String,
    attachmentUrl: String,
    onAttachmentUrlChange: (String) -> Unit,
    onBack: () -> Unit,
    onAdd: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = onBack,
                colors = ButtonDefaults.textButtonColors(contentColor = Red),
            ) {
                Text("Back", style = MaterialTheme.typography.bodySmall)
            }

            TextButton(
                onClick = onAdd,
                colors = ButtonDefaults.textButtonColors(contentColor = Accent),
            ) {
                Text("Add", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }

        Text(
            text = "Set New Reminder",
            style = MaterialTheme.typography.headlineLarge,
            color = TextColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(22.dp))

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Secondary)
                    .padding(horizontal = 4.dp),
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ReminderTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        placeholder = "Title",
                        maxLines = 1,
                    )
                }

                HorizontalDivider(
                    color = DividerColor,
                    thickness = 1.dp,
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ReminderTextField(
                        value = description,
                        onValueChange = onDescriptionChange,
                        placeholder = "Description",
                        maxLines = 3,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Secondary)
                    .padding(horizontal = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Subject",
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 16.sp,
                            letterSpacing = (-0.16).sp,
                        ),
                    color = TextColor,
                )

                Spacer(modifier = Modifier.width(32.dp))

                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x33475985))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = subject,
                        style =
                            MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Light,
                            ),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        color = TextColor,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Secondary)
                    .padding(horizontal = 4.dp),
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Alert",
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 16.sp,
                                letterSpacing = (-0.16).sp,
                            ),
                        color = TextColor,
                    )

                    Text(
                        text = "None >",
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 16.sp,
                                letterSpacing = (-0.16).sp,
                            ),
                        color = Color(0xff334759),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Secondary)
                    .padding(horizontal = 4.dp),
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Add attachment...",
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 16.sp,
                                letterSpacing = (-0.16).sp,
                            ),
                        color = TextColor,
                    )
                }

                HorizontalDivider(
                    color = DividerColor,
                    thickness = 1.dp,
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ReminderTextField(
                        value = attachmentUrl,
                        onValueChange = onAttachmentUrlChange,
                        placeholder = "URL",
                        maxLines = 1,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ReminderTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle =
            MaterialTheme.typography.bodyMedium.copy(
                color = TextColor,
                fontSize = 16.sp,
                letterSpacing = (-0.16).sp,
            ),
        cursorBrush = SolidColor(Accent),
        minLines = minLines,
        maxLines = maxLines,
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Transparent),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                color = TextColor,
                                fontSize = 16.sp,
                                letterSpacing = (-0.16).sp,
                            ),
                        color = Color(0xff566A7B),
                        fontSize = 16.sp,
                    )
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun AnimatedFabGroup(
    showExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onWriteNote: () -> Unit,
    onSetReminder: () -> Unit,
    onUploadFile: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.BottomEnd)
                .padding(16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AnimatedVisibility(
            visible = showExpanded,
            enter =
                fadeIn(animationSpec = tween(300)) +
                    slideInVertically(
                        animationSpec = spring(dampingRatio = 0.8f),
                        initialOffsetY = { it / 3 },
                    ),
            exit =
                fadeOut(animationSpec = tween(200)) +
                    slideOutVertically(
                        animationSpec = tween(200),
                        targetOffsetY = { it / 3 },
                    ),
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FabWithTooltip("Write Note", R.drawable.ic_edit_document, onWriteNote)
                FabWithTooltip("Set Reminder", R.drawable.ic_clock, onSetReminder)
                FabWithTooltip("Upload File", R.drawable.ic_upload, onUploadFile)
            }
        }

        FloatingActionButton(
            onClick = onToggleExpanded,
            containerColor = Secondary,
            contentColor = TextColor,
            elevation =
                FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp,
                    hoveredElevation = 8.dp,
                    focusedElevation = 8.dp,
                ),
            shape = RoundedCornerShape(9999.dp),
        ) {
            val rotation by animateFloatAsState(
                targetValue = if (showExpanded) 45f else 0f,
                animationSpec = spring(dampingRatio = 0.8f),
                label = "fab_rotation",
            )

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (showExpanded) "Close" else "Add content",
                modifier =
                    Modifier
                        .size(24.dp)
                        .graphicsLayer(rotationZ = rotation),
            )
        }
    }
}

@Composable
private fun FabWithTooltip(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AnimatedVisibility(
            visible = true,
            enter =
                scaleIn(
                    animationSpec = spring(dampingRatio = 0.8f),
                    initialScale = 0.8f,
                ) + fadeIn(animationSpec = tween(200)),
            exit =
                scaleOut(
                    animationSpec = tween(150),
                    targetScale = 0.8f,
                ) + fadeOut(animationSpec = tween(150)),
        ) {
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(2.dp, Secondary, RoundedCornerShape(8.dp))
                        .background(Background)
                        .clickable {
                            onClick()
                        }.padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextColor,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        AnimatedVisibility(
            visible = true,
            enter =
                scaleIn(
                    animationSpec = spring(dampingRatio = 0.8f),
                    initialScale = 0.6f,
                ) + fadeIn(animationSpec = tween(200)),
            exit =
                scaleOut(
                    animationSpec = tween(150),
                    targetScale = 0.6f,
                ) + fadeOut(animationSpec = tween(150)),
        ) {
            FloatingActionButton(
                onClick = onClick,
                containerColor = TextColor,
                contentColor = Secondary,
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(9999.dp),
                elevation =
                    FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp,
                        hoveredElevation = 8.dp,
                        focusedElevation = 8.dp,
                    ),
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = text,
                )
            }
        }
    }
}

@Composable
private fun FullScreenImageDialog(
    imageUrl: String,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { onDismiss() },
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Full screen image",
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                contentScale = ContentScale.Fit,
            )

            IconButton(
                onClick = onDismiss,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissNote(
    note: Note,
    onNoteClick: () -> Unit = {},
    onImageClick: (String) -> Unit = {},
    onStarClick: (Note) -> Unit = {},
    onDelete: () -> Unit = {},
) {
    val dismissState =
        rememberSwipeToDismissBoxState(
            confirmValueChange = { dismissValue ->
                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                    onDelete()
                    true
                } else {
                    false
                }
            },
        )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            color = Red,
                            shape = RoundedCornerShape(16.dp),
                        ).padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        },
        enableDismissFromEndToStart = true,
        enableDismissFromStartToEnd = false,
    ) {
        NoteItem(
            note = note,
            onNoteClick = onNoteClick,
            onImageClick = onImageClick,
            onStarClick = onStarClick,
        )
    }
}
