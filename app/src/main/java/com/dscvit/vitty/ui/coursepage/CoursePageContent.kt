package com.dscvit.vitty.ui.coursepage

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.ui.coursepage.components.*
import com.dscvit.vitty.ui.coursepage.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursePageContent(
    courseTitle: String,
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
