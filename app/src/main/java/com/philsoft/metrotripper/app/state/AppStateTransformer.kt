package com.philsoft.metrotripper.app.state

import com.philsoft.metrotripper.database.DataProvider
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.withLatestFrom

class AppStateTransformer(private val dataProvider: DataProvider) : ObservableTransformer<AppUiEvent, AppStateTransformer.AppUiEventWithState> {

    override fun apply(observable: Observable<AppUiEvent>): ObservableSource<AppUiEventWithState> {
        return observable
                .withLatestFrom(observable.compose(stateTransformer))
                .map { pair -> AppUiEventWithState(pair.first, pair.second) }
    }

    data class AppUiEventWithState(val appUiEvent: AppUiEvent, val appState: AppState)

    //State Transformers
    private val stateTransformer = ObservableTransformer<AppUiEvent, AppState> { observable ->
        val initialState = AppState(null)
        observable.scan(initialState, { previousState, appUiEvent ->
            when (appUiEvent) {
                is AppUiEvent.StopSearched -> handleStopSearched(previousState, appUiEvent.stopId)
                else -> previousState
            }
        })
    }

    private fun handleStopSearched(previousState: AppState, stopId: Long): AppState {
        val stop = dataProvider.getStopById(stopId)
        return previousState.copy(selectedStop = stop)
    }
}
