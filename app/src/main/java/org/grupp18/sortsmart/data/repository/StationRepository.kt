package org.grupp18.sortsmart.data.repository

import android.content.Context
import org.grupp18.sortsmart.data.api.RetrofitClient
import org.grupp18.sortsmart.data.api.dto.ReportRequestDto
import org.grupp18.sortsmart.data.local.SortSmartDatabase
import org.grupp18.sortsmart.data.local.entity.CacheMetadataEntity
import org.grupp18.sortsmart.data.mapper.toEntity
import org.grupp18.sortsmart.data.mapper.toMarkerEntity
import org.grupp18.sortsmart.data.mapper.toModel
import org.grupp18.sortsmart.data.mapper.toStationCategoryEntities
import org.grupp18.sortsmart.data.model.RecyclingStationDetail
import org.grupp18.sortsmart.data.model.RecyclingStationMarker
import org.grupp18.sortsmart.data.model.WasteCategory

class StationRepository(
    context: Context
) {
    private val stationDao = SortSmartDatabase
        .getInstance(context)
        .stationDao()

    suspend fun getCachedCategories(): List<WasteCategory> {
        return stationDao.getCategories().map { it.toModel() }
    }

    suspend fun getCachedMapMarkers(
        selectedCategoryIds: Set<Int> = emptySet(),
        problemOnly: Boolean = false
    ): List<RecyclingStationMarker> {
        val categoryIds = selectedCategoryIds.toList()

        val entities = when {
            categoryIds.isEmpty() && problemOnly -> {
                stationDao.getProblemMarkers()
            }

            categoryIds.isEmpty() -> {
                stationDao.getAllMarkers()
            }


            problemOnly -> {
                stationDao.getProblemMarkersMatchingAnyCategory(
                    categoryIds = categoryIds
                )
            }

            else -> {
                stationDao.getMarkersMatchingAnyCategory(
                    categoryIds = categoryIds
                )
            }
        }

        return entities.map { it.toModel() }
    }

    suspend fun syncInitialData(): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()

            val categories = RetrofitClient.apiService
                .getCategories()
                .map { it.toEntity(now) }

            val stationsResponse = RetrofitClient.apiService.getMapStations(
                lat = null,
                lon = null,
                radiusKm = null,
                categoryIds = null,
                stationType = null,
                view = "map"
            )

            val markerEntities = stationsResponse.stations.map { station ->
                station.toMarkerEntity(now)
            }

            val stationCategoryEntities = stationsResponse.stations.flatMap { station ->
                station.toStationCategoryEntities()
            }

            stationDao.replaceEverything(
                categories = categories,
                markers = markerEntities,
                stationCategories = stationCategoryEntities,
                syncedAtMillis = now
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun shouldInitialSync(): Boolean {
        return stationDao.markerCount() == 0 ||
                stationDao.categoryCount() == 0
    }

    suspend fun shouldRefreshStations(
        maxAgeMillis: Long = 30 * 60 * 1000L
    ): Boolean {
        val metadata = stationDao.getCacheMetadata(
            CacheMetadataEntity.STATIONS_LAST_SYNC
        ) ?: return true

        val lastSync = metadata.value.toLongOrNull() ?: return true

        return System.currentTimeMillis() - lastSync > maxAgeMillis
    }

    suspend fun getStationDetail(
        stationId: String
    ): Result<RecyclingStationDetail> {
        return try {
            val station = RetrofitClient.apiService.getStationDetail(stationId)
            val categoryIds = station.categories?.map { it.id } ?: emptyList()
            val categoryEntities = stationDao.getCategoriesByIds(categoryIds)

            Result.success(
                station.toModel(categoryEntities)
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reportStationCategory(
        accessToken: String,
        stationId: String,
        categoryId: Int,
        status: String,
        note: String? = null
    ): Result<Unit> {
        return try {
            RetrofitClient.apiService.reportStation(
                authorization = "Bearer $accessToken",
                stationId = stationId,
                request = ReportRequestDto(
                    categoryId = categoryId,
                    status = status,
                    note = note
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}