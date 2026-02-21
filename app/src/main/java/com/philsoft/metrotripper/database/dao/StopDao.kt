package com.philsoft.metrotripper.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.philsoft.metrotripper.model.Stop

@Dao
interface StopDao {
    @Query("SELECT * FROM t_stops WHERE stop_id = :stopId LIMIT 1")
    fun getStopById(stopId: Long): Stop?

    @Query("SELECT * FROM t_stops WHERE stop_id IN (:stopIds)")
    fun getStopsById(stopIds: List<Long>): List<Stop>

    @Query("SELECT * FROM t_stops ORDER BY (ABS(stop_lat - :lat) + ABS(stop_lon - :lon)) LIMIT :limit")
    fun getClosestStops(lat: Double, lon: Double, limit: Int): List<Stop>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(stops: List<Stop>)

    @Query("SELECT count(*) == 0 FROM t_stops")
    fun isTableEmpty(): Boolean

    @Query("DELETE FROM t_stops")
    fun deleteAll()
}
