package com.example.autoreply

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.telephony.SmsManager
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : Activity() {
    private lateinit var settings: Settings
    private lateinit var switchEnabled: Switch
    private lateinit var switchSms: Switch
    private lateinit var switchNotif: Switch
    private lateinit var editReply: EditText
    private lateinit var editDelay: EditText
    private lateinit var switchRandomDelay: Switch
    private lateinit var editMaxDelay: EditText
    private lateinit var textSelected: TextView
    private lateinit var spinnerMethod: Spinner
    private lateinit var switchQuiet: Switch
    private lateinit var editQuietStart: EditText
    private lateinit var editQuietEnd: EditText
    private lateinit var switchScreenOff: Switch
    private lateinit var switchDnd: Switch
    private lateinit var switchSkipContacts: Switch
    private lateinit var switchDryRun: Switch
    private lateinit var switchNotifyOn: Switch
    private lateinit var editMaxPerDay: EditText
    private lateinit var editCooldown: EditText
    private lateinit var switchOnlyNumbers: Switch
    private lateinit var editOnlyNumbers: EditText
    private lateinit var editIgnoredNumbers: EditText
    private lateinit var editTestNumber: EditText
    private lateinit var textStatus: TextView
    private lateinit var textStats: TextView
    private lateinit var textLog: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        settings = Settings(this)

        switchEnabled = findViewById(R.id.switchEnabled)
        switchSms = findViewById(R.id.switchSms)
        switchNotif = findViewById(R.id.switchNotif)
        editReply = findViewById(R.id.editReply)
        editDelay = findViewById(R.id.editDelay)
        switchRandomDelay = findViewById(R.id.switchRandomDelay)
        editMaxDelay = findViewById(R.id.editMaxDelay)
        textSelected = findViewById(R.id.textSelected)
        spinnerMethod = findViewById(R.id.spinnerMethod)
        switchQuiet = findViewById(R.id.switchQuiet)
        editQuietStart = findViewById(R.id.editQuietStart)
        editQuietEnd = findViewById(R.id.editQuietEnd)
        switchScreenOff = findViewById(R.id.switchScreenOff)
        switchDnd = findViewById(R.id.switchDnd)
        switchSkipContacts = findViewById(R.id.switchSkipContacts)
        switchDryRun = findViewById(R.id.switchDryRun)
        switchNotifyOn = findViewById(R.id.switchNotifyOn)
        editMaxPerDay = findViewById(R.id.editMaxPerDay)
        editCooldown = findViewById(R.id.editCooldown)
        switchOnlyNumbers = findViewById(R.id.switchOnlyNumbers)
        editOnlyNumbers = findViewById(R.id.editOnlyNumbers)
        editIgnoredNumbers = findViewById(R.id.editIgnoredNumbers)
        editTestNumber = findViewById(R.id.editTestNumber)
        textStatus = findViewById(R.id.textStatus)
        textStats = findViewById(R.id.textStats)
        textLog = findViewById(R.id.textLog)

        loadSettings()

        findViewById<Button>(R.id.btnSave).setOnClickListener { saveSettings() }
        findViewById<Button>(R.id.btnPickApps).setOnClickListener {
            startActivity(Intent(this, AppPickerActivity::class.java))
        }
        findViewById<Button>(R.id.btnAppRules).setOnClickListener {
            if (settings.packages.isEmpty()) {
                Toast.makeText(this, "আগে Select apps চাপো", Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(this, RulesActivity::class.java))
            }
        }
        findViewById<Button>(R.id.btnSmartRules).setOnClickListener {
            startActivity(Intent(this, RulesEditorActivity::class.java))
        }
        findViewById<Button>(R.id.btnSmsPerm).setOnClickListener { requestSmsPermissions() }
        findViewById<Button>(R.id.btnContactsPerm).setOnClickListener {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_CONTACTS), 3
            )
        }
        findViewById<Button>(R.id.btnNotifAccess).setOnClickListener {
            startActivity(Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
        findViewById<Button>(R.id.btnNotifPerm).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 2
                )
            } else {
                Toast.makeText(this, "Not needed on this Android version", Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<Button>(R.id.btnA11yPerm).setOnClickListener {
            startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        findViewById<Button>(R.id.btnBattery).setOnClickListener {
            try {
                startActivity(
                    Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        .setData(Uri.parse("package:$packageName"))
                )
            } catch (e: Exception) {
                Toast.makeText(this, "This phone does not support this option", Toast.LENGTH_LONG).show()
            }
        }
        findViewById<Button>(R.id.btnTest).setOnClickListener { sendTest() }
        findViewById<Button>(R.id.btnRefresh).setOnClickListener { refreshAll() }
        findViewById<Button>(R.id.btnShareLog).setOnClickListener {
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "AutoReply log")
                putExtra(Intent.EXTRA_TEXT, settings.logs.ifBlank { "No activity yet." })
            }
            startActivity(Intent.createChooser(share, "Share log"))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshSelectedSummary()
        refreshAll()
    }

    private fun refreshSelectedSummary() {
        textSelected.text = if (settings.packages.isEmpty()) {
            "No apps selected — tap the button above."
        } else {
            "Selected ${settings.packages.size} app(s): " +
                settings.packages.sorted().take(6).joinToString(", ")
        }
    }

    private fun refreshAll() {
        refreshStatus()
        textStats.text = "Sent: ${settings.sentCount}   •   Skipped: ${settings.skippedCount}" +
            "   •   Failed: ${settings.failedCount}"
        textLog.text = settings.logs.ifBlank { "No activity yet." }
    }

    private fun loadSettings() {
        switchEnabled.isChecked = settings.enabled
        switchSms.isChecked = settings.smsEnabled
        switchNotif.isChecked = settings.notificationEnabled
        editReply.setText(settings.replyText)
        editDelay.setText(settings.delaySeconds.toString())
        switchRandomDelay.isChecked = settings.randomDelay
        editMaxDelay.setText(settings.maxDelaySeconds.toString())
        spinnerMethod.setSelection(
            Settings.METHODS.indexOf(settings.preferredTransport).coerceAtLeast(0)
        )
        switchQuiet.isChecked = settings.quietEnabled
        editQuietStart.setText(settings.quietStart)
        editQuietEnd.setText(settings.quietEnd)
        switchScreenOff.isChecked = settings.onlyWhenScreenOff
        switchDnd.isChecked = settings.respectDnd
        switchSkipContacts.isChecked = settings.skipContacts
        switchDryRun.isChecked = settings.dryRun
        switchNotifyOn.isChecked = settings.notifyOnReply
        editMaxPerDay.setText(settings.maxPerDay.toString())
        editCooldown.setText(settings.cooldownSeconds.toString())
        switchOnlyNumbers.isChecked = settings.onlyNumbersEnabled
        editOnlyNumbers.setText(settings.onlyNumbers.joinToString("\n"))
        editIgnoredNumbers.setText(settings.ignoredNumbers.joinToString("\n"))
        refreshSelectedSummary()
    }

    private fun saveSettings() {
        settings.enabled = switchEnabled.isChecked
        settings.smsEnabled = switchSms.isChecked
        settings.notificationEnabled = switchNotif.isChecked
        settings.replyText = editReply.text.toString().ifBlank { settings.replyText }
        settings.delaySeconds = editDelay.text.toString().toIntOrNull() ?: 60
        settings.randomDelay = switchRandomDelay.isChecked
        settings.maxDelaySeconds = editMaxDelay.text.toString().toIntOrNull()
            ?: settings.delaySeconds
        settings.preferredTransport = Settings.METHODS.getOrElse(spinnerMethod.selectedItemPosition) { "auto" }
        settings.quietEnabled = switchQuiet.isChecked
        settings.quietStart = editQuietStart.text.toString().ifBlank { "22:00" }
        settings.quietEnd = editQuietEnd.text.toString().ifBlank { "07:00" }
        settings.onlyWhenScreenOff = switchScreenOff.isChecked
        settings.respectDnd = switchDnd.isChecked
        settings.skipContacts = switchSkipContacts.isChecked
        settings.dryRun = switchDryRun.isChecked
        settings.notifyOnReply = switchNotifyOn.isChecked
        settings.maxPerDay = editMaxPerDay.text.toString().toIntOrNull() ?: 0
        settings.cooldownSeconds = editCooldown.text.toString().toIntOrNull() ?: 600
        settings.onlyNumbersEnabled = switchOnlyNumbers.isChecked
        settings.onlyNumbers = parseNumbers(editOnlyNumbers.text.toString())
        settings.ignoredNumbers = parseNumbers(editIgnoredNumbers.text.toString())

        settings.log("Settings saved — delay=${settings.formatDelay(settings.delaySeconds)}")
        Toast.makeText(this, "Saved ✓", Toast.LENGTH_SHORT).show()
        refreshAll()
    }

    private fun parseNumbers(text: String): Set<String> =
        text.lines().map { it.trim() }.filter { it.isNotBlank() }.toSet()

    private fun sendTest() {
        val number = editTestNumber.text.toString().trim()
        if (number.length < 5) {
            Toast.makeText(this, "টেস্ট নম্বর লিখো আগে", Toast.LENGTH_SHORT).show()
            return
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "SMS permission আগে দিও", Toast.LENGTH_SHORT).show()
            requestSmsPermissions()
            return
        }
        try {
            SmsManager.getDefault().sendTextMessage(number, null, settings.replyText, null, null)
            settings.log("TEST reply sent to $number")
            Toast.makeText(this, "TEST SMS sent ✓", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            settings.log("TEST SMS FAILED: ${e.message}")
            Toast.makeText(this, "FAILED: ${e.message}", Toast.LENGTH_LONG).show()
        }
        refreshAll()
    }

    private fun requestSmsPermissions() {
        val needed = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            needed.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val missing = needed.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) {
            Toast.makeText(this, "SMS permissions already granted", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 1)
        }
    }

    private fun refreshStatus() {
        val smsOk = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) ==
                PackageManager.PERMISSION_GRANTED
        val listeners = android.provider.Settings.Secure.getString(
            contentResolver, "enabled_notification_listeners"
        ).orEmpty()
        val notifOk = listeners.contains(packageName)
        val a11yServices = android.provider.Settings.Secure.getString(
            contentResolver, android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        val a11yOk = a11yServices.contains("$packageName/.AutoReplyAccessibilityService")
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        val batteryOk = pm.isIgnoringBatteryOptimizations(packageName)
        val contactsOk = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED
        textStatus.text = buildString {
            append("SMS permission: ").append(if (smsOk) "✓" else "✗ (tap Grant SMS)")
            append('\n')
            append("Notification access: ").append(if (notifOk) "✓" else "✗ (tap Open Notification access)")
            append('\n')
            append("Accessibility: ").append(if (a11yOk) "✓" else "✗ (tap Enable Accessibility)")
            append('\n')
            append("Battery unrestricted: ").append(if (batteryOk) "✓" else "✗ (tap Battery button)")
            append('\n')
            append("Contacts (for skip): ").append(if (contactsOk) "✓" else "✗ (optional)")
            append('\n')
            append("Mode: ").append(if (settings.dryRun) "DRY RUN (nothing sent)" else "LIVE")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        refreshAll()
    }
}
