package com.mizbamd.zikra.di

import androidx.room.Room
import com.mizbamd.zikra.data.local.SettingsStore
import com.mizbamd.zikra.data.local.ZikraDatabase
import com.mizbamd.zikra.data.remote.ZikraApi
import com.mizbamd.zikra.data.repo.AuthRepository
import com.mizbamd.zikra.data.repo.FrameRepository
import com.mizbamd.zikra.ui.ZikraViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(androidContext(), ZikraDatabase::class.java, "zikra.db")
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<ZikraDatabase>().frames() }
    single { get<ZikraDatabase>().dailyCounts() }
    single { SettingsStore(androidContext()) }
    single { ZikraApi() }
    single { FrameRepository(get(), get(), get(), get()) }
    single { AuthRepository(get(), get(), get()) }
    viewModel { ZikraViewModel(androidApplication(), get(), get(), get()) }
}
