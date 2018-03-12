package com.philsoft.metrotripper.app.state

import com.google.android.gms.maps.model.CameraPosition

sealed class AppEvent {
    object ShowStops : AppEvent()
    object ShowSchedule : AppEvent()
    object ShowCurrentStopLocation : AppEvent()
    class SearchStop(val stopId: Long) : AppEvent()
    class CameraIdle(val cameraPosition: CameraPosition) : AppEvent()
}