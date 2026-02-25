package com.philsoft.metrotripper.di

import androidx.room.Room
import com.philsoft.metrotripper.app.nextrip.NexTripService
import com.philsoft.metrotripper.database.AppDatabase
import com.philsoft.metrotripper.prefs.Prefs
import com.philsoft.metrotripper.repository.NexTripRepository
import com.philsoft.metrotripper.repository.StopsRepository
import com.philsoft.metrotripper.viewmodel.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "metrotripper.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    single { get<AppDatabase>().stopDao() }

    single { Prefs(androidContext()) }

    single { NexTripService.create() }

    single { StopsRepository(androidContext(), get(), get()) }

    single { NexTripRepository(get()) }

    viewModel { MainViewModel(get(), get()) }
}
