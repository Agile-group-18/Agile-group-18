package org.grupp18.sortsmart

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// --- NEW BACKEND DATA MODELS ---
// These match the Pydantic schemas in your FastAPI 'models/schemas.py'

data class NearbyResponse(
    val total: Int,
    val stations: List<StationSummary>,
    val query_lat: Double,
    val query_lon: Double
)

data class StationSummary(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val distance_km: Double?,
    val waste_types: List<WasteTypeResponse>,
    val reported_status: String,
    val address: String?
)

data class WasteTypeResponse(
    val waste_type: String,
    val image_url: String?
)

// --- NETWORKING INTERFACES ---
interface RecyclingApiService {
    @GET("stations") // Calls /api/v1/stations
    suspend fun getNearbyStations(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("limit") limit: Int = 100,
        @Query("radius_km") radiusKm: Double = 50.0
    ): NearbyResponse
}

// --- RETROFIT CLIENT ---
object RetrofitClient {
    // If you deploy your backend, change this to "https://sortsmart.klepoatra.pro/api/v1/"
    private const val BASE_URL = "http://10.0.2.2:8000/api/v1/"

    val apiService: RecyclingApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RecyclingApiService::class.java)
    }
}