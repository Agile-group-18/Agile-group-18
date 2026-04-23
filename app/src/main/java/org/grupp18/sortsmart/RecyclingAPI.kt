package org.grupp18.sortsmart

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// --- DATA MODELS FOR AVFALL SVERIGE (Main Map Data) ---

/**
 * Root response object from the Avfall Sverige API.
 */
data class ApiResponse(
    val avsList: List<ApiStation>
)

/**
 * Represents a single recycling station as returned by the primary API.
 */
data class ApiStation(
    val id: String?,
    val name: String?,
    val externalAvsId: String?,
    val municipalityCode: String?,
    val streetAddress: String?,
    val lat: String?,
    // The API uses "long" which is a reserved keyword in Kotlin,
    // so we map it to "longitude" using SerializedName.
    @SerializedName("long") val longitude: String?,
    val services: List<Any>?
)

// --- NETWORKING INTERFACES ---

/**
 * API service for fetching the general list of recycling stations.
 */
interface RecyclingApiService {
    @GET("umbraco/Api/SoporApi/GetCacheItems/")
    suspend fun getAllStations(): ApiResponse
}

/**
 * API service for fetching specific station details from sopor.nu.
 */
interface SoporApiService {
    /**
     * Fetches detailed station information.
     * Note: We use [ResponseBody] here because the endpoint returns raw HTML
     * intended for web scraping with Jsoup, rather than a structured JSON object.
     */
    @GET("umbraco/surface/AvfallshubbenSurface/GetItem/")
    suspend fun getStationDetails(
        @Query("externalId") externalId: String,
        @Query("municipalityCode") municipalityCode: String,
        @Query("type") type: String = "0"
    ): ResponseBody
}

// --- RETROFIT CLIENTS ---

/**
 * Client for Avfall Sverige (The central hub for station locations).
 */
object RetrofitClient {
    private const val BASE_URL = "https://avfallshubben.avfallsverige.se/"

    val apiService: RecyclingApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RecyclingApiService::class.java)
    }
}

/**
 * Client for Sopor.nu (Used for detailed waste fraction scraping).
 */
object SoporRetrofitClient {
    private const val BASE_URL = "https://www.sopor.nu/"

    val apiService: SoporApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SoporApiService::class.java)
    }
}

