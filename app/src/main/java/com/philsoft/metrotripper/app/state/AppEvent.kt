package com.philsoft.metrotripper.app.state

import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Marker

sealed class AppEvent {
    object ShowStops : AppEvent()
    object ShowSchedule : AppEvent()
    object ShowCurrentStopLocation : AppEvent()
    class SearchStop(val stopId: Long) : AppEvent()
    class CameraIdle(val cameraPosition: CameraPosition) : AppEvent()
    class InitialLocationUpdate(val locationResult: LocationResult) : AppEvent()
    class MarkerClick(val marker: Marker) : AppEvent()
    object SaveStop : AppEvent()
}