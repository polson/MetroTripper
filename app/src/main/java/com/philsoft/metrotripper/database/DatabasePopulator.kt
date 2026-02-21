package com.philsoft.metrotripper.database

import android.content.Context
import com.philsoft.metrotripper.database.dao.StopDao
import com.philsoft.metrotripper.model.Stop
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import timber.log.Timber
import java.io.InputStreamReader

class DatabasePopulator(private val context: Context) {

    companion object {
        private const val STOPS_FILE = "stops.txt"
    }

    private val stopDao: StopDao = AppDatabase.getInstance(context).stopDao()

    fun populateStopsFast() {
        val assetManager = context.assets
        val inputStream = assetManager.open(STOPS_FILE)
        val parser = CSVParser(InputStreamReader(inputStream), CSVFormat.RFC4180.withHeader())
        
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

    fun isTableEmpty(): Boolean {
        return stopDao.isTableEmpty()
    }
}
