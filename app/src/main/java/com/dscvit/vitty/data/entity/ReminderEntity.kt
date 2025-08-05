package com.dscvit.vitty.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseId: String,
    val courseTitle: String, 
    val title: String,
    val description: String,
    val dateMillis: Long,
    val fromTimeHour: Int,
    val fromTimeMinute: Int,
    val toTimeHour: Int,
    val toTimeMinute: Int,
    val isAllDay: Boolean,
    val alertDaysBefore: Int, 
    val attachmentUrl: String? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
