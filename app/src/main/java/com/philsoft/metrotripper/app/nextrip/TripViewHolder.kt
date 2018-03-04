package com.philsoft.metrotripper.app.nextrip

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.constants.Direction
import com.philsoft.metrotripper.model.Trip
import com.philsoft.metrotripper.utils.ui.Ui

//TODO: use kotlin android extensions
class TripViewHolder(root: View) : RecyclerView.ViewHolder(root) {
    var route: TextView = Ui.findView(root, R.id.trip_item_route)
    var description: TextView = Ui.findView(root, R.id.trip_item_description)
    var timeNumber: TextView = Ui.findView(root, R.id.trip_item_time_number)
    var timeUnit: TextView = Ui.findView(root, R.id.trip_item_time_unit)
    var topLine: View = Ui.findView(root, R.id.trip_item_line_top)
    var bottomLine: View = Ui.findView(root, R.id.trip_item_line_bottom)
    var mainLayout: View = Ui.findView(root, R.id.trip_item_main_layout)
    var tripDirection: ImageView = Ui.findView(root, R.id.trip_item_route_direction)

    fun render(trip: Trip) {
        timeUnit.setVisibility(View.VISIBLE)
        route.setText(trip.route + trip.terminal)
        description.setText(trip.description)
        tripDirection.setImageResource(getTripDirectionResource(trip))

        val timeAndText = trip.departureText.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        timeNumber.setText(timeAndText[0])
        if (timeAndText.size > 1) {
            timeUnit.setText(R.string.minutes)
        } else {
            timeUnit.setVisibility(View.GONE)
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

    private fun setColors(holder: TripViewHolder, mainColorResId: Int, topLineColorResId: Int, bottomLineColorResId: Int) {
        holder.mainLayout.setBackgroundResource(mainColorResId)
        holder.topLine.setBackgroundResource(topLineColorResId)
        holder.bottomLine.setBackgroundResource(bottomLineColorResId)
    }
}