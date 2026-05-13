package org.grupp18.sortsmart.data.mapper

import com.google.android.gms.maps.model.LatLng
import org.grupp18.sortsmart.data.api.dto.CategoryDto
import org.grupp18.sortsmart.data.api.dto.StationDetailDto
import org.grupp18.sortsmart.data.api.dto.StationMapDto
import org.grupp18.sortsmart.data.local.entity.CategoryEntity
import org.grupp18.sortsmart.data.local.entity.StationCategoryEntity
import org.grupp18.sortsmart.data.local.entity.StationMarkerEntity
import org.grupp18.sortsmart.data.model.RecyclingStationDetail
import org.grupp18.sortsmart.data.model.RecyclingStationMarker
import org.grupp18.sortsmart.data.model.WasteCategory

fun CategoryDto.toEntity(
    syncedAtMillis: Long
): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        imageUrl = imageUrl,
        updatedAtMillis = syncedAtMillis
    )
}

fun CategoryEntity.toModel(): WasteCategory {
    return WasteCategory(
        id = id,
        name = name,
        imageUrl = imageUrl
    )
}

fun StationMapDto.toMarkerEntity(
    syncedAtMillis: Long
): StationMarkerEntity {
    val hasProblemReport = categories.any { category ->
        category.status == "full" || category.status == "not_working"
    }

    return StationMarkerEntity(
        id = id,
        stationType = stationType,
        latitude = latitude,
        longitude = longitude,
        hasProblemReport = hasProblemReport,
        syncedAtMillis = syncedAtMillis
    )
}

fun StationMapDto.toStationCategoryEntities(): List<StationCategoryEntity> {
    return categories.map { category ->
        StationCategoryEntity(
            stationId = id,
            categoryId = category.id,
            status = category.status
        )
    }
}

fun StationMarkerEntity.toModel(): RecyclingStationMarker {
    return RecyclingStationMarker(
        id = id,
        stationType = stationType,
        location = LatLng(latitude, longitude),
        hasProblemReport = hasProblemReport
    )
}

fun StationDetailDto.toModel(
    categoryEntities: List<CategoryEntity>
): RecyclingStationDetail {
    val categoriesById = categoryEntities.associateBy { it.id }

    val acceptedCategories = categories.map { stationCategory ->
        val category = categoriesById[stationCategory.id]

        WasteCategory(
            id = stationCategory.id,
            name = category?.name ?: "Category ${stationCategory.id}",
            imageUrl = category?.imageUrl
        )
    }

    val problemCategoryIds = categories
        .filter { stationCategory ->
            stationCategory.status == "full" || stationCategory.status == "not_working"
        }
        .map { it.id }
        .toSet()

    val fullBins = acceptedCategories
        .filter { category -> category.id in problemCategoryIds }
        .map { it.name }

    return RecyclingStationDetail(
        id = id,
        name = name,
        location = LatLng(latitude, longitude),
        address = address,
        municipality = municipality,
        stationType = stationType,
        openingHours = openingHours,
        operator = operator,
        distanceKm = distanceKm,
        acceptedCategories = acceptedCategories,
        fullBins = fullBins,
        reportCount = reportCount
    )
}