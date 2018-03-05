package com.philsoft.metrotripper.app.drawer

import android.app.Activity
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.google.common.collect.Lists
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.app.SelectedStopProvider
import com.philsoft.metrotripper.app.SettingsProvider
import com.philsoft.metrotripper.app.ui.MapHelper
import com.philsoft.metrotripper.database.DataProvider
import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.utils.EZ
import com.philsoft.metrotripper.utils.inflate
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.search_drawer_item.*
import org.apache.commons.lang.StringUtils

class DrawerAdapter(private val activity: Activity,
                    private val stopProvider: SelectedStopProvider,
                    private val drawer: DrawerLayout,
                    private val dataProvider: DataProvider,
                    private val mapHelper: MapHelper,
                    private val settingsProvider: SettingsProvider,
                    private val panel: SlidingUpPanelLayout) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<Long> = Lists.newArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        return when (viewType) {
            VIEW_TYPE_SEARCH -> {
                val drawerItemView = parent.inflate(R.layout.search_drawer_item, false)
                SearchViewHolder(drawerItemView)
            }
            VIEW_TYPE_STOP -> {
                val drawerItemView = parent.inflate(R.layout.stop_drawer_item, false)
                StopViewHolder(drawerItemView)
            }
            else -> {
                null
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
            VIEW_TYPE_SEARCH -> buildSearchRow(holder as SearchViewHolder)
            VIEW_TYPE_STOP -> {
                val stopId = items[position - 1]
                buildStopRow(holder as StopViewHolder, stopId)
            }
        }
    }

    private fun buildSearchRow(holder: SearchViewHolder) {
        holder.searchEvent.subscribe { stopId ->
            EZ.hideKeyboard(activity)
            searchStop(stopId, holder.entry)
        }
    }

    private fun searchStop(stopIdStr: String, entry: EditText) {
        if (!StringUtils.isEmpty(stopIdStr) && StringUtils.isNumeric(stopIdStr)) {
            val stopId = java.lang.Long.valueOf(stopIdStr)!!
            val stop = dataProvider.getStopById(stopId)
            if (stop != null) {
                selectStopOnMap(stop, true)
                entry.setText("")
            } else {
                Toast.makeText(activity, R.string.stop_not_found, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buildStopRow(holder: StopViewHolder, stopId: Long) {
        val stop = dataProvider.getStopById(stopId)
        if (stop != null) {
            holder.render(stop, isStopSelected(stopId))
            holder.clickEvent.subscribe {
                selectStopOnMap(stop, true)
            }
        }
    }

    private fun isStopSelected(stopId: Long): Boolean {
        return stopId == stopProvider.selectedStop?.stopId
    }

    fun selectStopOnMap(stop: Stop?, animate: Boolean) {
        drawer.closeDrawers()
        stopProvider.showStop(stop)
        panel.panelState = SlidingUpPanelLayout.PanelState.ANCHORED
        mapHelper.centerCameraOnLatLng(stop!!.latLng, animate)
    }

    override fun getItemCount(): Int {
        return items.size + 1
    }

    fun refresh() {
        items = Lists.newArrayList(settingsProvider.savedStopIds)
        notifyDataSetChanged()
    }

    companion object {

        private val VIEW_TYPE_SEARCH = 0
        private val VIEW_TYPE_STOP = 1
    }
}