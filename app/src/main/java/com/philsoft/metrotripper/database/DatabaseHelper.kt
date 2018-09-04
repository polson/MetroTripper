package com.philsoft.metrotripper.database

import android.annotation.TargetApi
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.philsoft.metrotripper.database.contracts.StopContract
import org.jetbrains.anko.db.*
import timber.log.Timber

class DatabaseHelper private constructor(context: Context) : ManagedSQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "metrotripper.db"
        const val DATABASE_VERSION = 1

        private var instance: DatabaseHelper? = null
        @Synchronized
        @JvmStatic
        fun getInstance(ctx: Context): DatabaseHelper {
            if (instance == null) {
                instance = DatabaseHelper(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        Timber.d("Creating tables in database: $DATABASE_NAME")
        db.createTable(StopContract.TABLE_NAME, true,
                StopContract.STOP_ID to INTEGER + PRIMARY_KEY + NOT_NULL,
                StopContract.STOP_NAME to TEXT,
                StopContract.STOP_DESC to TEXT,
                StopContract.STOP_LAT to REAL,
                StopContract.STOP_LON to REAL,
                StopContract.WHEELCHAIR_BOARDING to INTEGER,
                StopContract.STOP_URL to TEXT
        )
        Timber.d("Creating indexes in database: $DATABASE_NAME")
        db.createIndex("S1", StopContract.TABLE_NAME, false, false, StopContract.STOP_ID)
    }

    @TargetApi(16)
    override fun onConfigure(db: SQLiteDatabase) {
        setWriteAheadLoggingEnabled(true)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
    }
}
