package com.philsoft.metrotripper.app.ui

import android.app.Activity
import android.graphics.Bitmap
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.common.collect.Maps
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.app.SelectedStopProvider
import com.philsoft.metrotripper.app.SettingsProvider
import com.philsoft.metrotripper.database.DataProvider
import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.utils.map.MapUtils
import com.philsoft.metrotripper.utils.map.RxGoogleMap
import com.philsoft.metrotripper.utils.ui.Ui


class StopHelper(activity: Activity,
                 private val stopProvider: SelectedStopProvider,
                 private val map: GoogleMap,
                 private val dataProvider: DataProvider,
                 private val settingsProvider: SettingsProvider) : SettingsProvider.SettingsListener {

    private var stopMarkers: MutableMap<Long, Marker> = Maps.newHashMap()
    private val stopBitmap: Bitmap by lazy { Ui.createBitmapFromDrawableResource(activity, -30, -30, R.drawable.ic_bus_stop) }
    private val starredBitmap: Bitmap by lazy { Ui.createBitmapFromLayoutResource(activity, R.layout.starred_stop) }

    init {
        this.settingsProvider.addListener(this)
        RxGoogleMap.cameraIdleEvents(map)
                .subscribe(this::updateStops)
    }

    private fun updateStops(position: CameraPosition) {
        if (position.zoom < MIN_ZOOM_LEVEL) {
            removeAllStopsButSelected()
            return
        }
        val stops = dataProvider.getClosestStops(position.target.latitude, position.target.longitude, MAX_STOPS)
        stops
                .filterNot { stopMarkers.containsKey(it.stopId) }
                .forEach { MapUtils.fadeInMarker(createStopMarker(it), 500) }
    }

    fun selectStopMarker(stop: Stop) {
        var marker: Marker? = stopMarkers[stop.stopId]
        if (marker == null) {
            marker = createStopMarker(stop)
            MapUtils.fadeInMarker(marker, 500)
        }
        marker.showInfoWindow()
    }

    private fun removeAllStopsButSelected() {
        val selectedStop = stopProvider.selectedStop
        val selectedStopId = selectedStop?.stopId ?: -1
        val markerStopIdIter = stopMarkers.keys.iterator()
        while (markerStopIdIter.hasNext()) {
            val stopId = markerStopIdIter.next()
            val isMarkerSelected = stopId == selectedStopId
            if (!isMarkerSelected) {
                MapUtils.fadeOutMarkerAndRemove(stopMarkers[stopId]!!, 500)
                markerStopIdIter.remove()
            }
        }
    }

    private fun createStopMarker(stop: Stop?): Marker {
        val icon = if (settingsProvider.isStopSaved(stop!!.stopId)) starredBitmap else stopBitmap
        val marker = map.addMarker(MarkerOptions().title(stop.stopId.toString()).position(
                LatLng(stop.stopLat!!, stop.stopLon!!)).icon(BitmapDescriptorFactory.fromBitmap(icon)))
        stopMarkers.put(stop.stopId, marker)
        return marker
    }

    override fun onStopSaved(stopId: Long) {
        refreshMarker(stopId)
    }

    override fun onStopUnsaved(stopId: Long) {
        refreshMarker(stopId)
    }

    private fun refreshMarker(stopId: Long) {
        val marker = stopMarkers[stopId]
        if (marker != null) {
            marker.remove()
            createStopMarker(dataProvider.getStopById(stopId)).showInfoWindow()

        }
    }

    companion object {
        private val MAX_STOPS = 20
        private val MIN_ZOOM_LEVEL = 16
    }
}
