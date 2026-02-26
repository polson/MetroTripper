package com.philsoft.metrotripper.app.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.model.Trip
import com.philsoft.metrotripper.viewmodel.MainUiState

@OptIn(MapsComposeExperimentalApi::class)
@Composable
internal fun MainMapContent(
    uiState: MainUiState,
    runtimeState: MainMapRuntimeState,
    mapProperties: MapProperties,
    mapUiSettings: MapUiSettings,
    stopMarkers: List<Stop>,
    stopMarkerIds: List<Long>,
    onMarkerClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    GoogleMap(
        modifier = modifier,
        cameraPositionState = runtimeState.cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings,
        onMapLoaded = { runtimeState.mapLoaded = true }
    ) {
        stopMarkers.forEach { stop ->
            val markerState = runtimeState.stopMarkerStates.getOrPut(stop.stopId) {
                MarkerState(position = stop.latLng)
            }
            if (markerState.position != stop.latLng) {
                markerState.position = stop.latLng
            }

            Marker(
                state = markerState,
                title = stop.stopId.toString(),
                icon = runtimeState.stopIcon,
                onClick = {
                    onMarkerClick(stop.stopId)
                    false
                }
            )
        }

        val stopIdsSet = stopMarkerIds.toSet()
        runtimeState.stopMarkerStates.keys
            .filterNot(stopIdsSet::contains)
            .forEach(runtimeState.stopMarkerStates::remove)

        if (uiState.selectedStop != null) {
            uiState.trips.forEach { trip ->
                VehicleMarker(trip = trip)
            }
        }
    }
}

@Composable
private fun VehicleMarker(trip: Trip) {
    val label = remember(trip.route, trip.terminal) {
        "${trip.route}${trip.terminal}"
    }
    val position = remember(trip.vehicleLatitude, trip.vehicleLongitude) {
        LatLng(trip.vehicleLatitude.toDouble(), trip.vehicleLongitude.toDouble())
    }
    val markerState = remember {
        MarkerState(position = position)
    }
    if (markerState.position != position) {
        markerState.position = position
    }

    MarkerComposable(
        state = markerState,
        title = stringResource(R.string.vehicle) + " " + label,
        anchor = Offset(0f, 1f)
    ) {
        VehicleMarkerBadge(label = label)
    }
}

@Composable
private fun VehicleMarkerBadge(label: String) {
    val fillColor = colorResource(id = R.color.blue)
    val borderColor = colorResource(id = R.color.white)
    Box(
        modifier = Modifier
            .size(30.dp)
            .background(color = fillColor, shape = CircleShape)
            .border(width = 2.dp, color = borderColor, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = borderColor
        )
    }
}
