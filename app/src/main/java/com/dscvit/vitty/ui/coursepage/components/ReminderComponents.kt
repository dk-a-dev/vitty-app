package com.dscvit.vitty.ui.coursepage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dscvit.vitty.theme.Accent
import com.dscvit.vitty.theme.Background
import com.dscvit.vitty.theme.DividerColor
import com.dscvit.vitty.theme.Red
import com.dscvit.vitty.theme.Secondary
import com.dscvit.vitty.theme.TextColor
import com.dscvit.vitty.ui.coursepage.models.AlertOption
import java.util.*

@Composable
fun SetReminderBottomSheet(
    onDismiss: () -> Unit,
    courseTitle: String,
    onSaveReminder: (
        title: String,
        description: String,
        dateMillis: Long,
        fromTime: String,
        toTime: String,
        isAllDay: Boolean,
        alertDaysBefore: Int,
        attachmentUrl: String,
    ) -> Unit = { _, _, _, _, _, _, _, _ -> },
) {
    var currentPage by remember { mutableStateOf(0) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var isAllDay by remember { mutableStateOf(false) }
    var fromTime by remember { mutableStateOf("07:00") }
    var toTime by remember { mutableStateOf("18:00") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var attachmentUrl by remember { mutableStateOf("") }
    var selectedAlert by remember { mutableStateOf(AlertOption.NONE) }

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
                selectedAlert = selectedAlert,
                onAlertChange = { selectedAlert = it },
                onBack = { currentPage = 0 },
                onAdd = {
                    selectedDateMillis?.let { dateMillis ->
                        onSaveReminder(
                            title,
                            description,
                            dateMillis,
                            fromTime,
                            toTime,
                            isAllDay,
                            selectedAlert.days,
                            attachmentUrl,
                        )
                    }
                    onDismiss()
                },
            )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstPage(
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

    var showFromTimePicker by remember { mutableStateOf(false) }
    var showToTimePicker by remember { mutableStateOf(false) }

    val fromTimePickerState =
        rememberTimePickerState(
            initialHour = fromTime.split(":")[0].toIntOrNull() ?: 7,
            initialMinute = fromTime.split(":")[1].toIntOrNull() ?: 0,
            is24Hour = true,
        )

    val toTimePickerState =
        rememberTimePickerState(
            initialHour = toTime.split(":")[0].toIntOrNull() ?: 18,
            initialMinute = toTime.split(":")[1].toIntOrNull() ?: 0,
            is24Hour = true,
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
                            text = "From",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xff566A7B),
                        )
                        Box(
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x33475985))
                                    .clickable { showFromTimePicker = true }
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
                            text = "To",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xff566A7B),
                        )
                        Box(
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x33475985))
                                    .clickable { showToTimePicker = true }
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
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showFromTimePicker) {
        AlertDialog(
            onDismissRequest = { showFromTimePicker = false },
            title = { Text("Select From Time") },
            text = {
                TimePicker(
                    state = fromTimePickerState,
                    colors =
                        TimePickerDefaults.colors(
                            containerColor = Secondary,
                            timeSelectorSelectedContainerColor = Accent,
                            timeSelectorUnselectedContainerColor = Background,
                            timeSelectorSelectedContentColor = Background,
                            timeSelectorUnselectedContentColor = TextColor,
                            clockDialColor = Secondary,
                            clockDialUnselectedContentColor = TextColor,
                            clockDialSelectedContentColor = Secondary,
                        ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onFromTimeChange(String.format("%02d:%02d", fromTimePickerState.hour, fromTimePickerState.minute))
                        showFromTimePicker = false
                    },
                ) {
                    Text("OK", color = Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFromTimePicker = false }) {
                    Text("Cancel", color = Red)
                }
            },
            containerColor = Background,
        )
    }

    if (showToTimePicker) {
        AlertDialog(
            onDismissRequest = { showToTimePicker = false },
            title = { Text("Select To Time") },
            text = {
                TimePicker(
                    state = toTimePickerState,
                    colors =
                        TimePickerDefaults.colors(
                            containerColor = Secondary,
                            timeSelectorSelectedContainerColor = Accent,
                            timeSelectorUnselectedContainerColor = Background,
                            timeSelectorSelectedContentColor = Background,
                            timeSelectorUnselectedContentColor = TextColor,
                            clockDialColor = Secondary,
                            clockDialUnselectedContentColor = TextColor,
                            clockDialSelectedContentColor = Secondary,
                        ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onToTimeChange(String.format("%02d:%02d", toTimePickerState.hour, toTimePickerState.minute))
                        showToTimePicker = false
                    },
                ) {
                    Text("OK", color = Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showToTimePicker = false }) {
                    Text("Cancel", color = Red)
                }
            },
            containerColor = Background,
        )
    }
}

@Composable
fun SecondPage(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    subject: String,
    attachmentUrl: String,
    onAttachmentUrlChange: (String) -> Unit,
    selectedAlert: AlertOption,
    onAlertChange: (AlertOption) -> Unit,
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

        AlertDropdownBox(
            selectedAlert = selectedAlert,
            onAlertChange = onAlertChange,
        )

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
fun ReminderTextField(
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
fun AlertDropdownBox(
    selectedAlert: AlertOption,
    onAlertChange: (AlertOption) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Secondary)
                .clickable { expanded = true }
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = selectedAlert.displayText,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 16.sp,
                                letterSpacing = (-0.16).sp,
                            ),
                        color = Color(0xff334759),
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand dropdown",
                        tint = Color(0xff334759),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Secondary),
        ) {
            AlertOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.displayText,
                            color = TextColor,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    onClick = {
                        onAlertChange(option)
                        expanded = false
                    },
                    colors =
                        MenuDefaults.itemColors(
                            textColor = TextColor,
                        ),
                )
            }
        }
    }
}
