package com.philsoft.metrotripper.app.ui;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.Sets;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;
import com.philsoft.metrotripper.utils.EZ;

import java.util.Set;

/**
 * Created by polson on 1/23/15.
 */
public class MapHelper implements GoogleMap.OnCameraChangeListener {

    private final int CAMERA_CHECKER_INTERVAL = 1 * 1000; //ms
    private static final String KEY_CAMERA_POSITION = "KEY_CAMERA_POSITION";
    private static final int CAMERA_CHECKER_MAX_DISTANCE = 10; //meters

    private Logger log = LoggerManager.getLogger();
    private Activity activity;
    private GoogleMap map;
    private CameraPosition lastCameraPosition;
    private boolean isCameraCheckerRunning;
    private long lastCameraCheckTime = 0;
    private Handler handler = new Handler(Looper.getMainLooper());

    Set<CameraListener> listeners = Sets.newHashSet();

    public interface CameraListener {
        public void onCameraStoppedMoving(CameraPosition position);
    }

    public MapHelper(Activity activity, GoogleMap map) {
        this.activity = activity;
        this.map = map;
        map.setOnCameraChangeListener(this);
    }

    public void saveState(Bundle outState) {
        outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
    }

    public void restoreState(Bundle savedState) {
        CameraPosition cameraPosition = savedState.getParcelable(KEY_CAMERA_POSITION);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        map.moveCamera(cameraUpdate);
    }

    public void addListener(CameraListener listener) {
        listeners.add(listener);
    }

    public void removeListener(CameraListener listener) {
        listeners.remove(listener);
    }

    public void notifyCameraStopped() {
        for (CameraListener listener : listeners) {
            listener.onCameraStoppedMoving(map.getCameraPosition());
        }
    }

    public void centerCameraOnLatLng(LatLng latLng, boolean animate) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
        if (animate) {
            map.animateCamera(cameraUpdate);
        } else {
            map.moveCamera(cameraUpdate);
        }
    }

    public void centerCameraOnLocation(Location location, boolean animate) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        centerCameraOnLatLng(latLng, animate);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        lastCameraPosition = cameraPosition;
        long elapsed = System.currentTimeMillis() - lastCameraCheckTime;
        if (!isCameraCheckerRunning && elapsed > 1000) {
            new CameraChangeRunnable(cameraPosition, CAMERA_CHECKER_INTERVAL).run();
        }
    }

    private class CameraChangeRunnable implements Runnable {
        CameraPosition previousPosition;
        int interval;

        private CameraChangeRunnable(CameraPosition previousPosition, int interval) {
            this.previousPosition = previousPosition;
            this.interval = interval;
        }

        @Override
        public void run() {
            isCameraCheckerRunning = true;
            lastCameraCheckTime = System.currentTimeMillis();
            if (cameraStoppedMoving()) {
                handler.removeCallbacks(this);
                isCameraCheckerRunning = false;
                notifyCameraStopped();
            } else {
                //Schedule another check
                handler.postDelayed(this, interval);
            }
        }

        private boolean cameraStoppedMoving() {
            double distance = EZ.getDistanceInMeters(previousPosition.target, lastCameraPosition.target);
            return distance < CAMERA_CHECKER_MAX_DISTANCE;
        }
    }
}
