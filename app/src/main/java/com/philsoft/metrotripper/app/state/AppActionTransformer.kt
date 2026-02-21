package com.philsoft.metrotripper.app.state

import com.philsoft.metrotripper.app.state.transformer.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat

class AppActionTransformer {
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

    fun transform(appState: Flow<AppState>): Flow<AppAction> {
        return appState.flatMapConcat { state ->
            transformers.flatMap { it.buildActions(state) }.asFlow()
        }
    }
}
