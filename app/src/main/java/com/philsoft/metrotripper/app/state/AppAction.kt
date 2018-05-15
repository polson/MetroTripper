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
    object LoadTripsComplete : StopHeadingAction()
    class ShowStop(val stop: Stop, val isSaved: Boolean) : StopHeadingAction()
    object LoadTripsError : StopHeadingAction()
    object SaveStop : StopHeadingAction()
    object UnsaveStop : StopHeadingAction()
}

sealed class TripListAction : AppAction() {
    class ShowTrips(val trips: List<Trip>) : TripListAction()
}

sealed class DrawerAction : AppAction() {
    object CloseDrawer : DrawerAction()
}

sealed class NexTripAction : AppAction() {
    class GetTrips(val stopId: Long) : NexTripAction()
}

sealed class SlidingPanelAction : AppAction() {
    object Expand : SlidingPanelAction()
    object Collapse : SlidingPanelAction()
    object Hide : SlidingPanelAction()
}
