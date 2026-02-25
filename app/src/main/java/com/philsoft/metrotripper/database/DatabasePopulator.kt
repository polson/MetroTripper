package com.philsoft.metrotripper.database

import com.philsoft.metrotripper.database.dao.StopDao
import com.philsoft.metrotripper.model.Stop
import org.apache.commons.csv.CSVFormat
import timber.log.Timber
import java.io.InputStream
import java.io.InputStreamReader

class DatabasePopulator(private val stopDao: StopDao) {
    val isTableEmpty: Boolean
        get() = stopDao.isTableEmpty()

    fun populateStops(inputStream: InputStream) {
        stopDao.deleteAll()

        val parser = CSVFormat.RFC4180.builder().setHeader().build().parse(InputStreamReader(inputStream))
        
        val startTime = System.currentTimeMillis()
        val records = parser.iterator()
        val batch = mutableListOf<Stop>()
        var count = 0
        while (records.hasNext()) {
            val record = records.next()
            batch.add(Stop(
                stopId = record.get("stop_id").toLong(),
                stopName = record.get("stop_name"),
                stopDesc = record.get("stop_desc"),
                stopLat = record.get("stop_lat").toDouble(),
                stopLon = record.get("stop_lon").toDouble(),
                wheelchairBoarding = record.get("wheelchair_boarding").toInt(),
                stopUrl = record.get("stop_url")
            ))
            if (batch.size >= 1000) {
                stopDao.insertAll(batch)
                count += batch.size
                batch.clear()
            }
        }
        if (batch.isNotEmpty()) {
            stopDao.insertAll(batch)
            count += batch.size
        }
        val elapsed = System.currentTimeMillis() - startTime
        Timber.d("Inserted $count stops in $elapsed ms")
    }

    companion object {
        const val CURRENT_VERSION = 2
    }
}
