package com.philsoft.metrotripper.app.state.transformer

import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.LocationUiEvent.InitialLocationUpdate
import com.philsoft.metrotripper.app.state.MapAction
import com.philsoft.metrotripper.app.state.MapUiEvent.CameraIdle
import com.philsoft.metrotripper.app.state.StopHeadingUiEvent.LocationButtonClicked
import com.philsoft.metrotripper.app.state.StopListUiEvent.StopSearched
import com.philsoft.metrotripper.app.state.StopListUiEvent.StopSelectedFromDrawer
import com.philsoft.metrotripper.model.Stop

class MapTransformer : ViewActionTransformer<MapAction>() {

    companion object {
        private val MINNEAPOLIS_LATLNG = LatLng(44.9799700, -93.2638400)
    }

    override fun handleEvent(state: AppState) = state.appUiEvent.run {
        when (this) {
            is InitialLocationUpdate -> handleInitialLocationUpdate(locationResult)
            is LocationButtonClicked -> handleLocationButtonClicked(state.selectedStop)
            is StopSearched -> handleStopSearched(state.selectedStop)
            is StopSelectedFromDrawer -> handleStopSelected(stop)
            is CameraIdle -> handleCameraIdle(state.visibleStops)
        }
    }

    private fun handleStopSelected(stop: Stop) {
        send(MapAction.MoveCameraToPosition(stop.latLng))
        send(MapAction.SelectStopMarker(stop))
    }

    private fun handleCameraIdle(visibleStops: List<Stop>) {
        send(MapAction.ShowStopMarkers(visibleStops))
    }

    private fun handleStopSearched(selectedStop: Stop?) {
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

    private fun handleLocationButtonClicked(selectedStop: Stop?) {
        if (selectedStop != null) {
            send(MapAction.MoveCameraToPosition(selectedStop.latLng))
            send(MapAction.SelectStopMarker(selectedStop))
        }
    }
}
