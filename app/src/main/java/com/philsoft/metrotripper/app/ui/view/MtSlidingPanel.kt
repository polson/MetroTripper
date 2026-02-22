package com.philsoft.metrotripper.app.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.app.ui.slidingpanel.SlidingPanel
import com.philsoft.metrotripper.utils.dpToPx
import com.philsoft.metrotripper.utils.ui.Ui
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MtSlidingPanel @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : SlidingPanel(context, attrs, defStyleAttr) {

    companion object {
        /** Extra space (dp) below StopHeadingView so the first TripList row is visible */
        private const val TRIP_LIST_PEEK_DP = 80
    }

    private var lastInset = 0

    val slidingPanelEvents: Flow<Unit> = callbackFlow {
        panelStateChangeListener = { panelState: PanelState ->
            if (panelState == SlidingPanel.PanelState.EXPANDED) {
                trySend(Unit)
            }
        }
        awaitClose { panelStateChangeListener = {} }
    }

    @SuppressLint("NewApi")
    override fun onFinishInflate() {
        super.onFinishInflate()
        swipeableViews.clear()
        val stopHeading = findViewById<StopHeadingView>(R.id.stopHeading)
        swipeableViews.add(stopHeading)

        stopHeading.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            applyBottomInset(lastInset)
        }
    }

    fun expand() {
        setPanelState(PanelState.EXPANDED)
    }

    fun collapse() {
        setPanelState(PanelState.COLLAPSED)
    }

    fun hide() {
        setPanelState(PanelState.HIDDEN)
    }

    fun applyBottomInset(inset: Int) {
        lastInset = inset
        val stopHeading = findViewById<StopHeadingView>(R.id.stopHeading)
        val peekHint = context.dpToPx(TRIP_LIST_PEEK_DP)
        val baseOffset = if (stopHeading != null && stopHeading.height > 0) stopHeading.height + peekHint else 0
        bottomOffset = baseOffset + inset
        if (panelState == PanelState.COLLAPSED) {
            panelView.y = (Ui.getCurrentScreenHeight(context).toFloat() - bottomOffset)
        }
    }
}
