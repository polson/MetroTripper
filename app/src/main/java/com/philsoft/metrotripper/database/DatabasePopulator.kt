package com.philsoft.metrotripper.database

import com.philsoft.metrotripper.database.dao.StopDao
import com.philsoft.metrotripper.model.Stop
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import timber.log.Timber
import java.io.InputStream
import java.io.InputStreamReader

class DatabasePopulator(private val stopDao: StopDao) {
    val isTableEmpty: Boolean
        get() = stopDao.isTableEmpty()

    fun populateStops(inputStream: InputStream) {
        stopDao.deleteAll()

        val parser =
            CSVFormat.RFC4180.builder().setHeader().build().parse(InputStreamReader(inputStream))

        val startTime = System.currentTimeMillis()
        val records = parser.iterator()
        val batch = mutableListOf<Stop>()
        var count = 0
        fun flushBatch() {
            if (batch.isNotEmpty()) {
                stopDao.insertAll(batch)
                count += batch.size
                batch.clear()
            }
        }
        while (records.hasNext()) {
            val record = records.next()
            batch += record.toStop()
            if (batch.size >= 1000) {
                flushBatch()
            }
        }
        flushBatch()
        val elapsed = System.currentTimeMillis() - startTime
        Timber.d("Inserted $count stops in $elapsed ms")
    }

    private fun CSVRecord.toStop(): Stop =
        Stop(
            stopId = get("stop_id").toLong(),
            stopName = get("stop_name"),
            stopDesc = get("stop_desc"),
            stopLat = get("stop_lat").toDouble(),
            stopLon = get("stop_lon").toDouble(),
            wheelchairBoarding = get("wheelchair_boarding").toInt(),
            stopUrl = get("stop_url")
        )

    companion object {
        const val CURRENT_VERSION = 2
    }
}
