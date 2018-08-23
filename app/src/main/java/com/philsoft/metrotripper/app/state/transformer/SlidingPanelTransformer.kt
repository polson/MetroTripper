package com.philsoft.metrotripper.app.state.transformer

import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.AppUiEvent
import com.philsoft.metrotripper.app.state.SlidingPanelAction
import com.philsoft.metrotripper.app.ui.view.StopHeadingView.StopHeadingUiEvent.LocationButtonClicked
import com.philsoft.metrotripper.app.ui.view.StopHeadingView.StopHeadingUiEvent.ScheduleButtonClicked
import com.philsoft.metrotripper.app.ui.view.StopListView.StopListUiEvent.StopSearched
import com.philsoft.metrotripper.app.ui.view.StopListView.StopListUiEvent.StopSelectedFromDrawer

class SlidingPanelTransformer : ViewActionTransformer<SlidingPanelAction>() {

    override fun handleEvent(state: AppState) {
        when (state.appUiEvent) {
            is StopSearched -> handleStopSearched()
            is StopSelectedFromDrawer -> handleStopSelected()
            is ScheduleButtonClicked -> handleScheduleButtonClicked()
            is LocationButtonClicked -> handleLocationButtonClicked()
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
