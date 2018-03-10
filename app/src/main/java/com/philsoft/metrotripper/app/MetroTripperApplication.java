package com.philsoft.metrotripper.app;

import android.support.multidex.MultiDexApplication;

import timber.log.Timber;

public class MetroTripperApplication extends MultiDexApplication {


    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
    }
}