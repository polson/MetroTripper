package com.philsoft.metrotripper.utils.ui;

import android.view.View;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class SimplePanelSlideListener implements SlidingUpPanelLayout.PanelSlideListener {

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        // Can be overridden
    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

    }
}
