package com.example.autoreply

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast

/** Shows EVERY installed app with a checkbox — no need to type package names. */
class AppPickerActivity : Activity() {
    private lateinit var settings: Settings
    private lateinit var listContainer: LinearLayout
    private lateinit var search: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = Settings(this)
        title = "Select apps to auto-reply"

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        search = EditText(this).apply { hint = "Search app name or package…"; isSingleLine = true }
        listContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(listContainer) }

        val buttons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val all = Button(this).apply { text = "Select all" }
        val none = Button(this).apply { text = "Clear" }
        val done = Button(this).apply { text = "Done ✓" }
        buttons.addView(all, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        buttons.addView(none, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        buttons.addView(done, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        root.addView(search)
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        root.addView(buttons)
        setContentView(root)

        all.setOnClickListener {
            settings.packages = visiblePackages().toSet()
            render(search.text.toString())
            Toast.makeText(this, "Selected ${settings.packages.size} apps", Toast.LENGTH_SHORT).show()
        }
        none.setOnClickListener {
            settings.packages = emptySet()
            render(search.text.toString())
        }
        done.setOnClickListener { finish() }
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) { render(s.toString()) }
        })
        render("")
    }

    private data class AppItem(val pkg: String, val label: String)

    private fun visibleApps(query: String): List<AppItem> {
        val pm = packageManager
        return pm.getInstalledPackages(0)
            .filter { it.packageName != packageName && pm.getLaunchIntentForPackage(it.packageName) != null }
            .map { info ->
                val label = info.applicationInfo?.loadLabel(pm)?.toString() ?: info.packageName
                AppItem(info.packageName, label)
            }
            .filter { query.isBlank() || it.pkg.contains(query, true) || it.label.contains(query, true) }
            .sortedBy { it.label.lowercase() }
    }

    private fun visiblePackages(): List<String> = visibleApps(search.text.toString()).map { it.pkg }

    private fun render(query: String) {
        listContainer.removeAllViews()
        for (app in visibleApps(query)) {
            val box = CheckBox(this).apply {
                text = "${app.label}\n${app.pkg}"
                isChecked = app.pkg in settings.packages
                setOnCheckedChangeListener { _, checked ->
                    val current = settings.packages.toMutableSet()
                    if (checked) current.add(app.pkg) else current.remove(app.pkg)
                    settings.packages = current
                }
            }
            listContainer.addView(box)
        }
    }
}
