package com.philsoft.metrotripper.database

import android.content.Context
import com.philsoft.metrotripper.database.dao.StopDao
import com.philsoft.metrotripper.model.Stop


class DataProvider(context: Context) {
    private val stopDao: StopDao = AppDatabase.getInstance(context).stopDao()

    fun getStopById(stopId: Long): Stop? {
        return stopDao.getStopById(stopId)
    }

    fun getStopsById(stopIds: Collection<Long>): List<Stop> {
        if (stopIds.isEmpty()) {
            return emptyList()
        }
        return stopDao.getStopsById(stopIds.toList())
    }

    fun getClosestStops(lat: Double, lon: Double, max: Int): List<Stop> {
        return stopDao.getClosestStops(lat, lon, max)
    }
}
