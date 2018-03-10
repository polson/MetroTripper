package com.philsoft.metrotripper.app

import com.google.common.collect.Sets
import com.philsoft.metrotripper.prefs.Prefs

class SettingsProvider(val prefs: Prefs) {
    var savedStopIds = Sets.newLinkedHashSet<Long>()
        private set
    private var listeners: MutableSet<SettingsListener> = Sets.newHashSet()

    interface SettingsListener {
        fun onStopSaved(stopId: Long)

        fun onStopUnsaved(stopId: Long)
    }

    fun addListener(listener: SettingsListener) {
        listeners.add(listener)
    }

    private fun notifyStopSaved(stopId: Long) {
        for (listener in listeners) {
            listener.onStopSaved(stopId)
        }
    }

    private fun notifyStopUnsaved(stopId: Long) {
        for (listener in listeners) {
            listener.onStopUnsaved(stopId)
        }
    }

    fun saveStop(stopId: Long) {
        savedStopIds.add(stopId)
        prefs.saveStopId(stopId)
        notifyStopSaved(stopId)
    }

    fun unsaveStop(stopId: Long) {
        savedStopIds.remove(stopId)
        prefs.removeStopId(stopId)
        notifyStopUnsaved(stopId)
    }

    fun isStopSaved(stopId: Long): Boolean {
        return savedStopIds.contains(stopId)
    }

    companion object {


        val TAG = "TAG_SETTINGS_PROVIDER"
    }
}

