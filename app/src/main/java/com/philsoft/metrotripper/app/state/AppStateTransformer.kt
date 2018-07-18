package com.philsoft.metrotripper.app.state

import com.google.android.gms.maps.model.CameraPosition
import com.philsoft.metrotripper.app.SettingsProvider
import com.philsoft.metrotripper.database.DataProvider
import com.philsoft.metrotripper.model.Stop
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.withLatestFrom
import timber.log.Timber

class AppStateTransformer(private val dataProvider: DataProvider, private val settingsProvider: SettingsProvider) : ObservableTransformer<AppUiEvent, AppUiEventWithState> {
    companion object {
        private const val MAX_STOPS = 20
        const val MIN_ZOOM_LEVEL = 16f
    }

    private val initialState = setupInitialState()

    override fun apply(observable: Observable<AppUiEvent>): ObservableSource<AppUiEventWithState> {
        return observable.withLatestFrom(observable.scan(initialState, { previousState, appUiEvent ->
            Timber.d("State transformation: $appUiEvent")
            when (appUiEvent) {
                is AppUiEvent.StopSearched -> handleStopSearched(previousState, appUiEvent.stopId)
                is AppUiEvent.MarkerClicked -> handleMarkerClicked(previousState, appUiEvent.stopId)
                is AppUiEvent.CameraIdle -> handleCameraIdle(previousState, appUiEvent.cameraPosition)
                is AppUiEvent.SaveStopButtonClicked -> handleSaveStopButtonClicked(previousState)
                is AppUiEvent.StopSelectedFromDrawer -> handleStopSelected(previousState, appUiEvent.stop)
                else -> previousState
            }
        })).map { pair -> AppUiEventWithState(pair.first, pair.second) }
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
