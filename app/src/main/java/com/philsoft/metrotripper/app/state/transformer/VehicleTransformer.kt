package com.philsoft.metrotripper.app.state.transformer

import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.AppUiEvent
import com.philsoft.metrotripper.app.state.VehicleAction
import com.philsoft.metrotripper.app.ui.view.NexTripApiHelper.NexTripApiEvent.GetTripsComplete
import com.philsoft.metrotripper.model.Trip

class VehicleTransformer : ViewActionTransformer<VehicleAction>() {

    override fun handleEvent(state: AppState) = state.appUiEvent.run {
        when (this) {
            is GetTripsComplete -> handleGetTripsComplete(trips)
        }
    }

    private fun handleGetTripsComplete(trips: List<Trip>) {
        send(VehicleAction.ShowVehicles(trips))
    }
}
