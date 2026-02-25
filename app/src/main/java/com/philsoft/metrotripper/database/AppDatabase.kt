package com.philsoft.metrotripper.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.philsoft.metrotripper.database.dao.StopDao
import com.philsoft.metrotripper.model.Stop

@Database(entities = [Stop::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stopDao(): StopDao
}
