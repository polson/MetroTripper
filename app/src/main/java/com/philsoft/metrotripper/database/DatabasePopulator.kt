package com.philsoft.metrotripper.database

import android.content.Context
import com.philsoft.metrotripper.database.DataProvider.Companion.stopParser
import com.philsoft.metrotripper.database.contracts.StopContract
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.transaction
import timber.log.Timber
import java.io.IOException
import java.io.InputStreamReader

class DatabasePopulator(private val context: Context) {

    private val STOPS_FILE = "stops.txt"
    private val dbHelper: DatabaseHelper = DatabaseHelper.getInstance(context)

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

    fun insertStopsFast(records: List<CSVRecord>) {
        dbHelper.use {
            transaction {
                for (record in records) {
                    insert(StopContract.TABLE_NAME,
                            StopContract.STOP_ID to record.get("stop_id"),
                            StopContract.STOP_NAME to record.get("stop_name"),
                            StopContract.STOP_DESC to record.get("stop_desc"),
                            StopContract.STOP_LAT to record.get("stop_lat"),
                            StopContract.STOP_LON to record.get("stop_lon"),
                            StopContract.WHEELCHAIR_BOARDING to record.get("wheelchair_boarding"),
                            StopContract.STOP_URL to record.get("stop_url"))
                }
            }
        }
    }

    fun isTableEmpty(): Boolean {
        return dbHelper.use {
            return@use select(StopContract.TABLE_NAME)
                    .limit(1)
                    .parseList(stopParser).isEmpty()
        }
    }
}
