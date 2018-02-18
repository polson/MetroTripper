package com.philsoft.metrotripper.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import com.android.volley.RequestQueue;
import com.philsoft.metrotripper.app.MetroTripperApplication;

/**
 * Created by polson on 10/16/14.
 */
public class BaseFragment extends Fragment {


    protected RequestQueue requestQueue;

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
