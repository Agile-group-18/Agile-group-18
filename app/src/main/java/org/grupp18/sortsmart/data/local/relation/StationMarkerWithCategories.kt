package org.grupp18.sortsmart.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import org.grupp18.sortsmart.data.local.entity.StationCategoryEntity
import org.grupp18.sortsmart.data.local.entity.StationMarkerEntity

data class StationMarkerWithCategories(
    @Embedded
    val marker: StationMarkerEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "station_id"
    )
    val categories: List<StationCategoryEntity>
)