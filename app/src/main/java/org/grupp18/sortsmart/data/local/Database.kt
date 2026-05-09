package org.grupp18.sortsmart.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.grupp18.sortsmart.data.local.dao.StationDao
import org.grupp18.sortsmart.data.local.entity.CacheMetadataEntity
import org.grupp18.sortsmart.data.local.entity.CategoryEntity
import org.grupp18.sortsmart.data.local.entity.StationCategoryEntity
import org.grupp18.sortsmart.data.local.entity.StationMarkerEntity

@Database(
    entities = [
        CategoryEntity::class,
        StationMarkerEntity::class,
        StationCategoryEntity::class,
        CacheMetadataEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SortSmartDatabase : RoomDatabase() {

    abstract fun stationDao(): StationDao

    companion object {
        @Volatile
        private var INSTANCE: SortSmartDatabase? = null

        fun getInstance(context: Context): SortSmartDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SortSmartDatabase::class.java,
                    "sortsmart.db"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}