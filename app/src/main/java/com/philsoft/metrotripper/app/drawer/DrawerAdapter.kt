package com.philsoft.metrotripper.app.drawer

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.google.common.collect.Lists
import com.jakewharton.rxbinding2.view.RxView
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.utils.inflate
import io.reactivex.subjects.PublishSubject


class DrawerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = Lists.newArrayList<Stop>()
    private val stopSelectedSubject = PublishSubject.create<Stop>()
    private val stopSearchedSubject = PublishSubject.create<Long>()
    val stopSelectedEvent = stopSelectedSubject.hide()!!
    val searchStopEvent = stopSearchedSubject.hide()!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        return when (viewType) {
            VIEW_TYPE_SEARCH -> {
                val searchView = parent.inflate(R.layout.search_drawer_item, false)
                val viewHolder = SearchViewHolder(searchView)
                viewHolder.searchEvent
                        .takeUntil(RxView.detaches(parent))
                        .subscribe(stopSearchedSubject)
                viewHolder

            }
            else -> {
                val drawerItemView = parent.inflate(R.layout.stop_drawer_item, false)
                val viewHolder = StopViewHolder(drawerItemView)
                viewHolder.clickEvent
                        .takeUntil(RxView.detaches(parent))
                        .map { adapterPosition -> items[adapterPosition - 1] }
                        .subscribe(stopSelectedSubject)
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
            holder.render(stop, false)
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

    companion object {
        private val VIEW_TYPE_SEARCH = 0
        private val VIEW_TYPE_STOP = 1
    }
}