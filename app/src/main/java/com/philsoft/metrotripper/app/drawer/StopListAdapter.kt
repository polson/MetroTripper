package com.philsoft.metrotripper.app.drawer

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.utils.inflate


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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        return when (viewType) {
            VIEW_TYPE_SEARCH -> {
                val searchView = parent.inflate(R.layout.search_drawer_item, false)
                val viewHolder = SearchViewHolder(searchView)
                viewHolder.searchEvent
                        .takeUntil(RxView.detaches(parent))
                        .subscribe { stopId -> stopSearchedListener(stopId) }
                viewHolder

            }
            else -> {
                val drawerItemView = parent.inflate(R.layout.stop_drawer_item, false)
                val viewHolder = StopViewHolder(drawerItemView)
                viewHolder.clickEvent
                        .takeUntil(RxView.detaches(parent))
                        .map { adapterPosition -> items[adapterPosition - 1] }
                        .subscribe { stop -> stopSelectedListener(stop) }
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
