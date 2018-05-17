package com.philsoft.metrotripper.app.state.transformer

import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.AppUiEvent
import com.philsoft.metrotripper.app.state.MapAction
import com.philsoft.metrotripper.model.Stop

class MapTransformer : AppActionTransformer<MapAction>() {

    companion object {
        private val MINNEAPOLIS_LATLNG = LatLng(44.9799700, -93.2638400)
    }

    override fun handleEvent(event: AppUiEvent, state: AppState) {
        when (event) {
            is AppUiEvent.InitialLocationUpdate -> handleInitialLocationUpdate(event.locationResult)
            is AppUiEvent.LocationButtonClicked -> handleLocationButtonClicked(state)
            is AppUiEvent.StopSearched -> handleStopSearched(state)
            is AppUiEvent.StopSelectedFromDrawer -> handleStopSelected(event.stop)
            is AppUiEvent.CameraIdle -> handleCameraIdle(state, event.cameraPosition)
        }
    }

    private fun handleStopSelected(stop: Stop) {
        send(MapAction.MoveCameraToPosition(stop.latLng))
        send(MapAction.SelectStopMarker(stop))
    }

    private fun handleCameraIdle(state: AppState, cameraPosition: CameraPosition) = state.apply {
        if (cameraPosition.zoom < 15) {
            if (selectedStop != null) {
                send(MapAction.HideStopMarkers(selectedStop))
            }
        } else {
            send(MapAction.ShowStopMarkers(visibleStops))
        }
    }

    private fun handleStopSearched(state: AppState) = state.apply {
        if (selectedStop != null) {
            send(MapAction.MoveCameraToPosition(selectedStop.latLng))
            send(MapAction.SelectStopMarker(selectedStop))
        }
    }

    private fun handleInitialLocationUpdate(locationResult: LocationResult) {
        val lastLocation = locationResult.lastLocation
        if (lastLocation != null) {
            val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)
            send(MapAction.MoveCameraToPosition(latLng))
        } else {
            send(MapAction.MoveCameraToPosition(MINNEAPOLIS_LATLNG))
        }
    }

    private fun handleLocationButtonClicked(state: AppState) = state.apply {
        if (selectedStop != null) {
            send(MapAction.MoveCameraToPosition(selectedStop.latLng))
            send(MapAction.SelectStopMarker(selectedStop))
        }
    }
}
