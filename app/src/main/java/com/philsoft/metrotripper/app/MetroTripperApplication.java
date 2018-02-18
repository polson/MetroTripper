package com.philsoft.metrotripper.app;

import android.support.multidex.MultiDexApplication;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import timber.log.Timber;

public class MetroTripperApplication extends MultiDexApplication {

    private HurlStack hurlStack;
    private RequestQueue requestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        Timber.d("Planted!");
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            hurlStack = new HurlStack();
            requestQueue = Volley.newRequestQueue(this, hurlStack);
        }
        return requestQueue;
    }
}