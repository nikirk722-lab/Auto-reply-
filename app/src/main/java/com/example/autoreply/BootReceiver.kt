package com.example.autoreply

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Keeps the app aware of reboots; the system restarts enabled listener/accessibility services itself. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Settings(context).log("Device booted — AutoReply ready")
    }
}
