package com.philsoft.metrotripper.app.state

import com.google.android.gms.maps.model.CameraPosition
import com.philsoft.metrotripper.app.SettingsProvider
import com.philsoft.metrotripper.database.DataProvider
import com.philsoft.metrotripper.model.Stop
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer

class AppStateTransformer(private val dataProvider: DataProvider, private val settingsProvider: SettingsProvider) : ObservableTransformer<AppUiEvent, AppState> {
    companion object {
        private const val MAX_STOPS = 20
        const val MIN_ZOOM_LEVEL = 16f
    }

    private val initialState = setupInitialState()

    override fun apply(observable: Observable<AppUiEvent>): ObservableSource<AppState> {
        return observable.scan(initialState, { previousState, appUiEvent -> updateState(appUiEvent, previousState) })
    }

    private fun updateState(uiEvent: AppUiEvent, previousState: AppState): AppState {
        return when (uiEvent) {
            is AppUiEvent.StopSearched -> handleStopSearched(previousState, uiEvent.stopId)
            is AppUiEvent.MarkerClicked -> handleMarkerClicked(previousState, uiEvent.stopId)
            is AppUiEvent.CameraIdle -> handleCameraIdle(previousState, uiEvent.cameraPosition)
            is AppUiEvent.SaveStopButtonClicked -> handleSaveStopButtonClicked(previousState)
            is AppUiEvent.StopSelectedFromDrawer -> handleStopSelected(previousState, uiEvent.stop)
            else -> previousState
        }.copy(appUiEvent = uiEvent)
    }

    private fun setupInitialState(): AppState {
        val savedStopIds = settingsProvider.getSavedStopIds()
        val savedStops = dataProvider.getStopsById(savedStopIds)
        val savedStopsMap = LinkedHashMap(savedStops.associate { it.stopId to it })
        return AppState(appUiEvent = AppUiEvent.Initialize, savedStopsMap = savedStopsMap)
    }

    private fun handleStopSelected(previousState: AppState, stop: Stop): AppState {
        val isStopSaved = settingsProvider.isStopSaved(stop.stopId)
        return previousState.copy(selectedStop = stop, isSelectedStopSaved = isStopSaved)
    }

    private fun handleSaveStopButtonClicked(previousState: AppState): AppState = previousState.run {
        if (selectedStop != null) {
            if (isSelectedStopSaved) {
                settingsProvider.unsaveStop(selectedStop.stopId)
                savedStopsMap.remove(selectedStop.stopId)
                copy(isSelectedStopSaved = false)
            } else {
                settingsProvider.saveStop(selectedStop.stopId)
                savedStopsMap[selectedStop.stopId] = selectedStop
                copy(isSelectedStopSaved = true)
            }
        } else this
    }

    private fun handleCameraIdle(previousState: AppState, cameraPosition: CameraPosition): AppState {
        val visibleStops = if (cameraPosition.zoom < MIN_ZOOM_LEVEL) {
            //Zoomed out too far, hide all stops
            arrayListOf()
        } else {
            dataProvider.getClosestStops(cameraPosition.target.latitude, cameraPosition.target.longitude, MAX_STOPS).toMutableList()
        }
        previousState.selectedStop?.let { visibleStops.add(it) }
        return previousState.copy(visibleStops = visibleStops)
    }

    private fun handleMarkerClicked(previousState: AppState, stopId: Long): AppState {
        val stop = dataProvider.getStopById(stopId)
        val isStopSaved = settingsProvider.isStopSaved(stopId)
        return previousState.copy(selectedStop = stop, isSelectedStopSaved = isStopSaved)
    }

    private fun handleStopSearched(previousState: AppState, stopId: Long): AppState {
        val stop = dataProvider.getStopById(stopId)
        val isStopSaved = settingsProvider.isStopSaved(stopId)
        return previousState.copy(selectedStop = stop, isSelectedStopSaved = isStopSaved)
    }
}
