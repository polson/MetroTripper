package com.philsoft.metrotripper.repository

import com.philsoft.metrotripper.database.dao.StopDao
import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.prefs.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StopsRepository(private val stopDao: StopDao, private val prefs: Prefs) {

    suspend fun getStopById(stopId: Long): Stop? = withContext(Dispatchers.IO) {
        stopDao.getStopById(stopId)
    }

    suspend fun getStopsById(stopIds: Collection<Long>): List<Stop> = withContext(Dispatchers.IO) {
        if (stopIds.isEmpty()) emptyList() else stopDao.getStopsById(stopIds.toList())
    }

    suspend fun getClosestStops(lat: Double, lon: Double, max: Int): List<Stop> = withContext(Dispatchers.IO) {
        stopDao.getClosestStops(lat, lon, max)
    }

    suspend fun getSavedStopIds(): Set<Long> = prefs.getSavedStopIds()

    suspend fun isStopSaved(stopId: Long): Boolean = prefs.getSavedStopIds().contains(stopId)

    suspend fun saveStop(stopId: Long) = prefs.saveStopId(stopId)

    suspend fun unsaveStop(stopId: Long) = prefs.removeStopId(stopId)
}
