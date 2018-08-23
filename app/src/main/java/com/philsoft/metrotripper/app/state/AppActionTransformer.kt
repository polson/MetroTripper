package com.philsoft.metrotripper.app.state

import com.philsoft.metrotripper.app.state.transformer.*
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.toObservable

class AppActionTransformer : ObservableTransformer<AppState, AppAction> {
    private val transformers = listOf(
            MapTransformer(),
            StopHeadingTransformer(),
            DrawerActionTransformer(),
            NexTripApiActionTransformer(),
            SlidingPanelTransformer(),
            TripListTransformer(),
            StopListTransformer(),
            VehicleTransformer()
    )

    // Build a list of actions and convert it to an observable.
    override fun apply(upstream: Observable<AppState>): ObservableSource<AppAction> {
        return upstream.flatMap { appState ->
            // This is the kotlin collections flatmap, not the rx flatmap!
            transformers.flatMap { it.buildActions(appState) }.toObservable()
        }
    }
}
