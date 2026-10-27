package com.cosmic.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            ReminderStore(context).load().filter { !it.done }.forEach { ReminderAlarm.schedule(context, it) }
        }
    }
}
