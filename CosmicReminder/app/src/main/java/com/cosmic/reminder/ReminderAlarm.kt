package com.cosmic.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object ReminderAlarm {
    fun schedule(context: Context, reminder: Reminder) {
        if (reminder.done || reminder.dueAt <= System.currentTimeMillis()) return
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("id", reminder.id); putExtra("title", reminder.title); putExtra("note", reminder.note)
        }
        val pending = PendingIntent.getBroadcast(
            context, reminder.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !manager.canScheduleExactAlarms()) {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.dueAt, pending)
            } else {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.dueAt, pending)
            }
        } catch (_: SecurityException) {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.dueAt, pending)
        }
    }

    fun cancel(context: Context, id: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context, id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pending)
    }
}
