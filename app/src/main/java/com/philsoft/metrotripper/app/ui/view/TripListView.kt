package com.philsoft.metrotripper.app.ui.view

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MotionEvent
import com.philsoft.metrotripper.app.nextrip.TripAdapter
import com.philsoft.metrotripper.app.state.AppState

class TripListView(rv: RecyclerView) {

    val tripAdapter = TripAdapter()

    init {
        rv.layoutManager = LinearLayoutManager(rv.context)
        rv.adapter = tripAdapter
        // Fix issues with touch events when recyclerview is used inside sliding panel
        rv.setOnTouchListener { v, event ->
            val action = event.action
            when (action) {
                MotionEvent.ACTION_DOWN ->
                    // Disallow ScrollView to intercept touch events.
                    v.parent.requestDisallowInterceptTouchEvent(true)

                MotionEvent.ACTION_UP ->
                    // Allow ScrollView to intercept touch events.
                    v.parent.requestDisallowInterceptTouchEvent(false)
            }
            v.onTouchEvent(event)
            true
        }
    }

    fun render(state: AppState) {
        tripAdapter.setTrips(state.currentTrips)
    }
}
