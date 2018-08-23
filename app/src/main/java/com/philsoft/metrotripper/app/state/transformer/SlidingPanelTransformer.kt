package com.philsoft.metrotripper.app.state.transformer

import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.MapUiEvent.MarkerClicked
import com.philsoft.metrotripper.app.state.SlidingPanelAction
import com.philsoft.metrotripper.app.state.StopHeadingUiEvent.LocationButtonClicked
import com.philsoft.metrotripper.app.state.StopHeadingUiEvent.ScheduleButtonClicked
import com.philsoft.metrotripper.app.state.StopListUiEvent.StopSearched
import com.philsoft.metrotripper.app.state.StopListUiEvent.StopSelectedFromDrawer

class SlidingPanelTransformer : ViewActionTransformer<SlidingPanelAction>() {

    override fun handleEvent(state: AppState) {
        when (state.appUiEvent) {
            is StopSearched -> handleStopSearched()
            is StopSelectedFromDrawer -> handleStopSelected()
            is ScheduleButtonClicked -> handleScheduleButtonClicked()
            is LocationButtonClicked -> handleLocationButtonClicked()
            is MarkerClicked -> handleMarkerClicked()
        }
    }

    private fun handleMarkerClicked() {
        send(SlidingPanelAction.Collapse)
    }

    private fun handleStopSelected() {
        send(SlidingPanelAction.Collapse)
    }

    private fun handleLocationButtonClicked() {
        send(SlidingPanelAction.Collapse)
    }

    private fun handleScheduleButtonClicked() {
        send(SlidingPanelAction.Expand)
    }

    private fun handleStopSearched() {
        send(SlidingPanelAction.Collapse)
    }
}
