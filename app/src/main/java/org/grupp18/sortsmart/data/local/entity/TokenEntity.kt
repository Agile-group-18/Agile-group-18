package org.grupp18.sortsmart.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tokens")
data class TokenEntity(
    @PrimaryKey val id: Int = 1, // Single row — always update same row
    val accessToken: String,
    val refreshToken: String
)
