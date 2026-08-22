package com.radwan.nova

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class NovaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val messagesChannel = NotificationChannel(
                CHANNEL_MESSAGES,
                "Messages & Chats",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for direct messages and group chats"
                enableLights(true)
                enableVibration(true)
            }

            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "NOVA Remind Me",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Local reminders set from message layers"
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(messagesChannel)
            manager.createNotificationChannel(remindersChannel)
        }
    }

    companion object {
        const val CHANNEL_MESSAGES = "nova_messages_channel"
        const val CHANNEL_REMINDERS = "nova_reminders_channel"
    }
}
