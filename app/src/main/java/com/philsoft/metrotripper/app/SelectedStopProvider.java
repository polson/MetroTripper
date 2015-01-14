package com.philsoft.metrotripper.app;

import com.philsoft.metrotripper.model.Stop;

/**
 * Created by polson on 2/8/15.
 */
public interface SelectedStopProvider {
	public static final String KEY_STOP_ID = "KEY_STOP_ID";

	public Stop getSelectedStop();

	public void setSelectedStop(Stop stop);

	public void showStop(Stop stop);
}
