package com.philsoft.metrotripper.utils.ui;

import android.view.View;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Created by polson on 2/6/15.
 */
public class SimplePanelSlideListener implements SlidingUpPanelLayout.PanelSlideListener {

	@Override
	public void onPanelSlide(View panel, float slideOffset) {
		// Can be overridden
	}

	@Override
	public void onPanelCollapsed(View panel) {
		// Can be overridden
	}

	@Override
	public void onPanelExpanded(View panel) {
		// Can be overridden
	}

	@Override
	public void onPanelAnchored(View panel) {
		// Can be overridden
	}

	@Override
	public void onPanelHidden(View panel) {
		// Can be overridden
	}
}
