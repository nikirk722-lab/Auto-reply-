package com.example.autoreply

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

/** Handles plain SMS. Chat apps (Messenger/IMO/WhatsApp) go through the notification listener. */
class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val settings = Settings(context)
        if (!settings.enabled || !settings.smsEnabled) return

        // Dual-SIM phones: remember which SIM received the message so we reply from the same SIM.
        val subId = intent.getIntExtra("subscription", intent.getIntExtra("phone", -1))

        for (message in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
            val from = message.originatingAddress ?: continue
            val body = message.messageBody.orEmpty()
            if (body == settings.replyText) continue
            if (!settings.allowedNumber(from)) {
                settings.log("SMS from $from ignored by number filter")
                continue
            }
            if (settings.skipContacts && ContactsHelper.isSavedContact(context, from)) {
                settings.log("SMS from $from ignored — saved contact")
                continue
            }

            val rule = settings.matchRule(body, from, "sms")
            val reply = rule?.reply?.takeIf { it.isNotBlank() } ?: settings.replyText
            val baseDelay = rule?.delay?.takeIf { it > 0 } ?: settings.delaySeconds
            val delay = settings.pickDelay(baseDelay)

            ReplyScheduler.schedule(
                context = context,
                key = "sms:$from",
                target = from,
                transport = "sms",
                delaySeconds = delay,
                subscriptionId = subId,
                replyOverride = reply
            )
            settings.log(
                "SMS from $from (SIM $subId) → ${settings.formatDelay(delay)}" +
                    if (rule != null) " [rule matched]" else ""
            )
        }
    }
}
