package com.philsoft.metrotripper.app.ui.view

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import com.philsoft.metrotripper.app.nextrip.TripAdapter
import com.philsoft.metrotripper.model.Trip

class TripListView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RecyclerView(context, attrs, defStyleAttr) {

    private val tripAdapter = TripAdapter()

    init {
        layoutManager = LinearLayoutManager(context)
        adapter = tripAdapter
    }

    fun showTrips(trips: List<Trip>) {
        tripAdapter.setTrips(trips)
    }
}
