package com.philsoft.metrotripper.app.ui.view

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import com.philsoft.metrotripper.app.drawer.StopListAdapter
import com.philsoft.metrotripper.app.state.StopListAction
import com.philsoft.metrotripper.app.state.StopListUiEvent
import com.philsoft.metrotripper.app.state.StopListUiEvent.StopSearched
import com.philsoft.metrotripper.app.state.StopListUiEvent.StopSelectedFromDrawer
import com.philsoft.metrotripper.model.Stop
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class StopListView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RecyclerView(context, attrs, defStyleAttr) {

    private val stopListAdapter = StopListAdapter()

    val stopSearchedEvent: Flow<StopSearched> = callbackFlow {
        stopListAdapter.stopSearchedListener = { stopId ->
            trySend(StopListUiEvent.StopSearched(stopId))
        }
        awaitClose { stopListAdapter.stopSearchedListener = {} }
    }

    val stopSelectedEvent: Flow<StopSelectedFromDrawer> = callbackFlow {
        stopListAdapter.stopSelectedListener = { stop ->
            trySend(StopListUiEvent.StopSelectedFromDrawer(stop))
        }
        awaitClose { stopListAdapter.stopSelectedListener = {} }
    }

    init {
        layoutManager = LinearLayoutManager(context)
        adapter = stopListAdapter
    }

    fun render(action: StopListAction) = when (action) {
        is StopListAction.ShowStops -> showStops(action.stops)
        is StopListAction.SetStopSelected -> selectStop(action.stopId)
    }

    private fun selectStop(stopId: Long) {
        stopListAdapter.selectedStopId = stopId
    }

    private fun showStops(stops: List<Stop>) {
        stopListAdapter.showStops(stops)
    }
}
