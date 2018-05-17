package com.philsoft.metrotripper.app.state.transformer

import com.google.android.gms.maps.model.Marker
import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.AppStateTransformer
import com.philsoft.metrotripper.app.state.AppUiEvent
import com.philsoft.metrotripper.app.state.NexTripAction
import com.philsoft.metrotripper.model.Stop
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class NexTripApiActionTransformer : AppActionTransformer<NexTripAction>() {

    companion object {
        val UPDATE_INTERVAL = 10000L //ms
    }

    override fun handleEvent(event: AppUiEvent, state: AppState) {
        when (event) {
            is AppUiEvent.ScheduleButtonClicked -> handleScheduleButtonClicked(state.selectedStop)
            is AppUiEvent.MarkerClicked -> handleMarkerClicked(event.marker)
            is AppUiEvent.StopSearched -> handleStopSearched(event.stopId)
            is AppUiEvent.StopSelectedFromDrawer -> handleStopSelected(event.stop)
            is AppUiEvent.SlidingPanelExpanded -> handlePanelExpanded(state)
        }
    }

    private fun handleStopSelected(stop: Stop) {
        send(NexTripAction.GetTrips(stop.stopId))
    }

    private fun handlePanelExpanded(state: AppState) = state.apply {
        if (selectedStop != null) {
            send(NexTripAction.GetTrips(selectedStop.stopId))
        }
    }

    private fun handleMarkerClicked(marker: Marker) {
        send(NexTripAction.GetTrips(marker.title.toLong()))
    }

    private fun handleScheduleButtonClicked(selectedStop: Stop?) {
        if (selectedStop != null) {
            send(NexTripAction.GetTrips(selectedStop.stopId))
        }
    }

    private fun handleStopSearched(stopId: Long) {
        send(NexTripAction.GetTrips(stopId))
    }

    override fun apply(observable: Observable<AppStateTransformer.AppUiEventWithState>): ObservableSource<NexTripAction> {
        val actionObservable = super.apply(observable)
        return Observable.combineLatest(
                actionObservable,
                Observable.interval(UPDATE_INTERVAL, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()),
                BiFunction { action, interval ->
                    action
                }
        )
    }
}
