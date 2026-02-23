package com.philsoft.metrotripper.app.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.philsoft.metrotripper.model.Stop
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class StopHeadingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val _scheduleButtonClicks = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val _locationButtonClicks = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val _saveStopButtonClicks = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val scheduleButtonClicks: Flow<Unit> = _scheduleButtonClicks
    val locationButtonClicks: Flow<Unit> = _locationButtonClicks
    val saveStopButtonClicks: Flow<Unit> = _saveStopButtonClicks

    private var currentStop: Stop? = null
    private var isStopSaved: Boolean = false
    private var isSpinning: Boolean = false

    private val composeView = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    }

    init {
        addView(composeView)
        updateComposeView()
    }

    fun showStop(stop: Stop, isSaved: Boolean) {
        currentStop = stop
        isStopSaved = isSaved
        updateComposeView()
    }

    fun showProgressSpinner() {
        isSpinning = true
        updateComposeView()
    }

    fun hideProgressSpinner() {
        isSpinning = false
        updateComposeView()
    }

    private fun updateComposeView() {
        composeView.setContent {
            StopHeading(
                stop = currentStop,
                isSaved = isStopSaved,
                isLoading = isSpinning,
                onScheduleClick = { _scheduleButtonClicks.tryEmit(Unit) },
                onLocationClick = { _locationButtonClicks.tryEmit(Unit) },
                onSaveClick = { _saveStopButtonClicks.tryEmit(Unit) }
            )
        }
    }
}
