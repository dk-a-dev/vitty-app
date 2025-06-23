package com.dscvit.vitty.ui.academics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import com.dscvit.vitty.R
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.TextColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreenContent(onBackClick: () -> Unit) {
    var noteText by remember { mutableStateOf("") }

    Scaffold(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Background),
        containerColor = Background,
        topBar = {
            NoteHeader(onBackClick = onBackClick)
        },
        bottomBar = {
            NoteToolbar(
                onBoldClick = { /* TODO: Add bold formatting */ },
                onItalicClick = { /* TODO: Add italic formatting */ },
                onUnderlineClick = { /* TODO: Add underline formatting */ },
                onBulletClick = { /* TODO: Add bullet list formatting */ },
                onChecklistClick = { /* TODO: Add checklist formatting */ },
                onUndoClick = { /* TODO: Add undo functionality */ },
                onRedoClick = { /* TODO: Add redo functionality */ },
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
                BasicTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier.fillMaxSize(),
                    textStyle =
                        MaterialTheme.typography.bodyMedium.copy(color = TextColor),
                    cursorBrush = SolidColor(Accent),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            if (noteText.isEmpty()) {
                                Text(
                                    text = "Start writing your note...",
                                    color = TextColor.copy(alpha = 0.5f),
                                    style =
                                        MaterialTheme.typography.bodyMedium,
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

@Composable
private fun NoteHeader(onBackClick: () -> Unit) {
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
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_undo),
                contentDescription = "Undo",
                tint = Accent,
            )
        }

        ToolbarButton(
            onClick = onRedoClick,
            contentDescription = "Redo",
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_redo),
                contentDescription = "Redo",
                tint = Accent,
            )
        }
    }
}

@Composable
private fun ToolbarButton(
    onClick: () -> Unit,
    contentDescription: String,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
    ) {
        content()
    }
}
