package com.philsoft.metrotripper.app.ui.view

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.clicks
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.utils.inflate
import com.philsoft.metrotripper.utils.invisible
import com.philsoft.metrotripper.utils.visible
import kotlinx.android.synthetic.main.stop_heading.view.*

class StopHeadingView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RelativeLayout(context, attrs, defStyleAttr) {

    val scheduleButtonClicks by lazy { scheduleButtonWrapper.clicks() }
    val locationButtonClicks by lazy { locationButton.clicks() }
    val saveStopButtonClicks by lazy { saveButton.clicks() }
    val headingClicks by lazy { clicks() }

    init {
        this.inflate(R.layout.stop_heading, true)
    }

    fun render(state: AppState) {
        showCurrentStop(state)
        updateProgress(state)
    }

    private fun updateProgress(state: AppState) {
        if (state.isLoadingSchedule) {
            progressSpinner.visible()
            scheduleButton.invisible()
        } else {
            progressSpinner.invisible()
            scheduleButton.visible()
        }
    }

    private fun showCurrentStop(state: AppState) {
        state.selectedStop?.apply {
            route.text = "Stop $stopId"
            description.text = stopName
            if (state.isSelectedStopSaved) {
                saveButton.setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY)
            } else {
                saveButton.colorFilter = null
            }
        }
    }
}