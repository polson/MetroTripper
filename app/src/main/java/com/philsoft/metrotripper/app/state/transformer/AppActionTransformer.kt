package com.philsoft.metrotripper.app.state.transformer

import com.philsoft.metrotripper.app.state.AppAction
import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.AppUiEvent


abstract class AppActionTransformer<T : AppAction> {
    protected val actions: ArrayList<T> = arrayListOf()

    protected abstract fun handleEvent(event: AppUiEvent, state: AppState)

    fun buildActions(event: AppUiEvent, state: AppState): List<T> {
        handleEvent(event, state)
        val output = actions.toList()
        actions.clear()
        return output
    }

    protected fun send(action: T) {
        actions.add(action)
    }
}
