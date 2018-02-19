package com.philsoft.metrotripper.database

import android.content.Context
import com.philsoft.metrotripper.database.contracts.StopContract
import com.philsoft.metrotripper.model.Stop
import org.jetbrains.anko.db.rowParser
import org.jetbrains.anko.db.select


class DataProvider(context: Context) {
    private val dbHelper: DatabaseHelper = DatabaseHelper.getInstance(context)

    companion object {
        val stopParser = rowParser { stopId: Long,
                                     stopName: String,
                                     stopDesc: String,
                                     stopLat: Double,
                                     stopLon: Double,
                                     wheelchairBoarding: Int,
                                     stopUrl: String ->
            Stop(stopId, stopName, stopDesc, stopLat, stopLon, wheelchairBoarding, stopUrl)
        }
    }

    fun getStopById(stopId: Long): Stop? {
        return dbHelper.use {
            val stops = select(StopContract.TABLE_NAME)
                    .whereSimple("${StopContract.STOP_ID} = ?", stopId.toString())
                    .parseList(stopParser)
            return@use if (!stops.isEmpty()) stops[0] else null
        }
    }

    fun getClosestStops(lat: Double, lon: Double, max: Int): List<Stop> {
        return dbHelper.use {
            return@use select(StopContract.TABLE_NAME)
                    .orderBy("ABS(${StopContract.STOP_LAT} - $lat) + ABS(${StopContract.STOP_LON} - $lon)")
                    .limit(max)
                    .parseList(stopParser)
        }
    }
}
