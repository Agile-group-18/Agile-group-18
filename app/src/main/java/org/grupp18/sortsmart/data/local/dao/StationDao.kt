package org.grupp18.sortsmart.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.grupp18.sortsmart.data.local.entity.CacheMetadataEntity
import org.grupp18.sortsmart.data.local.entity.CategoryEntity
import org.grupp18.sortsmart.data.local.entity.StationCategoryEntity
import org.grupp18.sortsmart.data.local.entity.StationMarkerEntity
import org.grupp18.sortsmart.data.local.relation.StationMarkerWithCategories

@Dao
interface StationDao {

    @Query("SELECT * FROM categories ORDER BY id")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY id")
    suspend fun getCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id IN (:ids)")
    suspend fun getCategoriesByIds(ids: List<Int>): List<CategoryEntity>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun categoryCount(): Int

    @Upsert
    suspend fun upsertCategories(categories: List<CategoryEntity>)


    @Query("SELECT * FROM station_markers ORDER BY id")
    fun observeAllMarkers(): Flow<List<StationMarkerEntity>>

    @Query("SELECT * FROM station_markers ORDER BY id")
    suspend fun getAllMarkers(): List<StationMarkerEntity>

    @Query("SELECT COUNT(*) FROM station_markers")
    suspend fun markerCount(): Int

    @Upsert
    suspend fun upsertMarkers(markers: List<StationMarkerEntity>)

    @Query("DELETE FROM station_markers")
    suspend fun deleteAllMarkers()

    @Upsert
    suspend fun upsertStationCategories(categories: List<StationCategoryEntity>)

    @Query("DELETE FROM station_categories")
    suspend fun deleteAllStationCategories()

    @Query("DELETE FROM station_categories WHERE station_id = :stationId")
    suspend fun deleteCategoriesForStation(stationId: String)

    @Transaction
    @Query("SELECT * FROM station_markers WHERE id = :stationId")
    suspend fun getMarkerWithCategories(stationId: String): StationMarkerWithCategories?

    @Query(
        """
        SELECT DISTINCT sm.*
        FROM station_markers sm
        INNER JOIN station_categories sc
            ON sm.id = sc.station_id
        WHERE sc.category_id IN (:categoryIds)
        ORDER BY sm.id
        """
    )
    suspend fun getMarkersMatchingAnyCategory(
        categoryIds: List<Int>
    ): List<StationMarkerEntity>

    @Query(
        """
        SELECT DISTINCT sm.*
        FROM station_markers sm
        INNER JOIN station_categories sc
            ON sm.id = sc.station_id
        WHERE sc.category_id IN (:categoryIds)
            AND sm.has_problem_report = 1
        ORDER BY sm.id
        """
    )
    suspend fun getProblemMarkersMatchingAnyCategory(
        categoryIds: List<Int>
    ): List<StationMarkerEntity>


    @Query(
        """
        SELECT sm.*
        FROM station_markers sm
        INNER JOIN station_categories sc
            ON sm.id = sc.station_id
        WHERE sc.category_id IN (:categoryIds)
        GROUP BY sm.id
        HAVING COUNT(DISTINCT sc.category_id) = :selectedCount
        ORDER BY sm.id
        """
    )
    suspend fun getMarkersMatchingAllCategories(
        categoryIds: List<Int>,
        selectedCount: Int
    ): List<StationMarkerEntity>

    @Query(
        """
        SELECT sm.*
        FROM station_markers sm
        INNER JOIN station_categories sc
            ON sm.id = sc.station_id
        WHERE sc.category_id IN (:categoryIds)
            AND sm.has_problem_report = 1
        GROUP BY sm.id
        HAVING COUNT(DISTINCT sc.category_id) = :selectedCount
        ORDER BY sm.id
        """
    )
    suspend fun getProblemMarkersMatchingAllCategories(
        categoryIds: List<Int>,
        selectedCount: Int
    ): List<StationMarkerEntity>

    @Query(
        """
        SELECT *
        FROM station_markers
        WHERE has_problem_report = 1
        ORDER BY id
        """
    )
    suspend fun getProblemMarkers(): List<StationMarkerEntity>


    @Query("SELECT * FROM cache_metadata WHERE cache_key = :key")
    suspend fun getCacheMetadata(key: String): CacheMetadataEntity?

    @Upsert
    suspend fun upsertCacheMetadata(metadata: CacheMetadataEntity)

    @Transaction
    suspend fun replaceAllStations(
        markers: List<StationMarkerEntity>,
        stationCategories: List<StationCategoryEntity>,
        syncedAtMillis: Long
    ) {
        deleteAllStationCategories()
        deleteAllMarkers()
        upsertMarkers(markers)
        upsertStationCategories(stationCategories)
        upsertCacheMetadata(
            CacheMetadataEntity(
                key = CacheMetadataEntity.STATIONS_LAST_SYNC,
                value = syncedAtMillis.toString(),
                updatedAtMillis = syncedAtMillis
            )
        )
    }

    @Transaction
    suspend fun replaceCategories(
        categories: List<CategoryEntity>,
        syncedAtMillis: Long
    ) {
        upsertCategories(categories)
        upsertCacheMetadata(
            CacheMetadataEntity(
                key = CacheMetadataEntity.CATEGORIES_LAST_SYNC,
                value = syncedAtMillis.toString(),
                updatedAtMillis = syncedAtMillis
            )
        )
    }

    @Transaction
    suspend fun replaceEverything(
        categories: List<CategoryEntity>,
        markers: List<StationMarkerEntity>,
        stationCategories: List<StationCategoryEntity>,
        syncedAtMillis: Long
    ) {
        replaceCategories(
            categories = categories,
            syncedAtMillis = syncedAtMillis
        )

        replaceAllStations(
            markers = markers,
            stationCategories = stationCategories,
            syncedAtMillis = syncedAtMillis
        )
    }

    @Transaction
    @Query("SELECT * FROM station_markers ORDER BY id")
    suspend fun getAllMarkersWithCategories(): List<StationMarkerWithCategories>
}