package com.philsoft.metrotripper.app.ui.view

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import com.philsoft.metrotripper.app.drawer.StopListAdapter
import com.philsoft.metrotripper.model.Stop
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class StopListView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RecyclerView(context, attrs, defStyleAttr) {

    private val stopListAdapter = StopListAdapter()

    val stopSearchedEvent: Flow<Long> = callbackFlow {
        stopListAdapter.stopSearchedListener = { stopId ->
            trySend(stopId)
        }
        awaitClose { stopListAdapter.stopSearchedListener = {} }
    }

    val stopSelectedEvent: Flow<Stop> = callbackFlow {
        stopListAdapter.stopSelectedListener = { stop ->
            trySend(stop)
        }
        awaitClose { stopListAdapter.stopSelectedListener = {} }
    }

    init {
        layoutManager = LinearLayoutManager(context)
        adapter = stopListAdapter
    }

    fun showStops(stops: List<Stop>) {
        stopListAdapter.showStops(stops)
    }

    fun selectStop(stopId: Long) {
        stopListAdapter.selectedStopId = stopId
    }
}
