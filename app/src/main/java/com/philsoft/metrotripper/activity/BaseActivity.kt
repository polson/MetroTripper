package com.philsoft.metrotripper.activity

import android.annotation.SuppressLint
import android.support.v4.app.FragmentActivity

@SuppressLint("Registered")
open class BaseActivity : FragmentActivity() {

    var isPaused = false;

    override fun onPause() {
        isPaused = true
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        isPaused = false
    }
}
