package com.philsoft.metrotripper.app.ui.view

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.philsoft.metrotripper.app.state.StopHeadingAction
import com.philsoft.metrotripper.app.state.StopHeadingUiEvent
import com.philsoft.metrotripper.app.state.StopHeadingUiEvent.*
import com.philsoft.metrotripper.databinding.StopHeadingBinding
import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.utils.clicks
import com.philsoft.metrotripper.utils.gone
import com.philsoft.metrotripper.utils.throttleFirst
import com.philsoft.metrotripper.utils.visible
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StopHeadingView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RelativeLayout(context, attrs, defStyleAttr) {

    private val binding = StopHeadingBinding.inflate(LayoutInflater.from(context), this)

    val scheduleButtonClicks: Flow<ScheduleButtonClicked> by lazy {
        binding.scheduleButtonWrapper.clicks()
                .throttleFirst(500)
                .map { StopHeadingUiEvent.ScheduleButtonClicked }
    }

    val locationButtonClicks: Flow<LocationButtonClicked> by lazy {
        binding.locationButton.clicks()
                .map { StopHeadingUiEvent.LocationButtonClicked }
    }

    val saveStopButtonClicks: Flow<SaveStopButtonClicked> by lazy {
        binding.saveButton.clicks()
                .map { StopHeadingUiEvent.SaveStopButtonClicked }
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
        binding.progressSpinner.gone()
        binding.scheduleButton.visible()
    }

    private fun showProgressSpinner() {
        binding.progressSpinner.visible()
        binding.scheduleButton.gone()
    }

    private fun showStop(stop: Stop, isSaved: Boolean) {
        stop.apply {
            binding.route.text = "Stop $stopId"
            binding.description.text = stopName
            if (isSaved) {
                showAsSaved()
            } else {
                showAsUnsaved()
            }
        }
    }

    private fun showAsSaved() = binding.saveButton.setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY)

    private fun showAsUnsaved() {
        binding.saveButton.colorFilter = null
    }
}

