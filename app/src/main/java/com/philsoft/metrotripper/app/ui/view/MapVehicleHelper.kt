package com.philsoft.metrotripper.app.ui.view

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.drawToBitmap
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.model.Trip
import com.philsoft.metrotripper.utils.map.fadeIn
import com.philsoft.metrotripper.utils.map.fadeOutAndRemove

class MapVehicleHelper(private val activity: Activity, private val map: GoogleMap) {
    private val vehicleMarkers = HashSet<Marker>()
    private val vehicleIconCache = HashMap<String, BitmapDescriptor>()

    companion object {
        private const val FADE_DURATION = 1000
    }

    fun displayVehicleMarkers(trips: List<Trip>) {
        // Fade out existing vehicles
        for (vehicleMarker in vehicleMarkers) {
            vehicleMarker.fadeOutAndRemove(FADE_DURATION)
        }
        vehicleMarkers.clear()

        // Fade in new vehicles
        for (trip in trips) {
            createVehicleMarker(trip)?.let { marker ->
                vehicleMarkers.add(marker)
                marker.fadeIn(FADE_DURATION)
            }
        }
    }

    private fun createVehicleMarker(trip: Trip): Marker? {
        val label = vehicleLabel(trip)
        return map.addMarker(
                MarkerOptions().anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                        .title(activity.getString(R.string.vehicle) + " " + label)
                        .position(LatLng(trip.vehicleLatitude.toDouble(), trip.vehicleLongitude.toDouble()))
                        .icon(vehicleIconDescriptor(label)))
    }

    private fun vehicleLabel(trip: Trip): String {
        return "${trip.route}${trip.terminal}"
    }

    private fun vehicleIconDescriptor(label: String): BitmapDescriptor {
        return vehicleIconCache.getOrPut(label) {
            BitmapDescriptorFactory.fromBitmap(composeBadgeToBitmap(label))
        }
    }

    private fun composeBadgeToBitmap(label: String): Bitmap {
        val contentRoot =
                requireNotNull(activity.findViewById<ViewGroup>(android.R.id.content)) {
                    "Cannot render marker badge without an activity content root"
                }
        val composeView =
                ComposeView(activity).apply {
                    visibility = View.INVISIBLE
                    layoutParams =
                            ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT)
                    setContent {
                        VehicleMarkerBadge(label = label)
                    }
                }
        contentRoot.addView(composeView)
        return try {
            val unspecified = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            composeView.measure(unspecified, unspecified)
            composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)
            composeView.drawToBitmap(config = Bitmap.Config.ARGB_8888)
        } finally {
            contentRoot.removeView(composeView)
        }
    }
}

@Composable
private fun VehicleMarkerBadge(label: String) {
    val fillColor = colorResource(id = R.color.blue)
    val borderColor = colorResource(id = R.color.white)
    Box(
            modifier =
                    Modifier.size(30.dp)
                            .background(color = fillColor, shape = CircleShape)
                            .border(width = 2.dp, color = borderColor, shape = CircleShape),
            contentAlignment = Alignment.Center) {
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = borderColor)
    }
}
