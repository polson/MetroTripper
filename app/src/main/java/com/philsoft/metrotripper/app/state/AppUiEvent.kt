package com.philsoft.metrotripper.app.state

import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.CameraPosition
import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.model.Trip

sealed class AppUiEvent {
    object ScheduleButtonClicked : AppUiEvent()
    object LocationButtonClicked : AppUiEvent()
    class StopSearched(val stopId: Long) : AppUiEvent()
    class StopSelectedFromDrawer(val stop: Stop) : AppUiEvent()
    class CameraIdle(val cameraPosition: CameraPosition) : AppUiEvent()
    class InitialLocationUpdate(val locationResult: LocationResult) : AppUiEvent()
    class MarkerClicked(val stopId: Long) : AppUiEvent()
    object SaveStopButtonClicked : AppUiEvent()
    object GetTripsInFlight : AppUiEvent()
    class GetTripsComplete(val trips: List<Trip>) : AppUiEvent()
    object GetTripsFailed : AppUiEvent()
    object SlidingPanelExpanded : AppUiEvent()
    object Initialize : AppUiEvent()
}
