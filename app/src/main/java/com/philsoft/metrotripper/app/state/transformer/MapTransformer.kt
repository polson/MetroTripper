package com.philsoft.metrotripper.app.state.transformer

import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.philsoft.metrotripper.app.SettingsProvider
import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.AppUiEvent
import com.philsoft.metrotripper.app.state.MapAction
import com.philsoft.metrotripper.database.DataProvider
import com.philsoft.metrotripper.model.Stop

class MapTransformer(val settingsProvider: SettingsProvider, val dataProvider: DataProvider) : AppActionTransformer<MapAction>() {

    companion object {
        private val MAX_STOPS = 20
        private val MINNEAPOLIS_LATLNG = LatLng(44.9799700, -93.2638400)
    }

    override fun handleEvent(event: AppUiEvent, state: AppState) {
        when (event) {
            is AppUiEvent.InitialLocationUpdate -> handleInitialLocationUpdate(event.locationResult)
            is AppUiEvent.LocationButtonClicked -> handleLocationButtonClicked(state.selectedStop)
            is AppUiEvent.StopSearched -> handleStopSearched(state.selectedStop, event.stopId)
            is AppUiEvent.CameraIdle -> handleCameraIdle(event.cameraPosition)
        }
    }

    private fun handleCameraIdle(cameraPosition: CameraPosition) {
        val stops = dataProvider.getClosestStops(cameraPosition.target.latitude, cameraPosition.target.longitude, MAX_STOPS)
        val savedStopIds = settingsProvider.getSavedStopIds()
        send(MapAction.ShowStopMarkers(stops, savedStopIds))
    }

    private fun handleStopSearched(selectedStop: Stop?, stopId: Long) {
        if (selectedStop != null) {
            val isSavedStop = settingsProvider.isStopSaved(stopId)
            send(MapAction.MoveCameraToPosition(selectedStop.latLng))
            send(MapAction.SelectStopMarker(selectedStop, isSavedStop))
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
        }
    }
}
