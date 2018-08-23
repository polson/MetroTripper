package com.philsoft.metrotripper.app.state.transformer

import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.MapUiEvent.MarkerClicked
import com.philsoft.metrotripper.app.state.NexTripApiEvent.*
import com.philsoft.metrotripper.app.state.TripListAction
import com.philsoft.metrotripper.model.Trip

class TripListTransformer : ViewActionTransformer<TripListAction>() {

    override fun handleEvent(state: AppState) = state.appUiEvent.run {
        when (this) {
            is GetTripsComplete -> handleGetTripsComplete(trips)
            is GetTripsFailed -> handleGetTripsFailed()
            is MarkerClicked -> handleMarkerClicked()
        }
    }

    private fun handleMarkerClicked() {
        send(TripListAction.ShowTrips(arrayListOf()))
    }

    private fun handleGetTripsComplete(trips: List<Trip>) {
        send(TripListAction.ShowTrips(trips))
    }

    private fun handleGetTripsFailed() {
        send(TripListAction.ShowTrips(arrayListOf()))
    }
}
