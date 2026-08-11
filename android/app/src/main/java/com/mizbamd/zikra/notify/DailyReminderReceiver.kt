package com.mizbamd.zikra.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mizbamd.zikra.data.local.SettingsStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent

class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pending = goAsync()
            Thread {
                try {
                    rescheduleFromStore(context)
                } finally {
                    pending.finish()
                }
            }.start()
            return
        }
        DailyReminder.show(context)
        val pending = goAsync()
        Thread {
            try {
                rescheduleFromStore(context)
            } finally {
                pending.finish()
            }
        }.start()
    }

    private fun rescheduleFromStore(context: Context) {
        runCatching {
            val store = KoinJavaComponent.get<SettingsStore>(SettingsStore::class.java)
            val s = runBlocking { store.settings.first() }
            DailyReminder.apply(context, s.reminderEnabled, s.reminderHour, s.reminderMinute)
        }
    }
}
