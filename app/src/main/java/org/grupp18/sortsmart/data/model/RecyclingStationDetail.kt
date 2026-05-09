package org.grupp18.sortsmart.data.model

import com.google.android.gms.maps.model.LatLng

data class RecyclingStationDetail(
    val id: String,
    val name: String,
    val location: LatLng,
    val address: String?,
    val municipality: String,
    val stationType: String,
    val openingHours: String?,
    val operator: String?,
    val distanceKm: Double?,
    val acceptedCategories: List<WasteCategory>,
    val fullBins: List<String>,
    val reportCount: Int
)