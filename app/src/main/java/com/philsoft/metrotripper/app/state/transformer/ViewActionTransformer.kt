package com.philsoft.metrotripper.app.state.transformer

import com.philsoft.metrotripper.app.state.AppAction
import com.philsoft.metrotripper.app.state.AppState
import com.philsoft.metrotripper.app.state.AppUiEvent


abstract class ViewActionTransformer<T : AppAction> {
    protected val actions: ArrayList<T> = arrayListOf()

    protected abstract fun handleEvent(state: AppState): Any

    fun buildActions(state: AppState): List<T> {
        handleEvent(state)
        val output = actions.toList()
        actions.clear()
        return output
    }

    protected fun send(action: T) {
        actions.add(action)
    }
}
