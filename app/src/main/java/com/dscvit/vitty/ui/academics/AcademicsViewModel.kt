package com.dscvit.vitty.ui.academics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dscvit.vitty.data.database.VittyDatabase
import com.dscvit.vitty.data.repository.ReminderRepository
import com.dscvit.vitty.ui.coursepage.ReminderNotificationManager
import com.dscvit.vitty.ui.coursepage.models.Reminder
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted

class AcademicsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val reminderRepository: ReminderRepository
    private val reminderNotificationManager: ReminderNotificationManager

    init {
        val database = VittyDatabase.getDatabase(application)
        reminderRepository = ReminderRepository(database.reminderDao())
        reminderNotificationManager = ReminderNotificationManager(application)
    }

    val allReminders: StateFlow<List<Reminder>> =
        reminderRepository.getAllReminders()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun updateReminderStatus(reminderId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            reminderRepository.updateCompletedStatus(reminderId, isCompleted)
            if (isCompleted) {
                reminderNotificationManager.cancelReminderNotification(reminderId)
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderRepository.deleteReminder(reminder, reminder.courseId, reminder.courseTitle, reminder.id)
            reminderNotificationManager.cancelReminderNotification(reminder.id)
        }
    }
}
