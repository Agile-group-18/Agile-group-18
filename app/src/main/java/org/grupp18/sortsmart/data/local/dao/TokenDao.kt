package org.grupp18.sortsmart.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.grupp18.sortsmart.data.local.entity.TokenEntity

@Dao
interface TokenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTokens(tokens: TokenEntity)

    @Query("SELECT * FROM tokens WHERE id = 1")
    suspend fun getTokens(): TokenEntity?

    @Query("DELETE FROM tokens")
    suspend fun clearTokens()
}
