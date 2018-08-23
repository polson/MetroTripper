package com.philsoft.metrotripper.app.state.transformer

import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.AppUiEvent.Initialize
import com.philsoft.metrotripper.app.state.MapUiEvent.MarkerClicked
import com.philsoft.metrotripper.app.state.StopHeadingUiEvent.SaveStopButtonClicked
import com.philsoft.metrotripper.app.state.StopListAction
import com.philsoft.metrotripper.app.state.StopListUiEvent.StopSearched
import com.philsoft.metrotripper.app.state.StopListUiEvent.StopSelectedFromDrawer
import com.philsoft.metrotripper.model.Stop

class StopListTransformer : ViewActionTransformer<StopListAction>() {

    override fun handleEvent(state: AppState) = state.appUiEvent.run {
        when (this) {
            is Initialize -> handleInitialize(state)
            is SaveStopButtonClicked -> handleSaveStopButtonClicked(state)
            is StopSelectedFromDrawer -> handleStopSelected(stop)
            is StopSearched -> handleStopSearched(state)
            is MarkerClicked -> handleMarkerClicked(state)
        }
    }

    private fun handleMarkerClicked(state: AppState) {
        if (state.selectedStop != null) {
            send(StopListAction.SetStopSelected(state.selectedStop.stopId))
        }
    }

    private fun handleStopSearched(state: AppState) {
        if (state.selectedStop != null) {
            send(StopListAction.SetStopSelected(state.selectedStop.stopId))
        }
    }

    private fun handleStopSelected(stop: Stop) {
        send(StopListAction.SetStopSelected(stop.stopId))
    }

    private fun handleSaveStopButtonClicked(state: AppState) {
        val stops = state.savedStopsMap.values.toList()
        send(StopListAction.ShowStops(stops))
    }

    private fun handleInitialize(state: AppState) {
        val stops = state.savedStopsMap.values.toList()
        send(StopListAction.ShowStops(stops))
    }
}
