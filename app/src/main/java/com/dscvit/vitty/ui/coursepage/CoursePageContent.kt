package com.dscvit.vitty.ui.coursepage

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.ui.coursepage.components.AnimatedFabGroup
import com.dscvit.vitty.ui.coursepage.components.CourseInfoSection
import com.dscvit.vitty.ui.coursepage.components.CoursePageHeader
import com.dscvit.vitty.ui.coursepage.components.FullScreenImageDialog
import com.dscvit.vitty.ui.coursepage.components.NoteList
import com.dscvit.vitty.ui.coursepage.components.SearchBar
import com.dscvit.vitty.ui.coursepage.components.SetReminderBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursePageContent(
    courseTitle: String,
    courseCode: String,
    onBackClick: () -> Unit,
    onNavigateToNote: (courseCode: String, noteId: String?, onSaveNote: (String, String) -> Unit) -> Unit = { _, _, _ -> },
    viewModel: CoursePageViewModel = viewModel(),
) {
    val context = LocalContext.current
    var showBottomModal by remember { mutableStateOf(false) }
    var showSetReminderModal by remember { mutableStateOf(false) }
    var fullScreenImageUrl by remember { mutableStateOf<String?>(null) }
    val setReminderSheetState = rememberModalBottomSheetState()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()

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
        viewModel.setCourseTitle(courseTitle)
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
            CoursePageHeader(
                onBackClick = onBackClick,
                courseTitle = courseTitle
            )
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
                onSaveReminder = { title, description, dateMillis, fromTime, toTime, isAllDay, alertDaysBefore, attachmentUrl ->
                    viewModel.addReminder(
                        title = title,
                        description = description,
                        dateMillis = dateMillis,
                        fromTime = fromTime,
                        toTime = toTime,
                        isAllDay = isAllDay,
                        alertDaysBefore = alertDaysBefore,
                        attachmentUrl = attachmentUrl,
                        onSuccess = {
                            showSetReminderModal = false
                            Toast.makeText(context, "Reminder created successfully", Toast.LENGTH_SHORT).show()
                        },
                        onError = { errorMessage ->
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        },
                    )
                },
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
