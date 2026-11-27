package com.example.autoreply

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Fallback for chat apps whose notifications have no "Reply" button (e.g. IMO).
 * After the delay it opens the app, types the configured text into the message
 * field and taps Send. It never acts until the alarm from AlarmReceiver calls it.
 */
class AutoReplyAccessibilityService : AccessibilityService() {
    companion object {
        @Volatile
        var instance: AutoReplyAccessibilityService? = null
    }

    private data class Job(val pkg: String, val reply: String, val key: String)

    private val handler = Handler(Looper.getMainLooper())
    private var job: Job? = null

    private val fieldHint = Regex("message|reply|type|write|msg|মেসেজ|লিখুন|বার্তা", RegexOption.IGNORE_CASE)
    private val badHint = Regex("search|quantity|amount|খোঁজ", RegexOption.IGNORE_CASE)
    private val sendHint = Regex("send|পাঠান|প্রেরণ|submit", RegexOption.IGNORE_CASE)

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    fun sendReply(pkg: String, reply: String, key: String) {
        job = Job(pkg, reply, key)
        Settings(this).log("A11Y: waiting for $pkg window ($key)")
        bringToFront(pkg)
        for (attempt in 0..5) {
            handler.postDelayed({ trySend(attempt) }, 900L + attempt * 600L)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val current = job ?: return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.packageName?.toString() == current.pkg
        ) {
            handler.postDelayed({ trySend(0) }, 700)
        }
    }

    private fun bringToFront(pkg: String) {
        try {
            val launch = packageManager.getLaunchIntentForPackage(pkg) ?: run {
                Settings(this).log("A11Y: no launch intent for $pkg")
                return
            }
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(launch)
        } catch (e: Exception) {
            Settings(this).log("A11Y: cannot open $pkg: ${e.message}")
        }
    }

    private fun trySend(attempt: Int) {
        val current = job ?: return
        val settings = Settings(this)
        if (!settings.enabled) {
            job = null
            return
        }

        val root = rootInActiveWindow
        if (root == null) {
            if (attempt == 5) settings.log("A11Y: no window content for ${current.pkg}")
            return
        }

        val nodes = mutableListOf<AccessibilityNodeInfo>()
        collect(root, nodes)

        val editable = nodes.filter { it.isEditable && it.isVisibleToUser }
            .maxByOrNull { fieldScore(it) }
        if (editable == null) {
            if (attempt == 5) settings.log("A11Y: no message field found in ${current.pkg}")
            return
        }

        val args = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                current.reply
            )
        }
        val typed = editable.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        if (!typed) {
            if (attempt == 5) settings.log("A11Y: typing failed in ${current.pkg}")
            return
        }

        handler.postDelayed({
            val send = nodes.filter { it.isClickable && it.isEnabled && it.isVisibleToUser }
                .maxByOrNull { sendScore(it) }
            val clicked = send?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true
            settings.log(
                if (clicked) "A11Y: reply sent in ${current.pkg} (${current.key})"
                else "A11Y: typed but no Send button in ${current.pkg}"
            )
            job = null
        }, 350)
    }

    private fun collect(node: AccessibilityNodeInfo?, out: MutableList<AccessibilityNodeInfo>) {
        if (node == null) return
        out.add(node)
        for (i in 0 until node.childCount) {
            collect(node.getChild(i), out)
        }
    }

    private fun fieldScore(node: AccessibilityNodeInfo): Int {
        val text = listOfNotNull(
            node.hintText, node.contentDescription, node.text
        ).joinToString(" ")
        var score = 0
        if (fieldHint.containsMatchIn(text)) score += 10
        if (badHint.containsMatchIn(text)) score -= 5
        return score
    }

    private fun sendScore(node: AccessibilityNodeInfo): Int {
        val tooltip = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) node.tooltipText else null
        val text = listOfNotNull(
            node.contentDescription, node.text, tooltip
        ).joinToString(" ")
        return if (sendHint.containsMatchIn(text)) 10 else 0
    }
}
