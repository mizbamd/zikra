package com.mizbamd.zikra

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.mizbamd.zikra.data.local.SettingsStore
import com.mizbamd.zikra.data.repo.FrameRepository
import com.mizbamd.zikra.di.appModule
import com.mizbamd.zikra.notify.DailyReminder
import com.mizbamd.zikra.util.DhikrLexicon
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ZikraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        assets.open("dhikr.json").bufferedReader(Charsets.UTF_8).use {
            DhikrLexicon.loadFromJson(it.readText())
        }
        startKoin {
            androidContext(this@ZikraApplication)
            modules(appModule)
        }
        runCatching {
            val store = get<SettingsStore>()
            val s = runBlocking { store.settings.first() }
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(s.language))
            DailyReminder.ensureChannel(this)
            DailyReminder.apply(this, s.reminderEnabled, s.reminderHour, s.reminderMinute)
        }
        runCatching {
            runBlocking { get<FrameRepository>().pruneOldCounts() }
        }
    }
}
