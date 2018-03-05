package com.philsoft.metrotripper.app.drawer

import android.support.v7.widget.RecyclerView
import android.view.View
import com.jakewharton.rxbinding2.view.clicks
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.model.Stop
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.stop_drawer_item.*

class StopViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
    val clickEvent = containerView.clicks()

    fun render(stop: Stop, isStopSelected: Boolean) {
        header.text = stop.stopId.toString()
        text.text = stop.stopName
        if (isStopSelected) {
            containerView.setBackgroundResource(R.color.dark_blue)
        } else {
            containerView.setBackgroundResource(R.color.sidebar_bg)
        }
    }
}