package org.grupp18.sortsmart.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "station_markers",
    indices = [
        Index(value = ["latitude", "longitude"]),
        Index(value = ["has_problem_report"]),
        Index(value = ["station_type"])
    ]
)
data class StationMarkerEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "station_type")
    val stationType: String,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "has_problem_report")
    val hasProblemReport: Boolean,

    @ColumnInfo(name = "synced_at_millis")
    val syncedAtMillis: Long
)