package com.philsoft.metrotripper.app.state.transformer

import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.AppUiEvent
import com.philsoft.metrotripper.app.state.DrawerAction
import com.philsoft.metrotripper.model.Stop

class DrawerActionTransformer : AppActionTransformer<DrawerAction>() {

    override fun handleEvent(event: AppUiEvent, state: AppState) {
        when (event) {
            is AppUiEvent.StopSearched -> handleStopSearched(state.selectedStop)
            is AppUiEvent.StopSelectedFromDrawer -> handleStopSelected()
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
