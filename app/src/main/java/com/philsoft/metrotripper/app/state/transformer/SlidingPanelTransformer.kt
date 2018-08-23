package com.philsoft.metrotripper.app.state.transformer

import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.AppUiEvent
import com.philsoft.metrotripper.app.state.SlidingPanelAction

class SlidingPanelTransformer : ViewActionTransformer<SlidingPanelAction>() {

    override fun handleEvent(state: AppState) {
        when (state.appUiEvent) {
            is AppUiEvent.StopSearched -> handleStopSearched()
            is AppUiEvent.StopSelectedFromDrawer -> handleStopSelected()
            is AppUiEvent.ScheduleButtonClicked -> handleScheduleButtonClicked()
            is AppUiEvent.LocationButtonClicked -> handleLocationButtonClicked()
        }
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
