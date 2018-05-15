package com.philsoft.metrotripper.app.state.transformer

import com.philsoft.metrotripper.app.SettingsProvider
import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.AppUiEvent
import com.philsoft.metrotripper.app.state.StopHeadingAction
import com.philsoft.metrotripper.model.Stop


class StopHeadingTransformer(val settingsProvider: SettingsProvider) : AppActionTransformer<StopHeadingAction>() {

    override fun handleEvent(event: AppUiEvent, state: AppState) {
        when (event) {
            AppUiEvent.SaveStopButtonClicked -> handleSaveStopButtonClicked(state.selectedStop)
            is AppUiEvent.StopSearched -> handleStopSearched(state.selectedStop)
            AppUiEvent.ScheduleButtonClicked -> handleScheduleButtonClicked()
        }
    }

    private fun handleScheduleButtonClicked() {
        send(StopHeadingAction.LoadingTrips)
    }

    private fun handleStopSearched(selectedStop: Stop?) {
        if (selectedStop != null) {
            val isSavedStop = settingsProvider.isStopSaved(selectedStop.stopId)
            send(StopHeadingAction.ShowStop(selectedStop, isSavedStop))
        }
    }

    private fun handleSaveStopButtonClicked(selectedStop: Stop?) {
        val stopId = selectedStop?.stopId ?: return
        if (settingsProvider.isStopSaved(stopId)) {
            settingsProvider.unsaveStop(stopId)
            send(StopHeadingAction.UnsaveStop)
        } else {
            settingsProvider.saveStop(stopId)
            send(StopHeadingAction.SaveStop)
        }
    }
}