package com.example.autoreply

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/** Smart keyword rules: "if message contains X, reply Y after Z seconds". */
class RulesEditorActivity : Activity() {
    private lateinit var settings: Settings
    private lateinit var list: LinearLayout
    private var nextId = 1
    private val rows = mutableListOf<Row>()

    private class Row(
        val target: EditText,
        val keyword: EditText,
        val reply: EditText,
        val delay: EditText,
        val enabled: CheckBox,
        val view: LinearLayout
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = Settings(this)
        title = "Smart rules (keyword → reply)"

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this)
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(list)

        val buttons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val add = Button(this).apply { text = "+ Add rule" }
        val save = Button(this).apply { text = "Save ✓" }
        buttons.addView(add, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        buttons.addView(save, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        root.addView(TextView(this).apply {
            text = "প্রথম ম্যাচ করা rule চলবে। Keyword-এ 're:' দিলে regex ধরা হবে (যেমন re:where|কোথায়)। Target ফাঁকা রাখলে সবার জন্য।"
            setPadding(16, 12, 16, 12)
            textSize = 12f
        })
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        root.addView(buttons)
        setContentView(root)

        add.setOnClickListener { addRow(Settings.Rule(id = nextId++)) }
        save.setOnClickListener { saveRules() }

        val existing = settings.rules()
        if (existing.isEmpty()) {
            addRow(Settings.Rule(id = nextId++))
        } else {
            for (rule in existing) {
                addRow(rule)
                nextId = maxOf(nextId, rule.id + 1)
            }
        }
    }

    private fun addRow(rule: Settings.Rule) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8, 12, 8, 12)
            setBackgroundColor(0x11000000)
        }

        val enabled = CheckBox(this).apply {
            text = "Rule active"
            isChecked = rule.enabled
        }
        val target = EditText(this).apply {
            hint = "Target: number / package / conversation (blank = all)"
            setText(rule.target)
        }
        val keyword = EditText(this).apply {
            hint = "Keyword in message (or re:regex)"
            setText(rule.keyword)
        }
        val reply = EditText(this).apply {
            hint = "Reply for this rule"
            setText(rule.reply)
        }
        val delay = EditText(this).apply {
            hint = "Delay seconds (blank = app/global delay)"
            inputType = InputType.TYPE_CLASS_NUMBER
            if (rule.delay > 0) setText(rule.delay.toString())
        }
        val delete = Button(this).apply { text = "Delete rule" }

        box.addView(enabled)
        box.addView(target)
        box.addView(keyword)
        box.addView(reply)
        box.addView(delay)
        box.addView(delete)
        list.addView(box)

        val row = Row(target, keyword, reply, delay, enabled, box)
        rows.add(row)
        delete.setOnClickListener {
            list.removeView(box)
            rows.remove(row)
        }
    }

    private fun saveRules() {
        val result = mutableListOf<Settings.Rule>()
        for (row in rows) {
            result.add(
                Settings.Rule(
                    id = result.size + 1,
                    target = row.target.text.toString().trim(),
                    keyword = row.keyword.text.toString().trim(),
                    reply = row.reply.text.toString().trim(),
                    delay = row.delay.text.toString().toIntOrNull() ?: 0,
                    enabled = row.enabled.isChecked
                )
            )
        }
        settings.saveRules(result)
        Toast.makeText(this, "${result.size} rules saved ✓", Toast.LENGTH_SHORT).show()
        finish()
    }
}
