package com.philsoft.metrotripper.app.state.transformer

import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.AppUiEvent
import com.philsoft.metrotripper.app.state.TripListAction
import com.philsoft.metrotripper.model.Trip

class TripListTransformer : AppActionTransformer<TripListAction>() {

    override fun handleEvent(event: AppUiEvent, state: AppState) {
        when (event) {
            is AppUiEvent.GetTripsComplete -> handleGetTripsComplete(event.trips)
            is AppUiEvent.GetTripsFailed -> handleGetTripsFailed()
        }
    }

    private fun handleGetTripsComplete(trips: List<Trip>) {
        send(TripListAction.ShowTrips(trips))
    }

    private fun handleGetTripsFailed() {
        //TODO
    }
}
