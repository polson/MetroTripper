package com.philsoft.metrotripper.app.ui;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.philsoft.metrotripper.R;
import com.philsoft.metrotripper.app.SelectedStopProvider;
import com.philsoft.metrotripper.app.SettingsProvider;
import com.philsoft.metrotripper.app.nextrip.NexTripManager;
import com.philsoft.metrotripper.app.nextrip.TripAdapter;
import com.philsoft.metrotripper.model.Stop;
import com.philsoft.metrotripper.model.Trip;
import com.philsoft.metrotripper.utils.ui.SimplePanelSlideListener;
import com.philsoft.metrotripper.utils.ui.Ui;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.List;

import timber.log.Timber;

/**
 * Created by polson on 1/23/15.
 */
public class StopInfoHelper implements NexTripManager.NexTripListener {

    private static final String KEY_HAS_TRIP_INFO = "KEY_HAS_TRIP_INFO";

    private Activity activity;
    private SlidingUpPanelLayout panel;
    private NexTripManager nexTripManager;
    private MapHelper mapHelper;
    private SettingsProvider settingsProvider;
    private SelectedStopProvider stopProvider;
    private TripAdapter tripAdapter;
    private boolean hasTripInfo;

    public <T extends Activity & SelectedStopProvider> StopInfoHelper(T activity, SlidingUpPanelLayout panel,
                                                                      NexTripManager nexTripManager, MapHelper mapHelper, SettingsProvider settingsProvider, TripAdapter tripAdapter) {
        this.activity = activity;
        this.stopProvider = activity;
        this.panel = panel;
        this.nexTripManager = nexTripManager;
        this.mapHelper = mapHelper;
        this.settingsProvider = settingsProvider;
        this.tripAdapter = tripAdapter;
        setupView();
    }

    private void setupView() {
        View getTimesButton = Ui.findView(activity, R.id.stop_heading_get_times);
        ImageView getLocationButton = Ui.findView(activity, R.id.stop_heading_show_location);
        final ImageView saveStopButton = Ui.findView(activity, R.id.stop_heading_save_stop);
        panel.setPanelSlideListener(new SimplePanelSlideListener() {
            @Override
            public void onPanelExpanded(View view) {
                fetchTripInfo(stopProvider.getSelectedStop());
            }

            @Override
            public void onPanelAnchored(View panel) {
                fetchTripInfo(stopProvider.getSelectedStop());
            }
        });

        // Setup listeners
        getTimesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchTripInfo(stopProvider.getSelectedStop());
                panel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        });
        getLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                panel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                mapHelper.centerCameraOnLatLng(stopProvider.getSelectedStop().getLatLng(), true);
            }
        });
        saveStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (settingsProvider.isStopSaved(stopProvider.getSelectedStop().stopId)) {
                    settingsProvider.unsaveStop(stopProvider.getSelectedStop().stopId);
                    saveStopButton.setColorFilter(null);
                } else {
                    settingsProvider.saveStop(stopProvider.getSelectedStop().stopId);
                    saveStopButton.setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY);
                }
            }
        });
    }

    private void fetchTripInfo(final Stop stop) {
        showProgressSpinner();
        nexTripManager.getTripsRepeating(stop.stopId, MapVehicleHelper.LOCATION_UPDATE_INTERVAL_MS);
    }

    private void showProgressSpinner() {
        Ui.findView(activity, R.id.stop_heading_progress_spinner).setVisibility(View.VISIBLE);
        Ui.findView(activity, R.id.stop_heading_get_times_icon).setVisibility(View.GONE);
    }

    private void hideProgressSpinner() {
        Ui.findView(activity, R.id.stop_heading_progress_spinner).setVisibility(View.GONE);
        Ui.findView(activity, R.id.stop_heading_get_times_icon).setVisibility(View.VISIBLE);
    }

    public void showStopInfo(Stop stop) {
        hideProgressSpinner();
        Ui.setText(activity, R.id.stop_heading_route, "Stop " + stop.stopId);
        Ui.setText(activity, R.id.stop_heading_description, String.valueOf(stop.stopName));
        clearTrips();
        final ImageView saveStopButton = Ui.findView(activity, R.id.stop_heading_save_stop);
        if (stopProvider.getSelectedStop() != null && settingsProvider.isStopSaved(stop.stopId)) {
            saveStopButton.setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY);
        } else {
            saveStopButton.setColorFilter(null);
        }
    }

    private void clearTrips() {
        Timber.d("Clearing trips!");
        RecyclerView tripList = Ui.findView(activity, R.id.stop_info_list);
        ((TripAdapter) tripList.getAdapter()).clear();
    }

    @Override
    public void onNexTripLoadComplete(List<Trip> trips) {
        hideProgressSpinner();
        tripAdapter.setTrips(trips);
        hasTripInfo = true;
    }

    @Override
    public void onNexTripLoadFailed(String message) {
        Timber.d("Failed to get trips: " + message);
    }

    @Override
    public void onNexTripLoadingStopped() {
        // Do nothing
    }

    public void saveState(Bundle outState) {
        if (stopProvider.getSelectedStop() != null) {
            outState.putBoolean(KEY_HAS_TRIP_INFO, hasTripInfo);
        }
    }

    public void restoreState(Bundle savedState) {
        if (stopProvider.getSelectedStop() != null) {
            boolean hasTripInfo = savedState.getBoolean(KEY_HAS_TRIP_INFO);
            if (hasTripInfo) {
                // Only fetch trip info if it was fetched before config change
                fetchTripInfo(stopProvider.getSelectedStop());
            }
        }
    }
}
