package com.dscvit.vitty.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dscvit.vitty.R
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.TextColor
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreenContent(onBackClick: () -> Unit) {
    var noteText by remember { mutableStateOf(TextFieldValue("")) }
    var isPreviewMode by remember { mutableStateOf(false) }
    var undoStack by remember { mutableStateOf(listOf<TextFieldValue>()) }
    var redoStack by remember { mutableStateOf(listOf<TextFieldValue>()) }
    var lastSavedText by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(noteText) {
        delay(1000)
        if (noteText.text != lastSavedText.text && noteText.text.isNotEmpty()) {
            undoStack = (undoStack + lastSavedText).takeLast(50)
            redoStack = emptyList()
            lastSavedText = noteText
        }
    }

    fun saveToUndoStack() {
        if (noteText != lastSavedText) {
            undoStack = (undoStack + noteText).takeLast(50)
            redoStack = emptyList()
            lastSavedText = noteText
        }
    }

    Scaffold(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background),
        containerColor = Background,
        topBar = {
            NoteHeader(
                onBackClick = onBackClick,
                isPreviewMode = isPreviewMode,
                onTogglePreview = { isPreviewMode = !isPreviewMode },
            )
        },
        bottomBar = {
            NoteToolbar(
                onBoldClick = {
                    saveToUndoStack()
                    noteText = applyFormatting(noteText, "**", "**")
                },
                onItalicClick = {
                    saveToUndoStack()
                    noteText = applyFormatting(noteText, "*", "*")
                },
                onUnderlineClick = {
                    saveToUndoStack()
                    noteText = applyFormatting(noteText, "<u>", "</u>")
                },
                onBulletClick = {
                    saveToUndoStack()
                    noteText = applyListFormatting(noteText, "- ")
                },
                onChecklistClick = {
                    saveToUndoStack()
                    noteText = applyListFormatting(noteText, "- [ ] ")
                },
                onUndoClick = {
                    if (undoStack.isNotEmpty()) {
                        redoStack = (redoStack + noteText).takeLast(50)
                        noteText = undoStack.last()
                        undoStack = undoStack.dropLast(1)
                        lastSavedText = noteText
                    }
                },
                onRedoClick = {
                    if (redoStack.isNotEmpty()) {
                        undoStack = (undoStack + noteText).takeLast(50)
                        noteText = redoStack.last()
                        redoStack = redoStack.dropLast(1)
                        lastSavedText = noteText
                    }
                },
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Background)
                        .padding(16.dp),
            ) {
                if (isPreviewMode) {
                    MarkdownText(
                        markdown = noteText.text,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextColor),
                    )
                } else {
                    BasicTextField(
                        value = noteText,
                        onValueChange = { newValue ->
                            noteText = newValue
                        },
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextColor),
                        cursorBrush = SolidColor(Accent),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                if (noteText.text.isEmpty()) {
                                    Text(
                                        text = "Start writing your note...\n\nMarkdown supported:\n**bold** *italic* <u>underline</u>\n- bullet points\n- [ ] checkboxes",
                                        color = TextColor.copy(alpha = 0.5f),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteHeader(
    onBackClick: () -> Unit,
    isPreviewMode: Boolean,
    onTogglePreview: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Background)
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
            text = "Note",
            style = MaterialTheme.typography.headlineSmall,
            color = TextColor,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center),
        )

        IconButton(
            onClick = onTogglePreview,
            modifier = Modifier.align(Alignment.CenterEnd),
        ) {
            Icon(
                painter =
                    painterResource(
                        id = if (isPreviewMode) R.drawable.ic_edit_document else R.drawable.ic_notif,
                    ),
                contentDescription = if (isPreviewMode) "Edit" else "Preview",
                tint = if (isPreviewMode) Accent else TextColor,
            )
        }
    }
}

@Composable
private fun NoteToolbar(
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onUnderlineClick: () -> Unit,
    onBulletClick: () -> Unit,
    onChecklistClick: () -> Unit,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToolbarButton(
            onClick = onBulletClick,
            contentDescription = "Bullet List",
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_format_list_bulleted),
                contentDescription = "Bullet List",
                tint = Accent,
            )
        }

        ToolbarButton(
            onClick = onChecklistClick,
            contentDescription = "Checklist",
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_library_add_check),
                contentDescription = "CheckList",
                tint = Accent,
            )
        }

        ToolbarButton(
            onClick = onBoldClick,
            contentDescription = "Bold",
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_format_bold),
                contentDescription = "Bold",
                tint = Accent,
            )
        }

        ToolbarButton(
            onClick = onItalicClick,
            contentDescription = "Italic",
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_format_italic),
                contentDescription = "Italic",
                tint = Accent,
            )
        }

        ToolbarButton(
            onClick = onUnderlineClick,
            contentDescription = "Underline",
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_format_underlined),
                contentDescription = "Underline",
                tint = Accent,
            )
        }

        ToolbarButton(
            onClick = onUndoClick,
            contentDescription = "Undo",
            enabled = canUndo,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_undo),
                contentDescription = "Undo",
                tint = if (canUndo) Accent else TextColor.copy(alpha = 0.3f),
            )
        }

        ToolbarButton(
            onClick = onRedoClick,
            contentDescription = "Redo",
            enabled = canRedo,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_redo),
                contentDescription = "Redo",
                tint = if (canRedo) Accent else TextColor.copy(alpha = 0.3f),
            )
        }
    }
}

@Composable
private fun ToolbarButton(
    onClick: () -> Unit,
    contentDescription: String,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        enabled = enabled,
    ) {
        content()
    }
}

private fun applyFormatting(
    textFieldValue: TextFieldValue,
    prefix: String,
    suffix: String,
): TextFieldValue {
    val text = textFieldValue.text
    val selection = textFieldValue.selection

    return if (selection.collapsed) {
        val newText =
            text.substring(0, selection.start) +
                prefix + suffix +
                text.substring(selection.start)
        TextFieldValue(
            text = newText,
            selection = TextRange(selection.start + prefix.length),
        )
    } else {
        val selectedText = text.substring(selection.start, selection.end)
        val newText =
            text.substring(0, selection.start) +
                prefix + selectedText + suffix +
                text.substring(selection.end)
        TextFieldValue(
            text = newText,
            selection =
                TextRange(
                    selection.start + prefix.length,
                    selection.end + prefix.length,
                ),
        )
    }
}

private fun applyListFormatting(
    textFieldValue: TextFieldValue,
    listPrefix: String,
): TextFieldValue {
    val text = textFieldValue.text
    val selection = textFieldValue.selection

    val lineStart = text.lastIndexOf('\n', selection.start - 1) + 1
    val lineEnd = text.indexOf('\n', selection.start).let { if (it == -1) text.length else it }
    val currentLine = text.substring(lineStart, lineEnd)

    return if (currentLine.startsWith(listPrefix)) {
        val newText =
            text.substring(0, lineStart) +
                currentLine.removePrefix(listPrefix) +
                text.substring(lineEnd)
        TextFieldValue(
            text = newText,
            selection = TextRange(maxOf(0, selection.start - listPrefix.length)),
        )
    } else {
        val newText =
            text.substring(0, lineStart) +
                listPrefix + currentLine +
                text.substring(lineEnd)
        TextFieldValue(
            text = newText,
            selection = TextRange(selection.start + listPrefix.length),
        )
    }
}
