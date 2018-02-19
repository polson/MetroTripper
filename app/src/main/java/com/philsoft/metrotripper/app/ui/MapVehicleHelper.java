package com.philsoft.metrotripper.app.ui;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.collect.Sets;
import com.philsoft.metrotripper.R;
import com.philsoft.metrotripper.model.Trip;
import com.philsoft.metrotripper.utils.map.MapUtils;
import com.philsoft.metrotripper.utils.ui.Ui;

import java.util.List;
import java.util.Set;

import timber.log.Timber;

public class MapVehicleHelper {

    private static final int FADE_DURATION = 1000;
    public static final int LOCATION_UPDATE_INTERVAL_MS = 31 * 1000; // ms

    private Activity activity;
    private GoogleMap map;
    private Set<VehicleMarker> vehicleMarkers = Sets.newHashSet();

    public MapVehicleHelper(Activity activity, GoogleMap map) {
        this.activity = activity;
        this.map = map;
    }

    public void displayVehicleMarkers(List<Trip> trips) {
        // Fade out existing vehicles
        for (VehicleMarker vehicleMarker : vehicleMarkers) {
            MapUtils.fadeOutMarkerAndRemove(vehicleMarker.marker, FADE_DURATION);
        }
        vehicleMarkers.clear();

        // Fade in new vehicles
        for (Trip trip : trips) {
            VehicleMarker vehicleMarker = new VehicleMarker(trip, createVehicleMarker(trip));
            vehicleMarkers.add(vehicleMarker);
            MapUtils.fadeInMarker(vehicleMarker.marker, FADE_DURATION);
        }
    }

    public Marker createVehicleMarker(Trip trip) {
        View vehicleView = buildVehicleView(trip);
        Marker marker = map.addMarker(new MarkerOptions().anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                .title(activity.getString(R.string.vehicle) + " " + trip.getRoute() + trip.getTerminal()).position(
                        new LatLng(trip.getVehicleLatitude(), trip.getVehicleLongitude())).icon(
                        BitmapDescriptorFactory.fromBitmap(Ui.createBitmapFromView(activity, vehicleView))));
        return marker;
    }

    private View buildVehicleView(Trip trip) {
        View view = activity.getLayoutInflater().inflate(R.layout.vehicle, null);
        Ui.setText(view, R.id.vehicle_number, trip.getRoute() + trip.getTerminal());
        return view;
    }

    //TODO
    public void onNexTripLoadComplete(List<Trip> trips) {
        displayVehicleMarkers(trips);
    }

    //TODO
    public void onNexTripLoadFailed(String message) {
        Timber.w("Unable to load nexTrip: " + message);
        Toast.makeText(activity, R.string.unable_to_load_trips, Toast.LENGTH_SHORT);
    }

    private class VehicleMarker {
        public Trip trip;
        public Marker marker;

        private VehicleMarker(Trip trip, Marker marker) {
            this.trip = trip;
            this.marker = marker;
        }
    }
}
