package com.philsoft.metrotripper.app.ui;

import android.app.Activity;
import android.graphics.Bitmap;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.collect.Maps;
import com.philsoft.metrotripper.R;
import com.philsoft.metrotripper.app.SelectedStopProvider;
import com.philsoft.metrotripper.app.SettingsProvider;
import com.philsoft.metrotripper.database.DataProvider;
import com.philsoft.metrotripper.model.Stop;
import com.philsoft.metrotripper.utils.map.MapUtils;
import com.philsoft.metrotripper.utils.ui.Ui;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by polson on 2/4/15.
 */
public class StopHelper implements MapHelper.CameraListener, SettingsProvider.SettingsListener {

    private static final int MAX_STOPS = 20;
    private static final int MIN_ZOOM_LEVEL = 16;

    Activity activity;
    GoogleMap map;
    DataProvider dataProvider;
    Map<Long, Marker> stopMarkers = Maps.newHashMap();
    Bitmap stopBitmap;
    Bitmap starBitmap;
    SettingsProvider settingsProvider;
    SelectedStopProvider stopProvider;

    public <T extends Activity & SelectedStopProvider> StopHelper(T activity, GoogleMap map, DataProvider dataProvider,
                                                                  SettingsProvider settingsProvider) {
        this.activity = activity;
        this.stopProvider = activity;
        this.map = map;
        this.dataProvider = dataProvider;
        this.settingsProvider = settingsProvider;
        this.settingsProvider.addListener(this);
    }

    @Override
    public void onCameraStoppedMoving(CameraPosition currentPosition) {
        updateStops(currentPosition);
    }

    public void updateStops(CameraPosition position) {
        if (position.zoom < MIN_ZOOM_LEVEL) {
            removeAllStopsButSelected();
            return;
        }
        List<Stop> stops = dataProvider.getClosestStops(position.target.latitude, position.target.longitude, MAX_STOPS);
        for (Stop stop : stops) {
            if (!stopMarkers.containsKey(stop.stopId)) {
                MapUtils.fadeInMarker(createStopMarker(stop), 500);
            }
        }
    }

    public void selectStopMarker(Stop stop) {
        Marker marker = stopMarkers.get(stop.stopId);
        if (marker == null) {
            marker = createStopMarker(stop);
            MapUtils.fadeInMarker(marker, 500);
        }
        marker.showInfoWindow();
    }

    private void removeAllStopsButSelected() {
        Stop selectedStop = stopProvider.getSelectedStop();
        Long selectedStopId = selectedStop != null ? selectedStop.stopId : -1;
        for (Iterator<Long> markerStopIdIter = stopMarkers.keySet().iterator(); markerStopIdIter.hasNext(); ) {
            long stopId = markerStopIdIter.next();
            boolean isMarkerSelected = (stopId == selectedStopId);
            if (!isMarkerSelected) {
                MapUtils.fadeOutMarkerAndRemove(stopMarkers.get(stopId), 500);
                markerStopIdIter.remove();
            }
        }
    }

    private Marker createStopMarker(Stop stop) {
        Bitmap icon = settingsProvider.isStopSaved(stop.stopId) ? getStarredBitmap() : getStopBitmap();
        Marker marker = map.addMarker(new MarkerOptions().title(String.valueOf(stop.stopId)).position(
                new LatLng(stop.stopLat, stop.stopLon)).icon(BitmapDescriptorFactory.fromBitmap(icon)));
        stopMarkers.put(stop.stopId, marker);
        return marker;
    }

    private Bitmap getStopBitmap() {
        if (stopBitmap == null) {
            stopBitmap = Ui.createBitmapFromDrawableResource(activity, -30, -30, R.drawable.ic_bus_stop);
        }
        return stopBitmap;
    }

    private Bitmap getStarredBitmap() {
        if (starBitmap == null) {
            starBitmap = Ui.createBitmapFromLayoutResource(activity, R.layout.starred_stop);
        }
        return starBitmap;
    }

    @Override
    public void onStopSaved(long stopId) {
        refreshMarker(stopId);
    }

    @Override
    public void onStopUnsaved(long stopId) {
        refreshMarker(stopId);
    }

    private void refreshMarker(long stopId) {
        Marker marker = stopMarkers.get(stopId);
        if (marker != null) {
            marker.remove();
            createStopMarker(dataProvider.getStopById(stopId)).showInfoWindow();

        }
    }
}
