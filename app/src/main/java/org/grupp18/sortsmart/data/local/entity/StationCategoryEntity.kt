package org.grupp18.sortsmart.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "station_categories",
    primaryKeys = ["station_id", "category_id"],
    foreignKeys = [
        ForeignKey(
            entity = StationMarkerEntity::class,
            parentColumns = ["id"],
            childColumns = ["station_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["station_id"]),
        Index(value = ["category_id"]),
        Index(value = ["status"])
    ]
)
data class StationCategoryEntity(
    @ColumnInfo(name = "station_id")
    val stationId: String,

    @ColumnInfo(name = "category_id")
    val categoryId: Int,

    @ColumnInfo(name = "status")
    val status: String
)