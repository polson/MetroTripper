package com.philsoft.metrotripper.app.ui

import android.app.Activity
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.google.common.collect.Lists
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.app.SelectedStopProvider
import com.philsoft.metrotripper.app.SettingsProvider
import com.philsoft.metrotripper.app.nextrip.NexTripService
import com.philsoft.metrotripper.app.nextrip.TripAdapter
import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.model.Trip
import com.philsoft.metrotripper.utils.ui.SimplePanelSlideListener
import com.philsoft.metrotripper.utils.ui.Ui
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class StopInfoHelper(private val activity: Activity,
                     private val stopProvider: SelectedStopProvider,
                     private val panel: SlidingUpPanelLayout,
                     private val mapHelper: MapHelper,
                     private val settingsProvider: SettingsProvider,
                     private val tripAdapter: TripAdapter) {

    private var hasTripInfo: Boolean = false

    init {
        setupView()
    }

    private fun setupView() {
        val getTimesButton = Ui.findView<View>(activity, R.id.scheduleButtonWrapper)
        val getLocationButton = Ui.findView<ImageView>(activity, R.id.locationButton)
        val saveStopButton = Ui.findView<ImageView>(activity, R.id.saveButton)
        panel.addPanelSlideListener(object : SimplePanelSlideListener() {

            override fun onPanelStateChanged(panel: View, previousState: SlidingUpPanelLayout.PanelState, newState: SlidingUpPanelLayout.PanelState) {
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    fetchTripInfo(stopProvider.selectedStop)
                } else if (newState == SlidingUpPanelLayout.PanelState.ANCHORED) {
                    fetchTripInfo(stopProvider.selectedStop)
                }
            }
        })

        // Setup listeners
        getTimesButton.setOnClickListener {
            fetchTripInfo(stopProvider.selectedStop)
            panel.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        }
        getLocationButton.setOnClickListener {
            panel.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            mapHelper.centerCameraOnLatLng(stopProvider.selectedStop.latLng, true)
        }
        saveStopButton.setOnClickListener {
            if (settingsProvider.isStopSaved(stopProvider.selectedStop.stopId)) {
                settingsProvider.unsaveStop(stopProvider.selectedStop.stopId)
                saveStopButton.colorFilter = null
            } else {
                settingsProvider.saveStop(stopProvider.selectedStop.stopId)
                saveStopButton.setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY)
            }
        }
    }

    private fun fetchTripInfo(stop: Stop) {
        showProgressSpinner()
        NexTripService.create().getTrips(stop.stopId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { trips -> onNexTripLoadComplete(Lists.newArrayList(trips)) }
                .doOnError { throwable -> onNexTripLoadFailed(throwable.message) }
                .subscribe()
    }

    private fun showProgressSpinner() {
        Ui.findView<View>(activity, R.id.progressSpinner).visibility = View.VISIBLE
        Ui.findView<View>(activity, R.id.scheduleButton).visibility = View.GONE
    }

    private fun hideProgressSpinner() {
        Ui.findView<View>(activity, R.id.progressSpinner).visibility = View.GONE
        Ui.findView<View>(activity, R.id.scheduleButton).visibility = View.VISIBLE
    }

    fun showStopInfo(stop: Stop) {
        hideProgressSpinner()
        Ui.setText(activity, R.id.route, "Stop " + stop.stopId)
        Ui.setText(activity, R.id.description, stop.stopName.toString())
        clearTrips()
        val saveStopButton = Ui.findView<ImageView>(activity, R.id.saveButton)
        if (stopProvider.selectedStop != null && settingsProvider.isStopSaved(stop.stopId)) {
            saveStopButton.setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY)
        } else {
            saveStopButton.colorFilter = null
        }
    }

    private fun clearTrips() {
        Timber.d("Clearing trips!")
        (activity.tripList.adapter as TripAdapter).clear()
    }

    fun onNexTripLoadComplete(trips: List<Trip>) {
        hideProgressSpinner()
        tripAdapter.setTrips(trips)
        hasTripInfo = true
    }

    fun onNexTripLoadFailed(message: String?) {
        Timber.d("Failed to get trips: " + message)
    }

    fun saveState(outState: Bundle) {
        if (stopProvider.selectedStop != null) {
            outState.putBoolean(KEY_HAS_TRIP_INFO, hasTripInfo)
        }
    }

    fun restoreState(savedState: Bundle) {
        if (stopProvider.selectedStop != null) {
            val hasTripInfo = savedState.getBoolean(KEY_HAS_TRIP_INFO)
            if (hasTripInfo) {
                // Only fetch trip info if it was fetched before config change
                fetchTripInfo(stopProvider.selectedStop)
            }
        }
    }

    companion object {

        private val KEY_HAS_TRIP_INFO = "KEY_HAS_TRIP_INFO"
    }
}
