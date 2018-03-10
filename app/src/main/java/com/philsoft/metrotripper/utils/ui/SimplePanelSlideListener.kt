package com.philsoft.metrotripper.utils.ui

import android.view.View

import com.sothree.slidinguppanel.SlidingUpPanelLayout

open class SimplePanelSlideListener : SlidingUpPanelLayout.PanelSlideListener {

    override fun onPanelSlide(panel: View, slideOffset: Float) {
        // Can be overridden
    }

    override fun onPanelStateChanged(panel: View, previousState: SlidingUpPanelLayout.PanelState, newState: SlidingUpPanelLayout.PanelState) {

    }
}
