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

    companion object {
        private const val DATABASE_NAME = "metrotripper.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration() // Simpler for this migration if we don't want to handle schema mismatches
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
