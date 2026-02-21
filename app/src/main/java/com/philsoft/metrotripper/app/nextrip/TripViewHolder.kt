package com.philsoft.metrotripper.app.nextrip

import android.support.v7.widget.RecyclerView
import android.view.View
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.app.nextrip.constants.Direction
import com.philsoft.metrotripper.databinding.TripItemBinding
import com.philsoft.metrotripper.model.Trip

class TripViewHolder(val binding: TripItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun render(trip: Trip) {
        binding.timeUnit.visibility = View.VISIBLE
        binding.route.text = trip.route + trip.terminal
        binding.description.text = trip.description
        binding.routeDirection.setImageResource(getTripDirectionResource(trip))

        val timeAndText = trip.departureText.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        binding.timeNumber.text = timeAndText[0]
        if (timeAndText.size > 1) {
            binding.timeUnit.setText(R.string.minutes)
        } else {
            binding.timeUnit.visibility = View.GONE
        }
    }

    private fun getTripDirectionResource(trip: Trip): Int {
        val direction = Direction.valueOf(trip.routeDirection)
        return when (direction) {
            Direction.NORTHBOUND -> R.drawable.ic_up_arrow
            Direction.SOUTHBOUND -> R.drawable.ic_down_arrow
            Direction.EASTBOUND -> R.drawable.ic_right_arrow
            Direction.WESTBOUND -> R.drawable.ic_left_arrow
            else -> 0
        }
    }

    private fun setColors(mainColorResId: Int, topLineColorResId: Int, bottomLineColorResId: Int) {
        binding.mainLayout.setBackgroundResource(mainColorResId)
        binding.lineTop.setBackgroundResource(topLineColorResId)
        binding.lineBottom.setBackgroundResource(bottomLineColorResId)
    }
}

