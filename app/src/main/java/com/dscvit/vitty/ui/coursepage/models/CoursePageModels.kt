package com.dscvit.vitty.ui.coursepage.models

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
