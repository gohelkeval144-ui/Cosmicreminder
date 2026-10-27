package com.cosmic.reminder

import android.content.Context
import org.json.JSONArray

class ReminderStore(context: Context) {
    private val prefs = context.getSharedPreferences("cosmic_reminders", Context.MODE_PRIVATE)

    fun load(): List<Reminder> {
        val raw = prefs.getString("items", "[]") ?: "[]"
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).map { Reminder.fromJson(array.getJSONObject(it)) }
                .sortedWith(compareBy<Reminder> { it.done }.thenBy { it.dueAt })
        } catch (_: Exception) { emptyList() }
    }

    fun save(items: List<Reminder>) {
        val array = JSONArray()
        items.forEach { array.put(it.toJson()) }
        prefs.edit().putString("items", array.toString()).apply()
    }
}
