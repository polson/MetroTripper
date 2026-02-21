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
        val records = parser.records

        val startTime = System.currentTimeMillis()
        insertStopsFast(records)
        val elapsed = System.currentTimeMillis() - startTime
        Timber.d("Inserted ${records.size} stops in $elapsed ms")
    }

    private fun insertStopsFast(records: List<CSVRecord>) {
        val stops = records.map { record ->
            Stop(
                stopId = record.get("stop_id").toLong(),
                stopName = record.get("stop_name"),
                stopDesc = record.get("stop_desc"),
                stopLat = record.get("stop_lat").toDouble(),
                stopLon = record.get("stop_lon").toDouble(),
                wheelchairBoarding = record.get("wheelchair_boarding").toInt(),
                stopUrl = record.get("stop_url")
            )
        }
        stopDao.insertAll(stops)
    }

    fun isTableEmpty(): Boolean {
        return stopDao.isTableEmpty()
    }
}
