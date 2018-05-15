package com.philsoft.metrotripper.app.state.transformer

import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.AppUiEvent
import com.philsoft.metrotripper.app.state.SlidingPanelAction

class SlidingPanelTransformer : AppActionTransformer<SlidingPanelAction>() {

    override fun handleEvent(event: AppUiEvent, state: AppState) {
        when (event) {
            is AppUiEvent.StopSearched -> handleStopSearched()
            is AppUiEvent.ScheduleButtonClicked -> handleScheduleButtonClicked()
            is AppUiEvent.LocationButtonClicked -> handleLocationButtonClicked()
        }
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
