package com.example.autoreply

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle

object ReplyScheduler {
    private fun flags(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        else PendingIntent.FLAG_UPDATE_CURRENT

    private fun pendingIntent(context: Context, key: String, intent: Intent? = null): PendingIntent {
        val base = intent ?: Intent(context, AlarmReceiver::class.java)
        return PendingIntent.getBroadcast(context, key.hashCode(), base, flags())
    }

    fun schedule(
        context: Context,
        key: String,
        target: String,
        transport: String,
        delaySeconds: Int,
        extraPayload: Bundle? = null,
        subscriptionId: Int = -1,
        replyOverride: String? = null
    ) {
        val settings = Settings(context)
        if (!settings.enabled) return
        cancel(context, key)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("key", key)
            putExtra("target", target)
            putExtra("transport", transport)
            putExtra("scheduledAt", System.currentTimeMillis())
            putExtra("subscriptionId", subscriptionId)
            if (!replyOverride.isNullOrBlank()) putExtra("replyOverride", replyOverride)
            if (extraPayload != null) putExtras(extraPayload)
        }

        val seconds = delaySeconds.coerceIn(5, 86400)
        val triggerAt = System.currentTimeMillis() + seconds * 1000L
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarm.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent(context, key, intent)
            )
        } else {
            alarm.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent(context, key, intent))
        }
        settings.addPending(key)
        settings.log("Alarm set: $key → reply in ${settings.formatDelay(seconds)}")
    }

    fun cancel(context: Context, key: String) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pendingIntent(context, key))
        Settings(context).removePending(key)
    }
}
