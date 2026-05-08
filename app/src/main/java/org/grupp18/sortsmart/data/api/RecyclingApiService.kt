package org.grupp18.sortsmart.data.api

import org.grupp18.sortsmart.data.api.dto.CategoryDto
import org.grupp18.sortsmart.data.api.dto.ReportRequestDto
import org.grupp18.sortsmart.data.api.dto.ReportResponseDto
import org.grupp18.sortsmart.data.api.dto.StationDetailDto
import org.grupp18.sortsmart.data.api.dto.StationsMapResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RecyclingApiService {

    @GET("stations/categories")
    suspend fun getCategories(): List<CategoryDto>

    @GET("stations")
    suspend fun getMapStations(
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null,
        @Query("radius_km") radiusKm: Double? = null,
        @Query("category_ids") categoryIds: List<Int>? = null,
        @Query("station_type") stationType: String? = null,
        @Query("view") view: String = "map"
    ): StationsMapResponseDto

    @GET("stations/{station_id}")
    suspend fun getStationDetail(
        @Path("station_id") stationId: String
    ): StationDetailDto

    @POST("stations/{station_id}/report")
    suspend fun reportStation(
        @Header("Authorization") authorization: String,
        @Path("station_id") stationId: String,
        @Body request: ReportRequestDto
    ): ReportResponseDto
}