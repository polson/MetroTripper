package com.philsoft.metrotripper.activity;

import android.app.Activity;
import android.os.Bundle;

import com.android.volley.RequestQueue;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;
import com.philsoft.metrotripper.app.MetroTripperApplication;

/**
 * Created by polson on 10/15/14.
 */
public class BaseActivity extends Activity {

	protected final Logger log = LoggerManager.getLogger(getClass());
	protected RequestQueue requestQueue;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestQueue = ((MetroTripperApplication) getApplication()).getRequestQueue();
	}
}
