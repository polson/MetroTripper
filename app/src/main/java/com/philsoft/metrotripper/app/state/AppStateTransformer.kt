package com.philsoft.metrotripper.app.state

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Marker
import com.philsoft.metrotripper.app.SettingsProvider
import com.philsoft.metrotripper.database.DataProvider
import com.philsoft.metrotripper.model.Stop
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.withLatestFrom

class AppStateTransformer(private val dataProvider: DataProvider, private val settingsProvider: SettingsProvider) : ObservableTransformer<AppUiEvent, AppStateTransformer.AppUiEventWithState> {
    companion object {
        private val MAX_STOPS = 20
    }

    override fun apply(observable: Observable<AppUiEvent>): ObservableSource<AppUiEventWithState> {
        return observable
                .withLatestFrom(observable.compose(stateTransformer))
                .map { pair -> AppUiEventWithState(pair.first, pair.second) }
    }

    data class AppUiEventWithState(val appUiEvent: AppUiEvent, val appState: AppState)

    //State Transformers
    private val stateTransformer = ObservableTransformer<AppUiEvent, AppState> { observable ->
        val initialState = setupInitialState()
        observable.scan(initialState, { previousState, appUiEvent ->
            when (appUiEvent) {
                is AppUiEvent.StopSearched -> handleStopSearched(previousState, appUiEvent.stopId)
                is AppUiEvent.MarkerClicked -> handleMarkerClicked(previousState, appUiEvent.marker)
                is AppUiEvent.CameraIdle -> handleCameraIdle(previousState, appUiEvent.cameraPosition)
                is AppUiEvent.SaveStopButtonClicked -> handleSaveStopButtonClicked(previousState)
                is AppUiEvent.StopSelectedFromDrawer -> handleStopSelected(previousState, appUiEvent.stop)
                else -> previousState
            }
        })
    }

    private fun handleStopSelected(previousState: AppState, stop: Stop): AppState {
        val isStopSaved = settingsProvider.isStopSaved(stop.stopId)
        return previousState.copy(selectedStop = stop, isSelectedStopSaved = isStopSaved)
    }

    private fun setupInitialState(): AppState {
        val savedStopIds = settingsProvider.getSavedStopIds()
        val savedStops = dataProvider.getStopsById(savedStopIds)
        val savedStopsMap = LinkedHashMap(savedStops.associate { it.stopId to it })
        return AppState(savedStopsMap = savedStopsMap)
    }

    private fun handleSaveStopButtonClicked(previousState: AppState): AppState = previousState.run {
        if (selectedStop != null) {
            if (isSelectedStopSaved) {
                settingsProvider.unsaveStop(selectedStop.stopId)
                savedStopsMap.remove(selectedStop.stopId)
                copy(isSelectedStopSaved = false)
            } else {
                settingsProvider.saveStop(selectedStop.stopId)
                savedStopsMap.put(selectedStop.stopId, selectedStop)
                copy(isSelectedStopSaved = true)
            }
        } else this
    }

    private fun handleCameraIdle(previousState: AppState, cameraPosition: CameraPosition): AppState {
        val visibleStops = dataProvider.getClosestStops(cameraPosition.target.latitude, cameraPosition.target.longitude, MAX_STOPS)
        return previousState.copy(visibleStops = visibleStops)
    }

    private fun handleMarkerClicked(previousState: AppState, marker: Marker): AppState {
        val stopId = marker.title.toLong()
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
