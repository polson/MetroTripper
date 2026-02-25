package com.philsoft.metrotripper.repository

import com.philsoft.metrotripper.database.dao.StopDao
import com.philsoft.metrotripper.model.Stop
import com.philsoft.metrotripper.prefs.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import android.content.Context
import com.philsoft.metrotripper.database.DatabasePopulator
import timber.log.Timber

class StopsRepository(private val context: Context, private val stopDao: StopDao, private val prefs: Prefs) {

    suspend fun populateStopsIfNeeded() = withContext(Dispatchers.IO) {
        try {
            val currentVersion = DatabasePopulator.CURRENT_VERSION
            val stopsVersion = prefs.getStopsVersion()
            val populator = DatabasePopulator(stopDao)
            if (populator.isTableEmpty || stopsVersion < currentVersion) {
                Timber.d("Populating stops... current version: $stopsVersion, target version: $currentVersion")
                val stopData = context.assets.open("stops.txt")
                stopData.use { inputStream ->
                    populator.populateStops(inputStream)
                }
                prefs.setStopsVersion(currentVersion)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error populating stops")
        }
    }

    suspend fun getStopById(stopId: Long): Stop? = withContext(Dispatchers.IO) {
        stopDao.getStopById(stopId)
    }

    suspend fun getStopsById(stopIds: Collection<Long>): List<Stop> = withContext(Dispatchers.IO) {
        if (stopIds.isEmpty()) emptyList() else stopDao.getStopsById(stopIds.toList())
    }

    suspend fun getClosestStops(lat: Double, lon: Double, max: Int): List<Stop> = withContext(Dispatchers.IO) {
        stopDao.getClosestStops(lat, lon, max)
    }

    suspend fun getSavedStopsMap(): Map<Long, Stop> = withContext(Dispatchers.IO) {
        val savedStopIds = getSavedStopIds()
        val savedStopsList = getStopsById(savedStopIds)
        savedStopsList.associateBy { it.stopId }
    }

    suspend fun getSavedStopIds(): Set<Long> = prefs.getSavedStopIds()

    suspend fun isStopSaved(stopId: Long): Boolean = prefs.getSavedStopIds().contains(stopId)

    suspend fun saveStop(stopId: Long) = prefs.saveStopId(stopId)

    suspend fun unsaveStop(stopId: Long) = prefs.removeStopId(stopId)
}
