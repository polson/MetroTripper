package com.philsoft.metrotripper.app.ui.view

import android.app.Activity
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.app.state.VehicleAction
import com.philsoft.metrotripper.model.Trip
import com.philsoft.metrotripper.utils.map.fadeIn
import com.philsoft.metrotripper.utils.map.fadeOutAndRemove
import com.philsoft.metrotripper.utils.ui.Ui
import kotlinx.android.synthetic.main.vehicle.view.*

class MapVehicleHelper(private val activity: Activity, private val map: GoogleMap) {
    private val vehicleMarkers = HashSet<VehicleMarker>()

    fun render(action: VehicleAction) {
        when (action) {
            is VehicleAction.ShowVehicles -> displayVehicleMarkers(action.trips)
        }
    }

    private fun displayVehicleMarkers(trips: List<Trip>) {
        // Fade out existing vehicles
        for (vehicleMarker in vehicleMarkers) {
            vehicleMarker.marker.fadeOutAndRemove(FADE_DURATION)
        }
        vehicleMarkers.clear()

        // Fade in new vehicles
        for (trip in trips) {
            val vehicleMarker = VehicleMarker(trip, createVehicleMarker(trip))
            vehicleMarkers.add(vehicleMarker)
            vehicleMarker.marker.fadeIn(FADE_DURATION)
        }
    }

    private fun createVehicleMarker(trip: Trip): Marker {
        val vehicleView = buildVehicleView(trip)
        return map.addMarker(
                MarkerOptions().anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                        .title(activity.getString(R.string.vehicle) + " " + trip.route + trip.terminal)
                        .position(LatLng(trip.vehicleLatitude.toDouble(), trip.vehicleLongitude.toDouble()))
                        .icon(BitmapDescriptorFactory.fromBitmap(Ui.createBitmapFromView(activity, vehicleView))))
    }

    private fun buildVehicleView(trip: Trip): View {
        val view = activity.layoutInflater.inflate(R.layout.vehicle, null)
        view.vehicleNumber.text = "${trip.route}${trip.terminal}"
        return view
    }

    private inner class VehicleMarker constructor(val trip: Trip, val marker: Marker)

    companion object {
        private val FADE_DURATION = 1000
        val LOCATION_UPDATE_INTERVAL_MS = 31 * 1000 // ms
    }
}
