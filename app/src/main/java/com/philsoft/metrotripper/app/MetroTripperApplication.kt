package com.philsoft.metrotripper.app

import android.support.multidex.MultiDexApplication

import timber.log.Timber

class MetroTripperApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}
