package com.dscvit.vitty.data.repository

import com.dscvit.vitty.data.dao.ReminderDao
import com.dscvit.vitty.data.entity.ReminderEntity
import com.dscvit.vitty.ui.coursepage.models.Reminder
import com.dscvit.vitty.ui.coursepage.models.ReminderStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

class ReminderRepository(
    private val reminderDao: ReminderDao
) {
    fun getRemindersByCourse(courseId: String): Flow<List<Reminder>> =
        reminderDao.getRemindersByCourse(courseId).map { entities ->
            entities.map { it.toReminder() }
        }

    fun getAllReminders(): Flow<List<Reminder>> =
        reminderDao.getAllReminders().map { entities ->
            entities.map { it.toReminder() }
        }

    suspend fun insertReminder(reminder: Reminder, courseId: String, courseTitle: String): Long {
        return reminderDao.insertReminder(reminder.toEntity(courseId, courseTitle))
    }

    suspend fun updateReminder(reminder: Reminder, courseId: String, courseTitle: String, id: Long) {
        reminderDao.updateReminder(reminder.toEntity(courseId, courseTitle, id))
    }

    suspend fun deleteReminder(reminder: Reminder, courseId: String, courseTitle: String, id: Long) {
        reminderDao.deleteReminder(reminder.toEntity(courseId, courseTitle, id))
    }

    suspend fun deleteRemindersByCourse(courseId: String) {
        reminderDao.deleteRemindersByCourse(courseId)
    }

    suspend fun updateCompletedStatus(id: Long, isCompleted: Boolean) {
        reminderDao.updateCompletedStatus(id, isCompleted)
    }

    suspend fun getAllPendingReminders(): List<Reminder> {
        return reminderDao.getAllPendingReminders().map { it.toReminder() }
    }

    suspend fun getRemindersInRange(startTime: Long, endTime: Long): List<Reminder> {
        return reminderDao.getRemindersInRange(startTime, endTime).map { it.toReminder() }
    }

    private fun ReminderEntity.toReminder(): Reminder {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateMillis
        val currentTime = System.currentTimeMillis()
        
        val status = when {
            isCompleted -> ReminderStatus.COMPLETED
            dateMillis < currentTime -> ReminderStatus.UPCOMING
            else -> ReminderStatus.CAN_WAIT
        }
        
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        val fullDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        return Reminder(
            id = id,
            title = title,
            description = description,
            dueDate = dateFormat.format(Date(dateMillis)),
            date = fullDateFormat.format(Date(dateMillis)),
            status = status,
            dateMillis = dateMillis,
            fromTime = if (isAllDay) "" else String.format("%02d:%02d", fromTimeHour, fromTimeMinute),
            toTime = if (isAllDay) "" else String.format("%02d:%02d", toTimeHour, toTimeMinute),
            isAllDay = isAllDay,
            alertDaysBefore = alertDaysBefore,
            attachmentUrl = attachmentUrl,
            courseId = courseId,
            courseTitle = courseTitle
        )
    }

    private fun Reminder.toEntity(courseId: String, courseTitle: String, id: Long = 0): ReminderEntity {
        val fromTimeParts = fromTime.split(":")
        val toTimeParts = toTime.split(":")
        
        return ReminderEntity(
            id = if (id == 0L) this.id else id,
            courseId = if (courseId.isNotEmpty()) courseId else this.courseId,
            courseTitle = if (courseTitle.isNotEmpty()) courseTitle else this.courseTitle,
            title = title,
            description = description,
            dateMillis = dateMillis,
            fromTimeHour = if (fromTimeParts.size >= 2) fromTimeParts[0].toIntOrNull() ?: 0 else 0,
            fromTimeMinute = if (fromTimeParts.size >= 2) fromTimeParts[1].toIntOrNull() ?: 0 else 0,
            toTimeHour = if (toTimeParts.size >= 2) toTimeParts[0].toIntOrNull() ?: 0 else 0,
            toTimeMinute = if (toTimeParts.size >= 2) toTimeParts[1].toIntOrNull() ?: 0 else 0,
            isAllDay = isAllDay,
            alertDaysBefore = alertDaysBefore,
            attachmentUrl = attachmentUrl,
            isCompleted = status == ReminderStatus.COMPLETED,
            updatedAt = System.currentTimeMillis()
        )
    }
}
