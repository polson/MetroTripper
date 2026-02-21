package com.philsoft.metrotripper.app.nextrip

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.philsoft.metrotripper.databinding.TripItemBinding
import com.philsoft.metrotripper.model.Trip

class TripAdapter : RecyclerView.Adapter<TripViewHolder>() {

    private val trips = ArrayList<Trip>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = TripItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TripViewHolder(binding)
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
