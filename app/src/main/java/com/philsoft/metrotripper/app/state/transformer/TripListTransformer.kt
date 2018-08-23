package com.philsoft.metrotripper.app.state.transformer

import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.AppUiEvent
import com.philsoft.metrotripper.app.state.TripListAction
import com.philsoft.metrotripper.app.ui.view.NexTripApiHelper.NexTripApiEvent.GetTripsComplete
import com.philsoft.metrotripper.app.ui.view.NexTripApiHelper.NexTripApiEvent.GetTripsFailed
import com.philsoft.metrotripper.model.Trip

class TripListTransformer : ViewActionTransformer<TripListAction>() {

    override fun handleEvent(state: AppState) = state.appUiEvent.run {
        when (this) {
            is GetTripsComplete -> handleGetTripsComplete(trips)
            is GetTripsFailed -> handleGetTripsFailed()
        }
    }

    private fun handleGetTripsComplete(trips: List<Trip>) {
        send(TripListAction.ShowTrips(trips))
    }

    private fun handleGetTripsFailed() {
        //TODO
    }
}
