package org.grupp18.sortsmart.data.api.dto

import com.google.gson.annotations.SerializedName

data class ReportRequestDto(
    @SerializedName("category_id")
    val categoryId: Int,

    val status: String,
    val note: String? = null
)

data class ReportResponseDto(
    @SerializedName("station_id")
    val stationId: String,

    val status: String,

    @SerializedName("report_count")
    val reportCount: Int,

    val message: String
)