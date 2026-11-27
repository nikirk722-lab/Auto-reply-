package com.example.autoreply

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/** Per-app reply text and per-app delay — every app can behave differently. */
class RulesActivity : Activity() {
    private lateinit var settings: Settings
    private val replies = mutableMapOf<String, EditText>()
    private val delays = mutableMapOf<String, EditText>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = Settings(this)
        title = "Per-app replies & delays"

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(list)

        val save = Button(this).apply { text = "Save ✓" }
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        root.addView(save)
        setContentView(root)

        val pm = packageManager
        if (settings.packages.isEmpty()) {
            list.addView(TextView(this).apply {
                text = "কোনো অ্যাপ সিলেক্ট করা নেই। আগে 'Select apps' চাপো।"
                setPadding(16, 16, 16, 16)
            })
        }
        for (pkg in settings.packages.sorted()) {
            val label = try {
                pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
            } catch (e: Exception) { pkg }

            list.addView(TextView(this).apply {
                text = "$label\n$pkg"
                textSize = 15f
                setPadding(0, 24, 0, 4)
            })

            val reply = EditText(this).apply {
                hint = "Reply for this app (blank = global reply)"
                setText(settings.appReply(pkg))
            }
            val delay = EditText(this).apply {
                hint = "Delay in seconds (blank = global delay)"
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                val d = settings.appDelay(pkg)
                if (d > 0) setText(d.toString())
            }
            replies[pkg] = reply
            delays[pkg] = delay
            list.addView(reply)
            list.addView(delay)
        }

        save.setOnClickListener {
            for ((pkg, field) in replies) settings.setAppReply(pkg, field.text.toString().trim())
            for ((pkg, field) in delays) {
                settings.setAppDelay(pkg, field.text.toString().toIntOrNull() ?: 0)
            }
            Toast.makeText(this, "Rules saved ✓", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
