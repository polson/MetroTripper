package com.philsoft.metrotripper.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import com.android.volley.RequestQueue;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;
import com.philsoft.metrotripper.app.MetroTripperApplication;

/**
 * Created by polson on 10/16/14.
 */
public class BaseFragment extends Fragment {


	protected RequestQueue requestQueue;
	protected Logger log = LoggerManager.getLogger(getClass());

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		requestQueue = ((MetroTripperApplication) getActivity().getApplication()).getRequestQueue();
	}
}
