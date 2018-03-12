package com.philsoft.metrotripper.app.state

import com.google.android.gms.maps.model.LatLng
import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.model.Trip

sealed class AppAction {
    object None : AppAction()
}


sealed class MapAction : AppAction() {
    class MoveCameraToPosition(val latLng: LatLng) : MapAction()
    class ShowStopMarkers(val stops: List<Stop>, val savedStopIds: Set<Long>) : MapAction()
    class SelectStopMarker(val stop: Stop, val isSaved: Boolean) : MapAction()
}

sealed class StopHeadingAction : AppAction() {
    object LoadingTrips : StopHeadingAction()
    class LoadTripsComplete : StopHeadingAction()
    class ShowStop(val stop: Stop, val isSaved: Boolean) : StopHeadingAction()
    class LoadTripsError : StopHeadingAction()
    object SaveStop : StopHeadingAction()
    object UnsaveStop : StopHeadingAction()
}

sealed class TripListAction : AppAction() {
    class ShowTrips(val trips: List<Trip>) : TripListAction()
}
