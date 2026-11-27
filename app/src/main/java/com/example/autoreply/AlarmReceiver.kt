package com.example.autoreply

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.telephony.SmsManager

/** Fires after the configured delay and actually sends the reply. */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val settings = Settings(context)
        val key = intent.getStringExtra("key") ?: return
        settings.log("Alarm FIRED: $key")

        fun skip(reason: String) {
            settings.log("Skipped $key — $reason")
            settings.skippedCount++
            settings.removePending(key)
        }

        if (!settings.enabled) return skip("AutoReply is OFF")
        if (key !in settings.pendingKeys()) return skip("pending key gone (dismissed?)")
        if (settings.isQuietNow()) return skip("quiet hours")
        if (settings.onlyWhenScreenOff) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (pm.isScreenOn) return skip("screen is on")
        }
        if (settings.respectDnd) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL) {
                return skip("DND is on")
            }
        }
        val cooldown = settings.cooldownSeconds * 1000L
        if (cooldown > 0 && System.currentTimeMillis() - settings.lastReplyAt(key) < cooldown) {
            return skip("cooldown active")
        }
        if (settings.maxPerDay > 0 && settings.repliesToday(key) >= settings.maxPerDay) {
            return skip("daily limit reached")
        }

        val reply = intent.getStringExtra("replyOverride")?.takeIf { it.isNotBlank() }
            ?: settings.replyText
        val target = intent.getStringExtra("target").orEmpty()
        val scheduledAt = intent.getLongExtra("scheduledAt", 0L)
        val subId = intent.getIntExtra("subscriptionId", -1)
        val transport = intent.getStringExtra("transport")

        if (settings.dryRun) {
            settings.log("DRY RUN — would reply to $target: $reply")
            return skip("dry-run mode")
        }

        var sent = false
        when {
            transport == "sms" && alreadyRepliedBySms(context, target, scheduledAt) ->
                skip("you already replied by SMS")
            transport == "sms" -> {
                try {
                    val manager = if (subId >= 0) {
                        SmsManager.getSmsManagerForSubscriptionId(subId)
                    } else SmsManager.getDefault()
                    val piFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    } else PendingIntent.FLAG_UPDATE_CURRENT
                    val sentPi = PendingIntent.getBroadcast(
                        context, key.hashCode(),
                        Intent(SmsStatusReceiver.ACTION_SENT).putExtra("number", target), piFlags
                    )
                    val deliveredPi = PendingIntent.getBroadcast(
                        context, key.hashCode() + 1,
                        Intent(SmsStatusReceiver.ACTION_DELIVERED).putExtra("number", target), piFlags
                    )
                    manager.sendTextMessage(target, null, reply, sentPi, deliveredPi)
                    settings.log("SMS auto-reply sent to $target (SIM $subId)")
                    sent = true
                } catch (e: Exception) {
                    settings.log("SMS reply FAILED to $target: ${e.message}")
                    settings.failedCount++
                }
            }
            transport == "a11y" -> {
                val service = AutoReplyAccessibilityService.instance
                if (service == null) {
                    settings.log("Accessibility service OFF — enable it for $target")
                    settings.failedCount++
                } else {
                    service.sendReply(target, reply, key)
                    settings.log("Accessibility reply started for $target")
                    sent = true
                }
            }
            transport == "notif" -> {
                val pendingIntent: PendingIntent? =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        intent.getParcelableExtra("replyIntent", PendingIntent::class.java)
                    else
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra<PendingIntent>("replyIntent")

                val remoteInput: RemoteInput? =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        intent.getParcelableExtra("remoteInput", RemoteInput::class.java)
                    else
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra<RemoteInput>("remoteInput")

                if (pendingIntent == null || remoteInput == null) {
                    settings.log("No notification reply action for $target")
                    settings.failedCount++
                } else {
                    try {
                        val result = Intent()
                        val results = android.os.Bundle().apply {
                            putCharSequence(remoteInput.resultKey, reply)
                        }
                        RemoteInput.addResultsToIntent(arrayOf(remoteInput), result, results)
                        pendingIntent.send(context, 0, result)
                        settings.log("Notification auto-reply sent to $target")
                        sent = true
                    } catch (e: Exception) {
                        settings.log("Notification reply FAILED to $target: ${e.message}")
                        settings.failedCount++
                    }
                }
            }
            else -> {
                settings.log("Unknown transport for $target")
                settings.failedCount++
            }
        }

        if (sent) {
            settings.markReplied(key)
            settings.countReply(key)
            settings.sentCount++
            notifyUser(context, settings, target)
            settings.removePending(key)
        }
    }

    private fun notifyUser(context: Context, settings: Settings, target: String) {
        if (!settings.notifyOnReply || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification = android.app.Notification.Builder(context, AutoReplyApp.NOTIFY_CHANNEL)
                .setContentTitle("AutoReply")
                .setContentText("Replied to $target")
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setAutoCancel(true)
                .build()
            nm.notify(2001, notification)
        } catch (e: Exception) {
            settings.log("Status notification failed: ${e.message}")
        }
    }

    /** If an outgoing SMS to the same number happened after the incoming one, stay quiet. */
    private fun alreadyRepliedBySms(context: Context, number: String, since: Long): Boolean {
        if (number.isBlank()) return false
        return try {
            val cursor = context.contentResolver.query(
                Uri.parse("content://sms/sent"),
                arrayOf("date"), "address=? AND date>?", arrayOf(number, since.toString()), "date DESC"
            ) ?: return false
            cursor.use { it.moveToFirst() }
        } catch (e: SecurityException) {
            false
        }
    }
}
