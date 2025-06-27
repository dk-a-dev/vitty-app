package com.dscvit.vitty.ui.coursepage

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dscvit.vitty.data.database.VittyDatabase
import com.dscvit.vitty.data.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminder_id", -1)
        val title = intent.getStringExtra("title") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val courseTitle = intent.getStringExtra("course_title") ?: ""

        if (reminderId != -1L) {
            val reminderNotificationManager = ReminderNotificationManager(context)
            reminderNotificationManager.showReminderNotification(
                reminderId = reminderId,
                title = title,
                description = description,
                courseTitle = courseTitle
            )

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = VittyDatabase.getDatabase(context)
                    val reminderRepository = ReminderRepository(database.reminderDao())
                    reminderRepository.updateCompletedStatus(reminderId, true)

                } catch (e: Exception) {
                    Timber.e(e, "Failed to mark reminder as completed")
                }
            }
        } else {
            Timber.e("Invalid reminder ID received in broadcast")
        }
    }
}
