package com.philsoft.metrotripper.app.ui.view

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
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

    val scheduleButtonClicks: Flow<Unit> by lazy {
        binding.scheduleButtonWrapper.clicks()
                .throttleFirst(500)
                .map { Unit }
    }

    val locationButtonClicks: Flow<Unit> by lazy {
        binding.locationButton.clicks()
                .map { Unit }
    }

    val saveStopButtonClicks: Flow<Unit> by lazy {
        binding.saveButton.clicks()
                .map { Unit }
    }

    fun showStop(stop: Stop, isSaved: Boolean) {
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

    fun showProgressSpinner() {
        binding.progressSpinner.visible()
        binding.scheduleButton.gone()
    }

    fun hideProgressSpinner() {
        binding.progressSpinner.gone()
        binding.scheduleButton.visible()
    }

    private fun showAsSaved() = binding.saveButton.setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY)

    private fun showAsUnsaved() {
        binding.saveButton.colorFilter = null
    }
}

