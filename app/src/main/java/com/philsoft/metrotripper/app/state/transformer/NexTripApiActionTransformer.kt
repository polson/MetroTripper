package com.philsoft.metrotripper.app.state.transformer

import com.google.android.gms.maps.model.Marker
import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.AppUiEvent
import com.philsoft.metrotripper.app.state.NexTripAction
import com.philsoft.metrotripper.model.Stop

class NexTripApiActionTransformer : AppActionTransformer<NexTripAction>() {

    override fun handleEvent(event: AppUiEvent, state: AppState) {
        when (event) {
            is AppUiEvent.ScheduleButtonClicked -> handleScheduleButtonClicked(state.selectedStop)
            is AppUiEvent.MarkerClicked -> handleMarkerClicked(event.marker)
            is AppUiEvent.StopSearched -> handleStopSearched(event.stopId)
        }
    }

    private fun handleMarkerClicked(marker: Marker) {
        send(NexTripAction.GetTrips(marker.title.toLong()))
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
