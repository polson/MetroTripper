package com.philsoft.metrotripper.app.ui.view

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.clicks
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.app.state.StopHeadingAction
import com.philsoft.metrotripper.app.state.StopHeadingUiEvent
import com.philsoft.metrotripper.app.state.StopHeadingUiEvent.*
import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.utils.gone
import com.philsoft.metrotripper.utils.inflate
import com.philsoft.metrotripper.utils.visible
import io.reactivex.Observable
import kotlinx.android.synthetic.main.stop_heading.view.*
import java.util.concurrent.TimeUnit

class StopHeadingView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RelativeLayout(context, attrs, defStyleAttr) {

    val scheduleButtonClicks: Observable<ScheduleButtonClicked> by lazy {
        scheduleButtonWrapper.clicks()
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .map { StopHeadingUiEvent.ScheduleButtonClicked }
                .share()
    }

    val locationButtonClicks: Observable<LocationButtonClicked> by lazy {
        locationButton.clicks()
                .map { StopHeadingUiEvent.LocationButtonClicked }
                .share()
    }

    val saveStopButtonClicks: Observable<SaveStopButtonClicked> by lazy {
        saveButton.clicks()
                .map { StopHeadingUiEvent.SaveStopButtonClicked }
                .share()
    }

    init {
        this.inflate(R.layout.stop_heading, true)
    }

    fun render(action: StopHeadingAction) {
        when (action) {
            is StopHeadingAction.ShowStop -> showStop(action.stop, action.isSaved)
            StopHeadingAction.LoadingTrips -> showProgressSpinner()
            is StopHeadingAction.LoadTripsComplete -> hideProgressSpinner()
            is StopHeadingAction.LoadTripsError -> hideProgressSpinner()
        }
    }

    private fun hideProgressSpinner() {
        progressSpinner.gone()
        scheduleButton.visible()
    }

    private fun showProgressSpinner() {
        progressSpinner.visible()
        scheduleButton.gone()
    }

    private fun showStop(stop: Stop, isSaved: Boolean) {
        stop.apply {
            route.text = "Stop $stopId"
            description.text = stopName
            if (isSaved) {
                showAsSaved()
            } else {
                showAsUnsaved()
            }
        }
    }

    private fun showAsSaved() = saveButton.setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY)

    private fun showAsUnsaved() {
        saveButton.colorFilter = null
    }
}
