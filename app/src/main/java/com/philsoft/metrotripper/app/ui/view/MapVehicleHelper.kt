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
import com.philsoft.metrotripper.utils.createBitmap
import com.philsoft.metrotripper.utils.map.fadeIn
import com.philsoft.metrotripper.utils.map.fadeOutAndRemove
import kotlinx.android.synthetic.main.vehicle.view.*

class MapVehicleHelper(private val activity: Activity, private val map: GoogleMap) {
    private val vehicleMarkers = HashSet<Marker>()

    companion object {
        private const val FADE_DURATION = 1000
    }

    fun render(action: VehicleAction) {
        when (action) {
            is VehicleAction.ShowVehicles -> displayVehicleMarkers(action.trips)
        }
    }

    private fun displayVehicleMarkers(trips: List<Trip>) {
        // Fade out existing vehicles
        for (vehicleMarker in vehicleMarkers) {
            vehicleMarker.fadeOutAndRemove(FADE_DURATION)
        }
        vehicleMarkers.clear()

        // Fade in new vehicles
        for (trip in trips) {
            val vehicleMarker = createVehicleMarker(trip)
            vehicleMarkers.add(vehicleMarker)
            vehicleMarker.fadeIn(FADE_DURATION)
        }
    }

    private fun createVehicleMarker(trip: Trip): Marker {
        val vehicleView = buildVehicleView(trip)
        return map.addMarker(
                MarkerOptions().anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                        .title(activity.getString(R.string.vehicle) + " " + trip.route + trip.terminal)
                        .position(LatLng(trip.vehicleLatitude.toDouble(), trip.vehicleLongitude.toDouble()))
                        .icon(BitmapDescriptorFactory.fromBitmap(vehicleView.createBitmap())))
    }

    private fun buildVehicleView(trip: Trip): View {
        val view = activity.layoutInflater.inflate(R.layout.vehicle, null)
        view.vehicleNumber.text = "${trip.route}${trip.terminal}"
        return view
    }
}
