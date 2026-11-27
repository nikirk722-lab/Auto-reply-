package com.example.autoreply

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Logs SMS sent/delivery reports so you can see what really happened. */
class SmsStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val settings = Settings(context)
        val number = intent.getStringExtra("number").orEmpty()
        when (intent.action) {
            ACTION_SENT -> {
                if (resultCode == Activity.RESULT_OK) {
                    settings.log("SMS report: sent OK to $number")
                } else {
                    settings.log("SMS report: FAILED to $number (code $resultCode)")
                    settings.failedCount++
                }
            }
            ACTION_DELIVERED -> {
                settings.log(
                    if (resultCode == Activity.RESULT_OK) "SMS report: delivered to $number"
                    else "SMS report: not delivered to $number (code $resultCode)"
                )
            }
        }
    }

    companion object {
        const val ACTION_SENT = "com.example.autoreply.SMS_SENT"
        const val ACTION_DELIVERED = "com.example.autoreply.SMS_DELIVERED"
    }
}
