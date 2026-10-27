package com.cosmic.reminder

import org.json.JSONObject

data class Reminder(
    val id: Long,
    val title: String,
    val note: String,
    val category: String,
    val priority: String,
    val dueAt: Long,
    val done: Boolean = false
) {
    fun toJson() = JSONObject().apply {
        put("id", id); put("title", title); put("note", note)
        put("category", category); put("priority", priority)
        put("dueAt", dueAt); put("done", done)
    }

    companion object {
        fun fromJson(o: JSONObject) = Reminder(
            id = o.optLong("id"),
            title = o.optString("title"),
            note = o.optString("note"),
            category = o.optString("category", "Personal"),
            priority = o.optString("priority", "Normal"),
            dueAt = o.optLong("dueAt"),
            done = o.optBoolean("done")
        )
    }
}
