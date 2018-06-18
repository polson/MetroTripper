package com.philsoft.metrotripper.app.state.transformer

import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.AppUiEvent
import com.philsoft.metrotripper.app.state.VehicleAction
import com.philsoft.metrotripper.model.Trip

class VehicleTransformer : AppActionTransformer<VehicleAction>() {

    override fun handleEvent(event: AppUiEvent, state: AppState) {
        when (event) {
            is AppUiEvent.GetTripsComplete -> handleGetTripsComplete(event.trips)
        }
    }

    private fun handleGetTripsComplete(trips: List<Trip>) {
        send(VehicleAction.ShowVehicles(trips))
    }
}
