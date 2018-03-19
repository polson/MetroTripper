package com.philsoft.metrotripper.app.state

import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Marker

sealed class AppUiEvent {
    object HeadingButtonClicked : AppUiEvent()
    object ScheduleButtonClicked : AppUiEvent()
    object LocationButtonClicked : AppUiEvent()
    class StopSearched(val stopId: Long) : AppUiEvent()
    class CameraIdle(val cameraPosition: CameraPosition) : AppUiEvent()
    class InitialLocationUpdate(val locationResult: LocationResult) : AppUiEvent()
    class MarkerClicked(val marker: Marker) : AppUiEvent()
    object SaveStopButtonClicked : AppUiEvent()
}

data class AppUiEventWithState(val event: AppUiEvent, val state: AppState)