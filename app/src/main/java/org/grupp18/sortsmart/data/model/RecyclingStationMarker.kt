package org.grupp18.sortsmart.data.model

import com.google.android.gms.maps.model.LatLng

data class RecyclingStationMarker(
    val id: String,
    val stationType: String,
    val location: LatLng,
    val hasProblemReport: Boolean
)