package com.philsoft.metrotripper.app.drawer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.philsoft.metrotripper.databinding.SearchDrawerItemBinding
import com.philsoft.metrotripper.databinding.StopDrawerItemBinding
import com.philsoft.metrotripper.model.Stop

class StopListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SEARCH = 0
        private const val VIEW_TYPE_STOP = 1
    }

    private val items = ArrayList<Stop>()
    var selectedStopId: Long = 0L
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var stopSearchedListener: (Long) -> Unit = {}
    var stopSelectedListener: (Stop) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SEARCH -> {
                val binding = SearchDrawerItemBinding.inflate(layoutInflater, parent, false)
                val viewHolder = SearchViewHolder(binding)
                viewHolder.onSearchListener = { stopId -> stopSearchedListener(stopId) }
                viewHolder

            }
            else -> {
                val binding = StopDrawerItemBinding.inflate(layoutInflater, parent, false)
                val viewHolder = StopViewHolder(binding)
                viewHolder.onClickListener = { adapterPosition ->
                    val stop = items[adapterPosition - 1]
                    stopSelectedListener(stop)
                }
                viewHolder
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            VIEW_TYPE_SEARCH
        } else {
            VIEW_TYPE_STOP
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_STOP -> {
                val stop = items[position - 1]
                buildStopRow(holder as StopViewHolder, stop)
            }
        }
    }

    private fun buildStopRow(holder: StopViewHolder, stop: Stop) {
        if (stop != null) {
            val isSelected = stop.stopId == selectedStopId
            holder.render(stop, isSelected)
        }
    }

    override fun getItemCount(): Int {
        return items.size + 1
    }

    fun showStops(stops: List<Stop>) {
        items.clear()
        items.addAll(stops)
        notifyDataSetChanged()
    }

}
