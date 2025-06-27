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

data class Reminder(
    val id: Long = 0,
    val title: String,
    val description: String,
    val dueDate: String,
    val date: String = dueDate,
    val status: ReminderStatus,
    val dateMillis: Long,
    val fromTime: String,
    val toTime: String,
    val isAllDay: Boolean,
    val alertDaysBefore: Int,
    val attachmentUrl: String? = null,
    val courseId: String = "",
    val courseTitle: String = "",
)

enum class ReminderStatus {
    UPCOMING,
    CAN_WAIT,
    COMPLETED,
}

enum class AlertOption(
    val displayText: String,
    val days: Int,
) {
    NONE("None", 0),
    SAME_DAY("Same day", 0),
    ONE_DAY("1 day before", 1),
    TWO_DAYS("2 days before", 2),
    THREE_DAYS("3 days before", 3),
    ONE_WEEK("1 week before", 7),
}
