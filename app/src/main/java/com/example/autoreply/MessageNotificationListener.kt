package com.example.autoreply

import android.app.Notification
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Reads incoming chat notifications (Messenger / IMO / WhatsApp and any app you select)
 * and answers through the notification's own Reply button, or through the accessibility
 * fallback when the app offers no Reply action.
 */
class MessageNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        Settings(this).log("Notification listener CONNECTED ✓")
    }

    override fun onListenerDisconnected() {
        Settings(this).log("Notification listener disconnected")
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val settings = Settings(this)
        if (!settings.enabled || !settings.notificationEnabled) return
        val pkg = sbn.packageName
        if (pkg !in settings.packages) return
        if (sbn.notification.flags and Notification.FLAG_ONGOING_EVENT != 0) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        if (title.isNullOrBlank()) return
        val body = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()

        val rule = settings.matchRule(body, title, pkg)
        val reply = rule?.reply?.takeIf { it.isNotBlank() }
            ?: settings.replyFor(pkg)
        if (body == reply || body == settings.replyText) return

        val key = "notif:$pkg:$title"
        val baseDelay = rule?.delay?.takeIf { it > 0 } ?: settings.delayFor(pkg)
        val delay = settings.pickDelay(baseDelay)

        val replyAction = sbn.notification.actions?.firstOrNull {
            !it.remoteInputs.isNullOrEmpty()
        }
        val hasReply = replyAction?.actionIntent != null && !replyAction.remoteInputs.isNullOrEmpty()
        val pref = settings.preferredTransport

        if (hasReply && pref != "a11y") {
            val remoteInput = replyAction!!.remoteInputs!!.first()
            val payload = Bundle().apply {
                putParcelable("replyIntent", replyAction.actionIntent)
                putParcelable("remoteInput", remoteInput)
            }
            ReplyScheduler.schedule(this, key, title, "notif", delay, payload, replyOverride = reply)
            settings.log("Chat $pkg/$title → ${settings.formatDelay(delay)} via notification" +
                if (rule != null) " [rule matched]" else "")
        } else if (pref != "notif") {
            val payload = Bundle().apply { putString("conversation", title) }
            ReplyScheduler.schedule(this, key, pkg, "a11y", delay, payload, replyOverride = reply)
            settings.log("Chat $pkg/$title → ${settings.formatDelay(delay)} via accessibility" +
                if (rule != null) " [rule matched]" else "")
        } else {
            settings.log("Chat $pkg/$title → no Reply action and notification-only mode is on")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        val title = sbn.notification.extras
            .getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: return
        ReplyScheduler.cancel(this, "notif:${sbn.packageName}:$title")
    }
}
