package com.philsoft.metrotripper.app.state.transformer

import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.AppUiEvent
import com.philsoft.metrotripper.app.state.StopHeadingAction
import com.philsoft.metrotripper.model.Stop


class StopHeadingTransformer : AppActionTransformer<StopHeadingAction>() {

    override fun handleEvent(event: AppUiEvent, state: AppState) {
        when (event) {
            is AppUiEvent.SaveStopButtonClicked -> handleSaveStopButtonClicked(state)
            is AppUiEvent.StopSearched -> handleStopSearched(state)
            is AppUiEvent.ScheduleButtonClicked -> handleScheduleButtonClicked()
            is AppUiEvent.GetTripsComplete -> handleGetTripsComplete()
            is AppUiEvent.GetTripsInFlight -> handleGetTripsInFlight()
            is AppUiEvent.GetTripsFailed -> handleGetTripsFailed()
            is AppUiEvent.MarkerClicked -> handleMarkerClicked(state)
            is AppUiEvent.StopSelectedFromDrawer -> handleStopSelected(state, event.stop)
        }
    }

    private fun handleStopSelected(state: AppState, stop: Stop) {
        send(StopHeadingAction.ShowStop(stop, state.isSelectedStopSaved))
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

    private fun handleSaveStopButtonClicked(state: AppState) = state.apply {
        if (selectedStop != null) {
            send(StopHeadingAction.ShowStop(selectedStop, isSelectedStopSaved))
        }
    }
}
