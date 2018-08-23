package com.philsoft.metrotripper.app.ui.view

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import com.philsoft.metrotripper.app.drawer.StopListAdapter
import com.philsoft.metrotripper.app.state.AppUiEvent
import com.philsoft.metrotripper.app.state.StopListAction
import com.philsoft.metrotripper.app.ui.view.StopListView.StopListUiEvent.StopSearched
import com.philsoft.metrotripper.app.ui.view.StopListView.StopListUiEvent.StopSelectedFromDrawer
import com.philsoft.metrotripper.model.Stop
import io.reactivex.Observable

class StopListView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RecyclerView(context, attrs, defStyleAttr) {

    sealed class StopListUiEvent : AppUiEvent() {
        class StopSearched(val stopId: Long) : AppUiEvent()
        class StopSelectedFromDrawer(val stop: Stop) : AppUiEvent()
    }

    private val stopListAdapter = StopListAdapter()

    val stopSearchedEvent: Observable<StopSearched> = Observable.create<StopSearched> { emitter ->
        stopListAdapter.stopSearchedListener = { stopId ->
            emitter.onNext(StopListUiEvent.StopSearched(stopId))
        }
    }.share()

    val stopSelectedEvent: Observable<StopSelectedFromDrawer> = Observable.create<StopSelectedFromDrawer> { emitter ->
        stopListAdapter.stopSelectedListener = { stop ->
            emitter.onNext(StopListUiEvent.StopSelectedFromDrawer(stop))
        }
    }.share()

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
