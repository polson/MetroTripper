package com.philsoft.metrotripper.app.state.transformer

import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.MapUiEvent.MarkerClicked
import com.philsoft.metrotripper.app.state.NexTripAction
import com.philsoft.metrotripper.app.state.SlidingPanelUiEvent.SlidingPanelExpanded
import com.philsoft.metrotripper.app.state.StopHeadingUiEvent.ScheduleButtonClicked
import com.philsoft.metrotripper.app.state.StopListUiEvent.StopSearched
import com.philsoft.metrotripper.app.state.StopListUiEvent.StopSelectedFromDrawer
import com.philsoft.metrotripper.model.Stop

class NexTripApiActionTransformer : ViewActionTransformer<NexTripAction>() {

    override fun handleEvent(state: AppState) = state.run {
        when (appUiEvent) {
            is ScheduleButtonClicked -> handleScheduleButtonClicked(selectedStop)
            is MarkerClicked -> handleMarkerClicked(appUiEvent.stopId)
            is StopSearched -> handleStopSearched(appUiEvent.stopId)
            is StopSelectedFromDrawer -> handleStopSelected(appUiEvent.stop)
            is SlidingPanelExpanded -> handlePanelExpanded(selectedStop)
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
