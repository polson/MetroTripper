package com.philsoft.metrotripper.app;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

/**
 * Created by polson on 1/20/15.
 */
public class LocationHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final Logger log = LoggerManager.getLogger(getClass());

    public interface LocationReadyListener {
        public void onLocationReady(Location location);
    }

    Activity activity;
    GoogleApiClient googleApiClient;
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
        googleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        listener.onLocationReady(lastLocation);
    }

    @Override
    public void onConnectionSuspended(int i) {
        log.d("onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        log.d("onConnectionFailed");
    }


}
