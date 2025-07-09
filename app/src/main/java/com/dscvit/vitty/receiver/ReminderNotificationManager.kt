package com.dscvit.vitty.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dscvit.vitty.R
import com.dscvit.vitty.util.Constants
import com.dscvit.vitty.util.NotificationPermissionHelper
import timber.log.Timber

class ReminderNotificationManager(
    private val context: Context,
) {
    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel =
            NotificationChannel(
                Constants.REMINDER_CHANNEL_ID,
                Constants.REMINDER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = Constants.REMINDER_CHANNEL_DESC
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
            }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun scheduleReminderNotification(
        reminderId: Long,
        title: String,
        description: String,
        triggerTimeMillis: Long,
        courseTitle: String,
    ) {
        val hasNotificationPermission = NotificationPermissionHelper.hasNotificationPermission(context)
        val canScheduleExactAlarms = NotificationPermissionHelper.canScheduleExactAlarms(context)

        if (!hasNotificationPermission) {
            Timber.e("No notification permission! Reminders won't work.")
            return
        }

        if (!canScheduleExactAlarms) {
            Timber.w("Cannot schedule exact alarms! Notifications may be delayed.")
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent =
            Intent(context, ReminderBroadcastReceiver::class.java).apply {
                putExtra("reminder_id", reminderId)
                putExtra("title", title)
                putExtra("description", description)
                putExtra("course_title", courseTitle)
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                reminderId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        try {
            if (canScheduleExactAlarms) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent,
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent,
                )
            }
        } catch (e: SecurityException) {
            Timber.e(e, "Failed to schedule reminder notification")
        }
    }

    fun cancelReminderNotification(reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                reminderId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        alarmManager.cancel(pendingIntent)
    }

    fun showReminderNotification(
        reminderId: Long,
        title: String,
        description: String,
        courseTitle: String,
    ) {
        val notification =
            NotificationCompat
                .Builder(context, Constants.REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Reminder: $title")
                .setContentText("$courseTitle - $description")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .build()

        try {
            NotificationManagerCompat.from(context).notify(reminderId.toInt(), notification)
        } catch (e: SecurityException) {
            Timber.e(e, "Failed to show notification")
        }
    }
}
