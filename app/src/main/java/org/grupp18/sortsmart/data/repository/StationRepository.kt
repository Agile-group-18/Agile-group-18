package org.grupp18.sortsmart.data.repository

import android.content.Context
import org.grupp18.sortsmart.data.api.AuthRetrofitClient
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
import org.grupp18.sortsmart.RouteOptimizer

class StationRepository(context: Context) {

    private val api = RetrofitClient.stationService
    private val stationDao = SortSmartDatabase.getInstance(context).stationDao()

    suspend fun getCachedCategories(): List<WasteCategory> =
        stationDao.getCategories().map { it.toModel() }

    suspend fun getCachedMapMarkers(
        selectedCategoryIds: Set<Int> = emptySet(),
        problemOnly: Boolean = false
    ): List<RecyclingStationMarker> {
        val categoryIds = selectedCategoryIds.toList()
        val entities = when {
            categoryIds.isEmpty() && problemOnly -> stationDao.getProblemMarkers()
            categoryIds.isEmpty() -> stationDao.getAllMarkers()
            problemOnly -> stationDao.getProblemMarkersMatchingAnyCategory(categoryIds)
            else -> stationDao.getMarkersMatchingAnyCategory(categoryIds)
        }
        return entities.map { it.toModel() }
    }

    suspend fun syncInitialData(): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()

        val categories = api.getCategories().map { it.toEntity(now) }

        val stationsResponse = api.getMapStations(view = "map")

        val markers = stationsResponse.stations.map { it.toMarkerEntity(now) }
        val stationCategories = stationsResponse.stations.flatMap { it.toStationCategoryEntities() }

        stationDao.replaceEverything(
            categories = categories,
            markers = markers,
            stationCategories = stationCategories,
            syncedAtMillis = now
        )
    }

    suspend fun shouldInitialSync(): Boolean =
        stationDao.markerCount() == 0 || stationDao.categoryCount() == 0

    suspend fun shouldRefreshStations(maxAgeMillis: Long = 30 * 60 * 1000L): Boolean {
        val metadata = stationDao.getCacheMetadata(CacheMetadataEntity.STATIONS_LAST_SYNC)
            ?: return true
        val lastSync = metadata.value.toLongOrNull() ?: return true
        return System.currentTimeMillis() - lastSync > maxAgeMillis
    }

    suspend fun getStationDetail(stationId: String): Result<RecyclingStationDetail> = runCatching {
        val station = api.getStationDetail(stationId)
        val categoryEntities = stationDao.getCategoriesByIds(
            station.categories.map { it.id }
        )
        station.toModel(categoryEntities)
    }

    suspend fun reportStationCategory(
        stationId: String,
        categoryId: Int,
        status: String,
        note: String? = null
    ): Result<Unit> = runCatching {
        val token = AuthRetrofitClient.accessToken
            ?: error("Not logged in")

        api.reportStation(
            authorization = "Bearer $token",
            stationId = stationId,
            request = ReportRequestDto(categoryId, status, note)
        )
    }

    suspend fun getStationsForRouting(): List<RouteOptimizer.StationNode> {
        val markersWithCategories = stationDao.getAllMarkersWithCategories()

        return markersWithCategories.map { relation ->
            RouteOptimizer.StationNode(
                marker = relation.marker.toModel(),
                supportedCategoryIds = relation.categories.map { it.categoryId }.toSet()
            )
        }
    }
}