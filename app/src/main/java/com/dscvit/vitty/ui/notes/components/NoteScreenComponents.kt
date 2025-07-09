package com.dscvit.vitty.ui.notes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun NoteHeader(
    onBackClick: () -> Unit,
    isPreviewMode: Boolean,
    onTogglePreview: () -> Unit,
    onSaveNote: () -> Unit = {},
    canSave: Boolean = false,
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

        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(
                onClick = onSaveNote,
                enabled = canSave,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save",
                    tint = if (canSave) Accent else TextColor.copy(alpha = 0.5f),
                )
            }

            IconButton(
                onClick = onTogglePreview,
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
}

@Composable
fun NoteToolbar(
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
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_format_list_bulleted),
                contentDescription = "Bullet List",
                tint = Accent,
            )
        }

        ToolbarButton(
            onClick = onChecklistClick,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_library_add_check),
                contentDescription = "CheckList",
                tint = Accent,
            )
        }

        ToolbarButton(
            onClick = onBoldClick,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_format_bold),
                contentDescription = "Bold",
                tint = Accent,
            )
        }

        ToolbarButton(
            onClick = onItalicClick,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_format_italic),
                contentDescription = "Italic",
                tint = Accent,
            )
        }

        ToolbarButton(
            onClick = onUnderlineClick,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_format_underlined),
                contentDescription = "Underline",
                tint = Accent,
            )
        }

        ToolbarButton(
            onClick = onUndoClick,
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
fun ToolbarButton(
    onClick: () -> Unit,
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

fun applyFormatting(
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

fun applyListFormatting(
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
