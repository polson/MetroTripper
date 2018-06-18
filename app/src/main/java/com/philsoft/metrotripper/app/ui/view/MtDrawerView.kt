package com.philsoft.metrotripper.app.ui.view

import android.content.Context
import android.support.v4.widget.DrawerLayout
import android.util.AttributeSet
import com.philsoft.metrotripper.app.state.DrawerAction

class MtDrawerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : DrawerLayout(context, attrs, defStyleAttr) {

    fun render(action: DrawerAction) = when (action) {
        DrawerAction.CloseDrawer -> handleCloseDrawer()
    }

    private fun handleCloseDrawer() {
        closeDrawers()
    }
}
