package com.example.teststressappkotlin

import android.R
import android.app.Notification
import android.app.Notification.CATEGORY_SERVICE
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class NotificationModel {
    private val FOREGROUND_CHANNEL_ID = "FOREGROUND_NOTIFICATION"
    private val FRONT_CHANNEL_ID = "FRONT_NOTIFICATION"
    val ACTION_SNOOZE = "ACTION_SNOOZE"
    val LOG_TAG = "myLogs"
    private val context: Context = Constant.context!!

    fun getForegroundNotification(): Notification {
        val builder = NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
            .setContentTitle("Мы следим за вашим состоянием")
            .setContentText("Соединение с устройством установлено")
        val notification = builder.setOngoing(true)
            .setPriority(Notification.PRIORITY_DEFAULT)
            .setCategory(CATEGORY_SERVICE)
            .build()
        builder.setDefaults(Notification.DEFAULT_ALL)
        createNotificationChannel(FOREGROUND_CHANNEL_ID)
        return notification
    }

    fun getDisconnectNotification() {
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(
            context, FRONT_CHANNEL_ID
        )
            .setContentTitle("Произошёл разрыв с устройством")
            .setContentText(
                "Потеряно соединение с устройством, " +
                        "зайдите в приложение, что бы переподключиться"
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val notificationManagerCompat = NotificationManagerCompat.from(context)
        createNotificationChannel(FRONT_CHANNEL_ID)
        notificationManagerCompat.notify(102, builder.build())
    }

    private fun createNotificationChannel(id: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(id, id, importance)
            channel.description = id
            val notificationManager: NotificationManager = context.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}