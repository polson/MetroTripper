package com.philsoft.metrotripper.app.state.transformer

import com.philsoft.metrotripper.app.state.AppAction
import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.AppStateTransformer
import com.philsoft.metrotripper.app.state.AppUiEvent
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer


abstract class AppActionTransformer<T : AppAction> : ObservableTransformer<AppStateTransformer.AppUiEventWithState, T> {
    //This is unfortunate that we have to jump out of the stream, but it saves us a TON of boilerplate.  There might
    //be a better way to handle this
    private lateinit var actions: ArrayList<T>

    override fun apply(observable: Observable<AppStateTransformer.AppUiEventWithState>): ObservableSource<T> {
        return observable
                .flatMap<T> { appEvent ->
                    actions = arrayListOf()
                    handleEvent(appEvent.appUiEvent, appEvent.appState)
                    Observable.fromIterable(actions.toList())
                }
    }

    abstract fun handleEvent(event: AppUiEvent, state: AppState)

    fun send(action: T) {
        actions.add(action)
    }
}
