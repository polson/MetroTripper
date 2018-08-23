package com.philsoft.metrotripper.app.state.transformer

import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.DrawerAction
import com.philsoft.metrotripper.app.state.StopListUiEvent.StopSearched
import com.philsoft.metrotripper.app.state.StopListUiEvent.StopSelectedFromDrawer
import com.philsoft.metrotripper.model.Stop

class DrawerActionTransformer : ViewActionTransformer<DrawerAction>() {

    override fun handleEvent(state: AppState) {
        when (state.appUiEvent) {
            is StopSearched -> handleStopSearched(state.selectedStop)
            is StopSelectedFromDrawer -> handleStopSelected()
        }
    }

    private fun handleStopSelected() {
        send(DrawerAction.CloseDrawer)
    }

    private fun handleStopSearched(selectedStop: Stop?) {
        if (selectedStop != null) {
            send(DrawerAction.CloseDrawer)
        }
    }
}
