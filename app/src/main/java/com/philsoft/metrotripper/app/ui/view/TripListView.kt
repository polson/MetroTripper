package com.philsoft.metrotripper.app.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.philsoft.metrotripper.model.Trip

class TripListView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var currentTrips: List<Trip> = emptyList()

    private val composeView = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    }

    init {
        addView(composeView)
        updateComposeView()
    }

    fun showTrips(trips: List<Trip>) {
        currentTrips = trips
        updateComposeView()
    }

    private fun updateComposeView() {
        composeView.setContent {
            TripList(trips = currentTrips)
        }
    }
}
