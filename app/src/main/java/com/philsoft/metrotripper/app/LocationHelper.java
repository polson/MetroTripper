package com.philsoft.metrotripper.app;

import android.app.Activity;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationHelper {

    public interface LocationReadyListener {
        void onLocationReady(Location location);
    }

    Activity activity;
    Location lastLocation;
    LocationReadyListener listener;

    public LocationHelper(Activity activity, LocationReadyListener listener) {
        this.activity = activity;
        this.listener = listener;
    }

    public void getLastLocation() {
        if (lastLocation != null) {
            listener.onLocationReady(lastLocation);
        } else {
            setupGoogleApiClient();
        }
    }

    private void setupGoogleApiClient() {
        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(activity);
        LocationCallback callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                lastLocation = locationResult.getLastLocation();
                listener.onLocationReady(lastLocation);
            }
        };

        client.requestLocationUpdates(LocationRequest.create(), callback, Looper.myLooper());
    }
}
