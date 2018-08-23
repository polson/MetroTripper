package com.philsoft.metrotripper.app.state.transformer

import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.AppUiEvent
import com.philsoft.metrotripper.app.state.NexTripAction
import com.philsoft.metrotripper.model.Stop

class NexTripApiActionTransformer : ViewActionTransformer<NexTripAction>() {

    override fun handleEvent(state: AppState) = state.run {
        when (appUiEvent) {
            is AppUiEvent.ScheduleButtonClicked -> handleScheduleButtonClicked(selectedStop)
            is AppUiEvent.MarkerClicked -> handleMarkerClicked(appUiEvent.stopId)
            is AppUiEvent.StopSearched -> handleStopSearched(appUiEvent.stopId)
            is AppUiEvent.StopSelectedFromDrawer -> handleStopSelected(appUiEvent.stop)
            is AppUiEvent.SlidingPanelExpanded -> handlePanelExpanded(selectedStop)
        }
        Unit
    }

    private fun handleStopSelected(stop: Stop) {
        send(NexTripAction.GetTrips(stop.stopId))
    }

    private fun handlePanelExpanded(selectedStop: Stop?) {
        if (selectedStop != null) {
            send(NexTripAction.GetTrips(selectedStop.stopId))
        }
    }

    private fun handleMarkerClicked(stopId: Long) {
        send(NexTripAction.GetTrips(stopId))
    }

    private fun handleScheduleButtonClicked(selectedStop: Stop?) {
        if (selectedStop != null) {
            send(NexTripAction.GetTrips(selectedStop.stopId))
        }
    }

    private fun handleStopSearched(stopId: Long) {
        send(NexTripAction.GetTrips(stopId))
    }
}
