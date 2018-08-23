package com.philsoft.metrotripper.app.state

import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.CameraPosition
import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.model.Trip

sealed class AppUiEvent {
    object Initialize : AppUiEvent()
}

sealed class NexTripApiEvent : AppUiEvent() {
    object GetTripsInFlight : NexTripApiEvent()
    class GetTripsComplete(val trips: List<Trip>) : NexTripApiEvent()
    object GetTripsFailed : NexTripApiEvent()
}

sealed class MapUiEvent : AppUiEvent() {
    class CameraIdle(val cameraPosition: CameraPosition) : MapUiEvent()
    class MarkerClicked(val stopId: Long) : MapUiEvent()
}

sealed class SlidingPanelUiEvent : AppUiEvent() {
    object SlidingPanelExpanded : SlidingPanelUiEvent()
}

sealed class StopHeadingUiEvent : AppUiEvent() {
    object ScheduleButtonClicked : StopHeadingUiEvent()
    object LocationButtonClicked : StopHeadingUiEvent()
    object SaveStopButtonClicked : StopHeadingUiEvent()
}

sealed class StopListUiEvent : AppUiEvent() {
    class StopSearched(val stopId: Long) : StopListUiEvent()
    class StopSelectedFromDrawer(val stop: Stop) : StopListUiEvent()
}

sealed class LocationUiEvent : AppUiEvent() {
    class InitialLocationUpdate(val locationResult: LocationResult) : LocationUiEvent()
}
