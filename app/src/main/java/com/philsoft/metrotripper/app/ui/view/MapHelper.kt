package com.philsoft.metrotripper.app.ui.view

import android.annotation.SuppressLint
import android.graphics.Bitmap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.utils.map.FlowGoogleMap
import com.philsoft.metrotripper.utils.map.fadeIn
import com.philsoft.metrotripper.utils.map.fadeOutAndRemove
import com.philsoft.metrotripper.viewmodel.MainViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class MapHelper(private val stopBitmap: Bitmap, private val starredBitmap: Bitmap, private val map: GoogleMap) {

    val cameraIdleEvents: Flow<CameraPosition> = FlowGoogleMap.cameraIdleEvents(map)

    val markerClicks: Flow<Long> = FlowGoogleMap.markerClicks(map)
        .filter { it.title?.all { char -> char.isDigit() } ?: false }
        .map { it.title!!.toLong() }

    private val stopMarkersMap = hashMapOf<Long, Marker>()

    fun moveCameraToPosition(latLng: LatLng) {
        map.centerCameraOnLatLng(latLng, true)
    }

    fun showStopMarkers(stops: List<Stop>) {
        //Fade out existing markers that are no longer to be shown
        val stopIdsToShow = stops.map { it.stopId }
        val markersToDelete = stopMarkersMap.minus(stopIdsToShow)
        markersToDelete.forEach { (stopId, marker) ->
            removeMarker(stopId)
        }

        //Fade in new markers
        stops.forEach {
            val markerIsShown = stopMarkersMap.containsKey(it.stopId)
            if (!markerIsShown) {
                val marker = addStopMarkerToMap(it)
                marker.fadeIn(500)
                stopMarkersMap[it.stopId] = marker
            }
        }
    }

    fun selectStopMarker(stop: Stop) {
        val stopId = stop.stopId
        if (stopMarkersMap.containsKey(stopId)) {
            stopMarkersMap[stopId]?.showInfoWindow()
        } else {
            val newMarker = addStopMarkerToMap(stop)
            newMarker.showInfoWindow()
            stopMarkersMap[stop.stopId] = newMarker
        }
    }

    @SuppressLint("MissingPermission")
    fun enableLocationButton(enabled: Boolean) {
        map.isMyLocationEnabled = enabled
    }

    private fun removeMarker(stopId: Long) {
        val marker = stopMarkersMap.remove(stopId)
        marker?.fadeOutAndRemove(500)
    }

    private fun addStopMarkerToMap(stop: Stop): Marker {
        return map.addMarker(MarkerOptions()
                .title(stop.stopId.toString())
                .position(LatLng(stop.stopLat, stop.stopLon))
                .icon(BitmapDescriptorFactory.fromBitmap(stopBitmap)))!!
    }
}

//Extension functions
private fun GoogleMap.centerCameraOnLatLng(latLng: LatLng, animate: Boolean) {
    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, MainViewModel.MIN_ZOOM_LEVEL)
    if (animate) {
        animateCamera(cameraUpdate)
    } else {
        moveCamera(cameraUpdate)
    }
}


