package com.philsoft.metrotripper.app.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.R.id.panel
import com.philsoft.metrotripper.app.state.MapAction
import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.utils.map.RxGoogleMap
import com.philsoft.metrotripper.utils.ui.Ui
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.apache.commons.lang.StringUtils

class MapViewHelper(context: Context, private val map: GoogleMap) {

    companion object {
        private val MIN_ZOOM_LEVEL = 16
    }

    val cameraIdleEvents = RxGoogleMap.cameraIdleEvents(map)
    val markerClicks = RxGoogleMap.markerClicks(map)

    private val stopMarkers = hashMapOf<Long, Marker>()
    private val stopBitmap: Bitmap by lazy {
        Ui.createBitmapFromDrawableResource(context, -30, -30, R.drawable.ic_bus_stop)
    }

    fun render(action: MapAction) {
        when (action) {
            is MapAction.MoveCameraToPosition -> moveCameraToPosition(action.latLng)
            is MapAction.ShowStopMarkers -> {
                if (map.cameraPosition.zoom > MIN_ZOOM_LEVEL) {
                    showStopMarkers(action.stops)
                }
            }
        }
    }

    private fun moveCameraToPosition(latLng: LatLng) {
        map.centerCameraOnLatLng(latLng, true)
    }

    private fun showStopMarkers(stops: List<Stop>) {
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
                val marker = addStopMarkerToMap(it)
                marker.fadeIn(500)
                stopMarkers.put(it.stopId, marker)
            }
        }
    }

    private fun addStopMarkerToMap(stop: Stop): Marker {
        return map.addMarker(MarkerOptions()
                .title(stop.stopId.toString())
                .position(LatLng(stop.stopLat, stop.stopLon))
                .icon(BitmapDescriptorFactory.fromBitmap(stopBitmap)))
    }
}

//Extension fu
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


