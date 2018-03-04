package com.philsoft.metrotripper.app.nextrip

import android.support.v7.widget.RecyclerView
import android.view.View
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.app.nextrip.constants.Direction
import com.philsoft.metrotripper.model.Trip
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.trip_item.*

class TripViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
    fun render(trip: Trip) {
        timeUnit.visibility = View.VISIBLE
        route.text = trip.route + trip.terminal
        description.text = trip.description
        routeDirection.setImageResource(getTripDirectionResource(trip))

        val timeAndText = trip.departureText.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        timeNumber.text = timeAndText[0]
        if (timeAndText.size > 1) {
            timeUnit.setText(R.string.minutes)
        } else {
            timeUnit.visibility = View.GONE
        }
    }

    private fun getTripDirectionResource(trip: Trip): Int {
        val direction = Direction.valueOf(trip.routeDirection)
        when (direction) {
            Direction.NORTHBOUND -> return R.drawable.ic_up_arrow
            Direction.SOUTHBOUND -> return R.drawable.ic_down_arrow
            Direction.EASTBOUND -> return R.drawable.ic_right_arrow
            Direction.WESTBOUND -> return R.drawable.ic_left_arrow
            else -> return 0
        }
    }

    private fun setColors(mainColorResId: Int, topLineColorResId: Int, bottomLineColorResId: Int) {
        mainLayout.setBackgroundResource(mainColorResId)
        lineTop.setBackgroundResource(topLineColorResId)
        lineBottom.setBackgroundResource(bottomLineColorResId)
    }
}