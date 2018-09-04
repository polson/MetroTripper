package com.philsoft.metrotripper.app

import com.philsoft.metrotripper.prefs.Prefs

class SettingsProvider(private val prefs: Prefs) {
    private val savedStopIds = LinkedHashSet<Long>(prefs.getSavedStopIds())

    fun saveStop(stopId: Long) {
        savedStopIds.add(stopId)
        prefs.saveStopId(stopId)
    }

    fun unsaveStop(stopId: Long) {
        savedStopIds.remove(stopId)
        prefs.removeStopId(stopId)
    }

    fun getSavedStopIds() = prefs.getSavedStopIds()

    fun isStopSaved(stopId: Long): Boolean {
        return savedStopIds.contains(stopId)
    }
}

