package com.philsoft.metrotripper.app;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

/**
 * Created by polson on 10/14/14.
 */
public class MetroTripperApplication extends Application {

	private HurlStack hurlStack;
	private RequestQueue requestQueue;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public RequestQueue getRequestQueue() {
		if (requestQueue == null) {
			hurlStack = new HurlStack();
			requestQueue = Volley.newRequestQueue(this, hurlStack);
		}
		return requestQueue;
	}
}