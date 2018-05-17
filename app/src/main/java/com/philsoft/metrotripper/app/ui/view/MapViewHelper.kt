package com.philsoft.metrotripper.app.ui.view

import android.graphics.Bitmap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.philsoft.metrotripper.app.state.MapAction
import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.utils.map.RxGoogleMap
import com.philsoft.metrotripper.utils.map.fadeIn
import com.philsoft.metrotripper.utils.map.fadeOutAndRemove

class MapViewHelper(private val stopBitmap: Bitmap, private val starredBitmap: Bitmap, private val map: GoogleMap) {

    companion object {
        private val MIN_ZOOM_LEVEL = 16
    }

    val cameraIdleEvents = RxGoogleMap.cameraIdleEvents(map)
    val markerClicks = RxGoogleMap.markerClicks(map)

    private val stopMarkers = hashMapOf<Long, Marker>()

    fun render(action: MapAction) = when (action) {
        is MapAction.MoveCameraToPosition -> moveCameraToPosition(action.latLng)
        is MapAction.ShowStopMarkers -> showStopMarkers(action.stops)
        is MapAction.SelectStopMarker -> selectStopMarker(action.stop)
        is MapAction.HideStopMarkers -> hideStopMarkers(action.selectedStop)
    }

    private fun hideStopMarkers(selectedStop: Stop) {
        val markersToDelete = stopMarkers.filterKeys { it != selectedStop.stopId }
        markersToDelete.forEach { removeMarker(it.key) }
    }

    private fun selectStopMarker(stop: Stop) {
        val stopId = stop.stopId
        if (stopMarkers.containsKey(stopId)) {
            stopMarkers[stopId]?.showInfoWindow()
        } else {
            val newMarker = addStopMarkerToMap(stop)
            newMarker.showInfoWindow()
            stopMarkers.put(stop.stopId, newMarker)
        }
    }


    private fun moveCameraToPosition(latLng: LatLng) {
        map.centerCameraOnLatLng(latLng, true)
    }

    private fun showStopMarkers(stops: List<Stop>) {
        if (map.cameraPosition.zoom < MIN_ZOOM_LEVEL) {
            return
        }
        //Remove markers that are not in the new list
        val updatedStopIds = stops.map { it.stopId }
        val markersToDelete = stopMarkers.minus(updatedStopIds)
        markersToDelete.forEach { (stopId, marker) ->
            removeMarker(stopId)
        }

        //Fade in new markers
        stops.forEach {
            val markerIsShown = stopMarkers.containsKey(it.stopId)
            if (!markerIsShown) {
                val marker = addStopMarkerToMap(it)
                marker.fadeIn(500)
                stopMarkers.put(it.stopId, marker)
            }
        }
    }

    private fun removeMarker(stopId: Long) {
        val marker = stopMarkers.remove(stopId)
        marker?.fadeOutAndRemove(500)
    }

    private fun addStopMarkerToMap(stop: Stop): Marker {
        return map.addMarker(MarkerOptions()
                .title(stop.stopId.toString())
                .position(LatLng(stop.stopLat, stop.stopLon))
                .icon(BitmapDescriptorFactory.fromBitmap(stopBitmap)))
    }
}

//Extension functions
private fun GoogleMap.centerCameraOnLatLng(latLng: LatLng, animate: Boolean) {
    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
    if (animate) {
        animateCamera(cameraUpdate)
    } else {
        moveCamera(cameraUpdate)
    }
}


