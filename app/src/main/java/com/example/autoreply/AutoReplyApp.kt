package com.example.autoreply

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class AutoReplyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID, "AutoReply status", NotificationManager.IMPORTANCE_LOW
                ).apply { description = "Replies sent automatically" }
            )
            nm?.createNotificationChannel(
                NotificationChannel(
                    NOTIFY_CHANNEL, "Reply sent", NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Confirmation when an auto-reply is sent" }
            )
        }
    }

    companion object {
        const val CHANNEL_ID = "autoreply_status"
        const val NOTIFY_CHANNEL = "autoreply_reply_sent"
    }
}
