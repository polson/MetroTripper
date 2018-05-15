package com.philsoft.metrotripper.app.ui.view

import android.animation.ValueAnimator
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

class MapViewHelper(private val stopBitmap: Bitmap, private val starredBitmap: Bitmap, private val map: GoogleMap) {

    companion object {
        private val MIN_ZOOM_LEVEL = 16
    }

    val cameraIdleEvents = RxGoogleMap.cameraIdleEvents(map)
    val markerClicks = RxGoogleMap.markerClicks(map)

    private val stopMarkers = hashMapOf<Long, Marker>()

    fun render(action: MapAction) {
        when (action) {
            is MapAction.MoveCameraToPosition -> moveCameraToPosition(action.latLng)
            is MapAction.ShowStopMarkers -> showStopMarkers(action.stops, action.savedStopIds)
            is MapAction.SelectStopMarker -> selectStopMarker(action.stop, action.isSaved)
        }
    }

    private fun selectStopMarker(stop: Stop, isSaved: Boolean) {
        val stopId = stop.stopId
        if (stopMarkers.containsKey(stopId)) {
            stopMarkers[stopId]?.showInfoWindow()
        } else {
            val newMarker = addStopMarkerToMap(stop, isSaved)
            newMarker.showInfoWindow()
            stopMarkers.put(stop.stopId, newMarker)
        }
    }


    private fun moveCameraToPosition(latLng: LatLng) {
        map.centerCameraOnLatLng(latLng, true)
    }

    private fun showStopMarkers(stops: List<Stop>, savedStopIds: Set<Long>) {
        if (map.cameraPosition.zoom < MIN_ZOOM_LEVEL) {
            return
        }
        //Remove markers that are not in the new list
        val updatedStopIds = stops.map { it.stopId }
        val markersToDelete = stopMarkers.minus(updatedStopIds)
        markersToDelete.forEach { (stopId, marker) ->
            marker.remove()
            stopMarkers.remove(stopId)
        }

        //Fade in new markers
        stops.forEach {
            val markerIsShown = stopMarkers.containsKey(it.stopId)
            if (!markerIsShown) {
                val marker = addStopMarkerToMap(it, savedStopIds.contains(it.stopId))
                marker.fadeIn(500)
                stopMarkers.put(it.stopId, marker)
            }
        }
    }

    private fun addStopMarkerToMap(stop: Stop, isSaved: Boolean): Marker {
        val bitmap = if (isSaved) starredBitmap else stopBitmap
        return map.addMarker(MarkerOptions()
                .title(stop.stopId.toString())
                .position(LatLng(stop.stopLat, stop.stopLon))
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)))
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

private fun Marker.fadeIn(duration: Int): ValueAnimator {
    val ani = ValueAnimator.ofFloat(0f, 1f)
    ani.duration = duration.toLong()
    ani.addUpdateListener { animation -> alpha = animation.animatedValue as Float }
    ani.start()
    return ani
}


