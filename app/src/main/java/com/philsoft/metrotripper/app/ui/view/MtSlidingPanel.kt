package com.philsoft.metrotripper.app.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import com.philsoft.metrotripper.app.state.SlidingPanelAction
import com.philsoft.metrotripper.app.ui.slidingpanel.SlidingPanel
import com.philsoft.metrotripper.utils.dpToPx
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_main.view.*

class MtSlidingPanel @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : SlidingPanel(context, attrs, defStyleAttr) {

    val slidingPanelEvents: Observable<PanelState> = Observable.create<PanelState> { emitter ->
        panelStateChangeListener = { panelState ->
            emitter.onNext(panelState)
        }
        emitter.setCancellable { panelStateChangeListener = {} }
    }.share()


    @SuppressLint("NewApi")
    override fun onFinishInflate() {
        super.onFinishInflate()
        bottomOffset = context.dpToPx(176)
        swipableViews.clear()
        swipableViews.add(stopHeading)
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
