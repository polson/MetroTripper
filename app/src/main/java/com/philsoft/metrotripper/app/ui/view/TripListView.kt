package com.philsoft.metrotripper.app.ui.view

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import com.philsoft.metrotripper.app.nextrip.TripAdapter
import com.philsoft.metrotripper.app.state.TripListAction
import com.philsoft.metrotripper.model.Trip

class TripListView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RecyclerView(context, attrs, defStyleAttr) {

    private val tripAdapter = TripAdapter()

    init {
        layoutManager = LinearLayoutManager(context)
        adapter = tripAdapter
    }

    fun render(action: TripListAction) {
        when (action) {
            is TripListAction.ShowTrips -> showTrips(action.trips)
        }
    }

    private fun showTrips(trips: List<Trip>) {
        tripAdapter.setTrips(trips)
    }
}
