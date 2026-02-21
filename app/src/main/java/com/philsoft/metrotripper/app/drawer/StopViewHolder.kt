package com.philsoft.metrotripper.app.drawer

import androidx.recyclerview.widget.RecyclerView
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.databinding.StopDrawerItemBinding
import com.philsoft.metrotripper.model.Stop

class StopViewHolder(val binding: StopDrawerItemBinding) : RecyclerView.ViewHolder(binding.root) {
    var onClickListener: (Int) -> Unit = {}

    init {
        binding.root.setOnClickListener {
            onClickListener(adapterPosition)
        }
    }

    fun render(stop: Stop, isStopSelected: Boolean) {
        binding.header.text = stop.stopId.toString()
        binding.text.text = stop.stopName
        if (isStopSelected) {
            binding.root.setBackgroundResource(R.color.dark_blue)
        } else {
            binding.root.setBackgroundResource(R.color.sidebar_bg)
        }
    }
}

