package dev.braian.goalbit.utils

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import dev.braian.goalbit.R
import dev.braian.goalbit.constants.DataBaseConstants
import dev.braian.goalbit.view.activities.MainActivity

class HabitReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val habitName = intent.getStringExtra(DataBaseConstants.NOTIFICATION.habitName)
        Toast.makeText(context, "Habito nome: $habitName", Toast.LENGTH_LONG).show()

        val notification = buildNotification(context, intent)

        val notificationFixed = 1

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationFixed, notification)
    }

    private fun buildNotification(context: Context, intent: Intent): Notification {

        var icon_resource = R.drawable.baseline_add_alert_24

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            icon_resource = R.drawable.baseline_add_alert_24
        }

        val habitName = intent.getStringExtra(DataBaseConstants.NOTIFICATION.habitName)
        var message = "Your habit: $habitName need attention"
        return Notification.Builder(context, DataBaseConstants.NOTIFICATION.channelID)
            .setSmallIcon(icon_resource)
            .setContentTitle("Goalbit")
            .setContentText(message)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.logo288x))
            .setAutoCancel(true)
            .build()
    }
}
