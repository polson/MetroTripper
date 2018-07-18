package com.philsoft.metrotripper.app.state.transformer

import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.AppUiEvent
import com.philsoft.metrotripper.app.state.NexTripAction
import com.philsoft.metrotripper.model.Stop

class NexTripApiActionTransformer : AppActionTransformer<NexTripAction>() {

    companion object {
        val UPDATE_INTERVAL = 10000L //ms
    }

    override fun handleEvent(event: AppUiEvent, state: AppState) {
        when (event) {
            is AppUiEvent.ScheduleButtonClicked -> handleScheduleButtonClicked(state.selectedStop)
            is AppUiEvent.MarkerClicked -> handleMarkerClicked(event.stopId)
            is AppUiEvent.StopSearched -> handleStopSearched(event.stopId)
            is AppUiEvent.StopSelectedFromDrawer -> handleStopSelected(event.stop)
            is AppUiEvent.SlidingPanelExpanded -> handlePanelExpanded(state)
        }
    }

    private fun handleStopSelected(stop: Stop) {
        send(NexTripAction.GetTrips(stop.stopId))
    }

    private fun handlePanelExpanded(state: AppState) = state.apply {
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
