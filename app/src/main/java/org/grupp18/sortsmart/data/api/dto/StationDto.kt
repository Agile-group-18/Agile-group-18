package org.grupp18.sortsmart.data.api.dto

import com.google.gson.annotations.SerializedName

data class StationsMapResponseDto(
    val total: Int,
    val stations: List<StationMapDto>
)

data class StationMapDto(
    val id: String,
    @SerializedName("station_type")
    val stationType: String,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("categories")
    val categories: List<StationCategoryDto> = emptyList()
)

data class StationDetailDto(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val municipality: String,
    @SerializedName("station_type")
    val stationType: String,
    @SerializedName("opening_hours")
    val openingHours: String? = null,
    val operator: String? = null,
    @SerializedName("distance_km")
    val distanceKm: Double? = null,
    val categories: List<CategoryDto> = emptyList(),


    @SerializedName("category_statuses")
    val categoryStatuses: List<StationCategoryDto> = emptyList(),

    @SerializedName("report_count")
    val reportCount: Int = 0,
    @SerializedName("last_synced")
    val lastSynced: String? = null
)

data class StationCategoryDto(
    val id: Int,
    val status: String = "unknown"
)

data class NearbyResponseDto(
    val total: Int,
    val stations: List<StationDetailDto>,

    @SerializedName("query_lat")
    val queryLat: Double,
    @SerializedName("query_lon")
    val queryLon: Double
)


data class CategoryDto(
    val id: Int,
    val name: String,

    @SerializedName("image_url")
    val imageUrl: String? = null
)