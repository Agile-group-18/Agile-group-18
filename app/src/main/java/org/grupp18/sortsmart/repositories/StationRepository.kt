package org.grupp18.sortsmart.data

import com.google.android.gms.maps.model.LatLng
import org.grupp18.sortsmart.RecyclingStation
import org.grupp18.sortsmart.RetrofitClient
import org.grupp18.sortsmart.WasteCategory

class StationRepository {

    // We pass Gothenburg's coordinates by default
    suspend fun getStations(lat: Double = 57.708870, lon: Double = 11.974560): Result<List<RecyclingStation>> {
        return try {
            val response = RetrofitClient.apiService.getNearbyStations(lat = lat, lon = lon)

            val stations = response.stations.map { apiStation ->
                RecyclingStation(
                    name = apiStation.name,
                    location = LatLng(apiStation.latitude, apiStation.longitude),
                    externalId = apiStation.id, // Using the new backend's primary ID
                    municipalityCode = "", // We no longer need this for scraping!
                    acceptedCategories = apiStation.waste_types.map {
                        WasteCategory(name = it.waste_type, iconUrl = it.image_url)
                    },

                    // The new backend returns a "reported_status" string ("operational", "full", etc.)
                    // If it's full, we just add a dummy string to trigger the red map pin in the UI.
                    fullBins = if (apiStation.reported_status == "full") listOf("Reported Full") else emptyList<String>()
                    )
            }
            Result.success(stations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}