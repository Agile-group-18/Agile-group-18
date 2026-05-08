package org.grupp18.sortsmart.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cache_metadata")
data class CacheMetadataEntity(
    @PrimaryKey
    @ColumnInfo(name = "cache_key")
    val key: String,

    @ColumnInfo(name = "cache_value")
    val value: String,

    @ColumnInfo(name = "updated_at_millis")
    val updatedAtMillis: Long
) {
    companion object {
        const val CATEGORIES_LAST_SYNC = "categories_last_sync"
        const val STATIONS_LAST_SYNC = "stations_last_sync"
    }
}