package com.philsoft.metrotripper.app

import androidx.multidex.MultiDexApplication

import timber.log.Timber

class MetroTripperApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}
