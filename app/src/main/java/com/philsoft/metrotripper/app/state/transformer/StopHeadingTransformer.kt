package com.philsoft.metrotripper.app.state.transformer

import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.MapUiEvent.MarkerClicked
import com.philsoft.metrotripper.app.state.NexTripApiEvent.*
import com.philsoft.metrotripper.app.state.StopHeadingAction
import com.philsoft.metrotripper.app.state.StopHeadingUiEvent.SaveStopButtonClicked
import com.philsoft.metrotripper.app.state.StopHeadingUiEvent.ScheduleButtonClicked
import com.philsoft.metrotripper.app.state.StopListUiEvent.StopSearched
import com.philsoft.metrotripper.app.state.StopListUiEvent.StopSelectedFromDrawer
import com.philsoft.metrotripper.model.Stop


class StopHeadingTransformer : ViewActionTransformer<StopHeadingAction>() {

    override fun handleEvent(state: AppState) = state.appUiEvent.run {
        when (this) {
            is StopSelectedFromDrawer -> handleStopSelected(stop, state.isSelectedStopSaved)
            is StopSearched -> handleStopSearched(state)
            is ScheduleButtonClicked -> handleScheduleButtonClicked()
            is GetTripsComplete -> handleGetTripsComplete()
            is GetTripsInFlight -> handleGetTripsInFlight()
            is GetTripsFailed -> handleGetTripsFailed()
            is MarkerClicked -> handleMarkerClicked(state)
            is SaveStopButtonClicked -> handleSaveStopButtonClicked(state.selectedStop, state.isSelectedStopSaved)
            else -> { }
        }
    }

    private fun handleStopSelected(stop: Stop, isStopSaved: Boolean) {
        send(StopHeadingAction.ShowStop(stop, isStopSaved))
    }

    private fun handleGetTripsInFlight() {
        send(StopHeadingAction.LoadingTrips)
    }

    private fun handleMarkerClicked(state: AppState) = state.apply {
        if (selectedStop != null) {
            send(StopHeadingAction.ShowStop(selectedStop, isSelectedStopSaved))
        }
    }

    private fun handleGetTripsComplete() {
        send(StopHeadingAction.LoadTripsComplete)
    }

    private fun handleGetTripsFailed() {
        send(StopHeadingAction.LoadTripsError)
    }

    private fun handleScheduleButtonClicked() {
        send(StopHeadingAction.LoadingTrips)
    }

    private fun handleStopSearched(state: AppState) = state.apply {
        if (selectedStop != null) {
            send(StopHeadingAction.ShowStop(selectedStop, isSelectedStopSaved))
        }
    }

    private fun handleSaveStopButtonClicked(stop: Stop?, isStopSaved: Boolean) {
        if (stop != null) {
            send(StopHeadingAction.ShowStop(stop, isStopSaved))
        }
    }
}
