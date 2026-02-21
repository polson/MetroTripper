package com.philsoft.metrotripper.activity

import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

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
