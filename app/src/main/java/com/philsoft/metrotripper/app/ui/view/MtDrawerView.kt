package com.philsoft.metrotripper.app.ui.view

import android.content.Context
import androidx.drawerlayout.widget.DrawerLayout
import android.util.AttributeSet

class MtDrawerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : DrawerLayout(context, attrs, defStyleAttr) {

    fun closeDrawer() {
        closeDrawers()
    }
}
