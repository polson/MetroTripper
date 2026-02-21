package com.philsoft.metrotripper.app.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.app.state.SlidingPanelAction
import com.philsoft.metrotripper.app.state.SlidingPanelUiEvent.SlidingPanelExpanded
import com.philsoft.metrotripper.app.ui.slidingpanel.SlidingPanel
import com.philsoft.metrotripper.utils.dpToPx
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MtSlidingPanel @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : SlidingPanel(context, attrs, defStyleAttr) {

    val slidingPanelEvents: Flow<SlidingPanelExpanded> = callbackFlow {
        panelStateChangeListener = { panelState: PanelState ->
            if (panelState == SlidingPanel.PanelState.EXPANDED) {
                trySend(SlidingPanelExpanded)
            }
        }
        awaitClose { panelStateChangeListener = {} }
    }

    @SuppressLint("NewApi")
    override fun onFinishInflate() {
        super.onFinishInflate()
        bottomOffset = context.dpToPx(176)
        swipeableViews.clear()
        val stopHeading = findViewById<StopHeadingView>(R.id.stopHeading)
        swipeableViews.add(stopHeading)
    }

    fun render(action: SlidingPanelAction) {
        when (action) {
            SlidingPanelAction.Expand -> handleExpand()
            SlidingPanelAction.Collapse -> handleCollapse()
            SlidingPanelAction.Hide -> handleHide()
        }
    }

    private fun handleHide() {
        setPanelState(PanelState.HIDDEN)
    }

    private fun handleCollapse() {
        setPanelState(PanelState.COLLAPSED)
    }

    private fun handleExpand() {
        setPanelState(PanelState.EXPANDED)
    }
}
