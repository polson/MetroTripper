package com.philsoft.metrotripper.app.drawer

import android.support.v7.widget.RecyclerView
import android.view.View
import com.jakewharton.rxbinding2.view.clicks
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.databinding.StopDrawerItemBinding
import com.philsoft.metrotripper.model.Stop
import io.reactivex.Observable

class StopViewHolder(val binding: StopDrawerItemBinding) : RecyclerView.ViewHolder(binding.root) {
    val clickEvent: Observable<Int> = binding.root.clicks().map { adapterPosition }

    fun render(stop: Stop, isStopSelected: Boolean) {
        binding.header.text = stop.stopId.toString()
        binding.text.text = stop.stopName
        if (isStopSelected) {
            binding.root.setBackgroundResource(R.color.dark_blue)
        } else {
            binding.root.setBackgroundResource(R.color.sidebar_bg)
        }
        clickEvent.map { stop.stopId }
    }
}

