package com.example.autoreply

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import kotlin.random.Random

/** All configuration lives in SharedPreferences so every component can read it. */
class Settings(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("autoreply", Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = prefs.getBoolean("enabled", true)
        set(value) = prefs.edit().putBoolean("enabled", value).apply()

    var smsEnabled: Boolean
        get() = prefs.getBoolean("sms_enabled", true)
        set(value) = prefs.edit().putBoolean("sms_enabled", value).apply()

    var notificationEnabled: Boolean
        get() = prefs.getBoolean("notif_enabled", true)
        set(value) = prefs.edit().putBoolean("notif_enabled", value).apply()

    var replyText: String
        get() = prefs.getString("reply_text", DEFAULT_REPLY) ?: DEFAULT_REPLY
        set(value) = prefs.edit().putString("reply_text", value).apply()

    /** Minimum delay in SECONDS. */
    var delaySeconds: Int
        get() = prefs.getInt("delay_seconds", 60)
        set(value) = prefs.edit().putInt("delay_seconds", value.coerceIn(5, 86400)).apply()

    /** If random delay is on, actual delay is picked between delaySeconds and maxDelaySeconds. */
    var randomDelay: Boolean
        get() = prefs.getBoolean("random_delay", false)
        set(value) = prefs.edit().putBoolean("random_delay", value).apply()

    var maxDelaySeconds: Int
        get() = prefs.getInt("max_delay_seconds", 60)
        set(value) = prefs.edit().putInt("max_delay_seconds", value.coerceIn(5, 86400)).apply()

    fun pickDelay(base: Int): Int =
        if (randomDelay && maxDelaySeconds > base) Random.nextInt(base, maxDelaySeconds + 1)
        else base

    var packages: Set<String>
        get() = prefs.getStringSet("packages", DEFAULT_PACKAGES) ?: DEFAULT_PACKAGES
        set(value) = prefs.edit().putStringSet("packages", value).apply()

    var preferredTransport: String
        get() = prefs.getString("transport", "auto") ?: "auto"
        set(value) = prefs.edit().putString("transport", value).apply()

    var quietEnabled: Boolean
        get() = prefs.getBoolean("quiet_enabled", false)
        set(value) = prefs.edit().putBoolean("quiet_enabled", value).apply()

    var quietStart: String
        get() = prefs.getString("quiet_start", "22:00") ?: "22:00"
        set(value) = prefs.edit().putString("quiet_start", value).apply()

    var quietEnd: String
        get() = prefs.getString("quiet_end", "07:00") ?: "07:00"
        set(value) = prefs.edit().putString("quiet_end", value).apply()

    var onlyWhenScreenOff: Boolean
        get() = prefs.getBoolean("screen_off_only", false)
        set(value) = prefs.edit().putBoolean("screen_off_only", value).apply()

    var respectDnd: Boolean
        get() = prefs.getBoolean("respect_dnd", false)
        set(value) = prefs.edit().putBoolean("respect_dnd", value).apply()

    var cooldownSeconds: Int
        get() = prefs.getInt("cooldown_seconds", 600)
        set(value) = prefs.edit().putInt("cooldown_seconds", value.coerceIn(0, 86400)).apply()

    /** 0 = unlimited. */
    var maxPerDay: Int
        get() = prefs.getInt("max_per_day", 0)
        set(value) = prefs.edit().putInt("max_per_day", value.coerceIn(0, 100)).apply()

    var onlyNumbersEnabled: Boolean
        get() = prefs.getBoolean("only_numbers_enabled", false)
        set(value) = prefs.edit().putBoolean("only_numbers_enabled", value).apply()

    var onlyNumbers: Set<String>
        get() = prefs.getStringSet("only_numbers", emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet("only_numbers", value).apply()

    var ignoredNumbers: Set<String>
        get() = prefs.getStringSet("ignored_numbers", emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet("ignored_numbers", value).apply()

    var skipContacts: Boolean
        get() = prefs.getBoolean("skip_contacts", false)
        set(value) = prefs.edit().putBoolean("skip_contacts", value).apply()

    var dryRun: Boolean
        get() = prefs.getBoolean("dry_run", false)
        set(value) = prefs.edit().putBoolean("dry_run", value).apply()

    var notifyOnReply: Boolean
        get() = prefs.getBoolean("notify_on_reply", true)
        set(value) = prefs.edit().putBoolean("notify_on_reply", value).apply()

    var sentCount: Int
        get() = prefs.getInt("sent_count", 0)
        set(value) = prefs.edit().putInt("sent_count", value).apply()

    var skippedCount: Int
        get() = prefs.getInt("skipped_count", 0)
        set(value) = prefs.edit().putInt("skipped_count", value).apply()

    var failedCount: Int
        get() = prefs.getInt("failed_count", 0)
        set(value) = prefs.edit().putInt("failed_count", value).apply()

    // ---------- JSON-backed maps ----------

    private fun jsonMap(key: String): JSONObject =
        try { JSONObject(prefs.getString(key, "{}") ?: "{}") } catch (e: Exception) { JSONObject() }

    private fun saveMap(key: String, map: JSONObject) {
        prefs.edit().putString(key, map.toString()).apply()
    }

    fun replyFor(pkg: String): String =
        jsonMap("app_replies").optString(pkg, "").ifBlank { replyText }

    fun delayFor(pkg: String): Int {
        val v = jsonMap("app_delays").optInt(pkg, -1)
        return if (v > 0) v else delaySeconds
    }

    fun setAppReply(pkg: String, text: String) {
        val map = jsonMap("app_replies")
        if (text.isBlank()) map.remove(pkg) else map.put(pkg, text)
        saveMap("app_replies", map)
    }

    fun setAppDelay(pkg: String, seconds: Int) {
        val map = jsonMap("app_delays")
        if (seconds <= 0) map.remove(pkg) else map.put(pkg, seconds.coerceIn(5, 86400))
        saveMap("app_delays", map)
    }

    fun appReply(pkg: String): String = jsonMap("app_replies").optString(pkg, "")
    fun appDelay(pkg: String): Int = jsonMap("app_delays").optInt(pkg, 0)

    fun lastReplyAt(key: String): Long = jsonMap("last_replied").optLong(key, 0L)

    fun markReplied(key: String) {
        val map = jsonMap("last_replied")
        map.put(key, System.currentTimeMillis())
        saveMap("last_replied", map)
    }

    private fun todayTag(): String {
        val c = Calendar.getInstance()
        return "${c.get(Calendar.YEAR)}-${c.get(Calendar.DAY_OF_YEAR)}"
    }

    fun repliesToday(key: String): Int {
        val raw = jsonMap("daily_counts").optString(key, "")
        val parts = raw.split(":")
        return if (parts.size == 2 && parts[0] == todayTag()) parts[1].toIntOrNull() ?: 0 else 0
    }

    fun countReply(key: String) {
        val map = jsonMap("daily_counts")
        map.put(key, "${todayTag()}:${repliesToday(key) + 1}")
        saveMap("daily_counts", map)
    }

    // ---------- Smart keyword rules ----------

    data class Rule(
        var id: Int = 0,
        var target: String = "",
        var keyword: String = "",
        var reply: String = "",
        var delay: Int = 0,
        var enabled: Boolean = true
    )

    fun rules(): List<Rule> {
        val result = mutableListOf<Rule>()
        try {
            val arr = JSONArray(prefs.getString("rules", "[]") ?: "[]")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                result.add(
                    Rule(
                        id = o.optInt("id", i),
                        target = o.optString("target", ""),
                        keyword = o.optString("keyword", ""),
                        reply = o.optString("reply", ""),
                        delay = o.optInt("delay", 0),
                        enabled = o.optBoolean("enabled", true)
                    )
                )
            }
        } catch (e: Exception) { /* ignore corrupt data */ }
        return result
    }

    fun saveRules(list: List<Rule>) {
        val arr = JSONArray()
        for (r in list) {
            val o = JSONObject()
            o.put("id", r.id)
            o.put("target", r.target)
            o.put("keyword", r.keyword)
            o.put("reply", r.reply)
            o.put("delay", r.delay)
            o.put("enabled", r.enabled)
            arr.put(o)
        }
        prefs.edit().putString("rules", arr.toString()).apply()
    }

    /** First matching enabled rule wins. Target can be a number, package name or conversation title. */
    fun matchRule(body: String, target: String, pkg: String): Rule? {
        for (rule in rules()) {
            if (!rule.enabled) continue
            val t = rule.target.trim()
            if (t.isNotBlank() && !target.contains(t, true) && !pkg.contains(t, true)) continue
            val k = rule.keyword.trim()
            if (k.isBlank()) continue
            val matched = if (k.startsWith("re:", true)) {
                try { Regex(k.removePrefix("re:"), RegexOption.IGNORE_CASE).containsMatchIn(body) }
                catch (e: Exception) { false }
            } else {
                body.contains(k, true)
            }
            if (matched) return rule
        }
        return null
    }

    // ---------- Pending alarms ----------

    fun pendingKeys(): MutableSet<String> =
        prefs.getStringSet("pending", emptySet())?.toMutableSet() ?: mutableSetOf()

    fun addPending(key: String) {
        val keys = pendingKeys().apply { add(key) }
        prefs.edit().putStringSet("pending", keys).apply()
    }

    fun removePending(key: String) {
        val keys = pendingKeys().apply { remove(key) }
        prefs.edit().putStringSet("pending", keys).apply()
    }

    // ---------- Logs ----------

    var logs: String
        get() = prefs.getString("logs", "") ?: ""
        set(value) = prefs.edit().putString("logs", value.take(16000)).apply()

    fun log(line: String) {
        val stamp = java.text.SimpleDateFormat("MM-dd HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        logs = "[$stamp] $line\n$logs"
    }

    // ---------- Helpers ----------

    fun isQuietNow(): Boolean {
        if (!quietEnabled) return false
        val start = parseTime(quietStart) ?: return false
        val end = parseTime(quietEnd) ?: return false
        val now = Calendar.getInstance()
        val cur = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        return if (start <= end) cur in start until end else cur >= start || cur < end
    }

    private fun parseTime(value: String): Int? {
        val parts = value.split(":")
        if (parts.size != 2) return null
        val h = parts[0].trim().toIntOrNull() ?: return null
        val m = parts[1].trim().toIntOrNull() ?: return null
        return if (h in 0..23 && m in 0..59) h * 60 + m else null
    }

    private fun digits(value: String): String = value.filter { it.isDigit() }
    private fun norm(value: String): String = digits(value).takeLast(10)

    fun allowedNumber(raw: String): Boolean {
        val n = norm(raw)
        if (n.length < 7) return true
        if (ignoredNumbers.any { norm(it) == n }) return false
        if (onlyNumbersEnabled && onlyNumbers.none { norm(it) == n }) return false
        return true
    }

    fun formatDelay(seconds: Int): String = when {
        seconds < 60 -> "$seconds sec"
        seconds % 3600 == 0 -> "${seconds / 3600} hour"
        seconds % 60 == 0 -> "${seconds / 60} min"
        else -> "$seconds sec"
    }

    companion object {
        const val DEFAULT_REPLY = "Jack is out right now. I'll reply as soon as I'm back."
        val DEFAULT_PACKAGES = setOf(
            "com.facebook.orca", "com.imo.android.imoim", "com.whatsapp"
        )
        val METHODS = arrayOf("auto", "notif", "a11y")
    }
}
