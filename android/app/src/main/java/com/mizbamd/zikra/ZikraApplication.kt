package com.mizbamd.zikra

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.mizbamd.zikra.di.appModule
import com.mizbamd.zikra.util.DhikrLexicon
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
            val store = org.koin.java.KoinJavaComponent.get<com.mizbamd.zikra.data.local.SettingsStore>(
                com.mizbamd.zikra.data.local.SettingsStore::class.java,
            )
            val lang = runBlocking { store.settings.first().language }
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))
        }
    }
}
