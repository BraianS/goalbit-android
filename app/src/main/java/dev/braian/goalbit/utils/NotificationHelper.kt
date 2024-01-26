package dev.braian.goalbit.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import dev.braian.goalbit.constants.DataBaseConstants
import java.util.Calendar

class NotificationHelper(private val context: Context) {
    fun createNotificationChannel() {
        Log.i(DataBaseConstants.TAG.NOTIFICATION_HELPER, "Notification Created ")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                DataBaseConstants.NOTIFICATION.channelID,
                DataBaseConstants.NOTIFICATION.channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun cancelNotification(habit: Int) {
        val intent = Intent(context, HabitReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habit,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleNotification(context: Context, intent: Intent) {

        val hour = intent.getIntExtra(DataBaseConstants.NOTIFICATION.HOUR, -1)
        val minute = intent.getIntExtra(DataBaseConstants.NOTIFICATION.MINUTE, -1)
        val habitId = intent.getIntExtra(DataBaseConstants.NOTIFICATION.habitId, -1)
        val habitName = intent.getIntExtra(DataBaseConstants.NOTIFICATION.habitName, -1)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val now = Calendar.getInstance()

        val nextDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (nextDay.before(now)) {
            nextDay.add(Calendar.DAY_OF_MONTH, 1)
        }

        val nextDayTime = nextDay.timeInMillis
        Log.i(DataBaseConstants.TAG.ADD_HABIT_FORM_ACTIVITY, "Time in millis: $nextDayTime")

        if (Build.VERSION.SDK_INT >= 23) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextDayTime,
                pendingIntent
            )
        } else if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                nextDayTime,
                pendingIntent
            )
        } else {
            alarmManager[AlarmManager.RTC_WAKEUP, nextDayTime] = pendingIntent
        }
    }
}