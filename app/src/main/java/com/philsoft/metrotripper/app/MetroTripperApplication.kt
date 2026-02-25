package com.philsoft.metrotripper.app

import androidx.multidex.MultiDexApplication
import com.philsoft.metrotripper.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class MetroTripperApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        
        startKoin {
            androidLogger()
            androidContext(this@MetroTripperApplication)
            modules(appModule)
        }
    }
}
