package com.philsoft.metrotripper.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng


@Entity(tableName = "t_stops")
data class Stop(
    @PrimaryKey
    @ColumnInfo(name = "stop_id")
    val stopId: Long,

    @ColumnInfo(name = "stop_name")
    val stopName: String,

    @ColumnInfo(name = "stop_desc")
    val stopDesc: String,

    @ColumnInfo(name = "stop_lat")
    val stopLat: Double,

    @ColumnInfo(name = "stop_lon")
    val stopLon: Double,

    @ColumnInfo(name = "wheelchair_boarding")
    val wheelchairBoarding: Int,

    @ColumnInfo(name = "stop_url")
    val stopUrl: String
) {

    @get:Ignore
    val latLng: LatLng
        get() = LatLng(stopLat, stopLon)
}
