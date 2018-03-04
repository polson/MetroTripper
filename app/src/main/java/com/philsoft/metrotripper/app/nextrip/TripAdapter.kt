package com.philsoft.metrotripper.app.nextrip

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.common.collect.Lists
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.model.Trip

class TripAdapter : RecyclerView.Adapter<TripViewHolder>() {

    private val trips = Lists.newArrayList<Trip>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val drawerItem = LayoutInflater.from(parent.context).inflate(R.layout.trip_item, parent, false)
        return TripViewHolder(drawerItem)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.render(trips[position])
    }

    fun setTrips(updatedTrips: List<Trip>) {
        trips.clear()
        trips.addAll(updatedTrips)
        notifyDataSetChanged()
    }

    fun clear() {
        trips.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return trips.size
    }
}