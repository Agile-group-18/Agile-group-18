package org.grupp18.sortsmart

import retrofit2.http.GET
import retrofit2.http.Query

interface ItemApiService {

    @GET("items/search")
    suspend fun searchItems(
        @Query("q") query: String
    ): ItemSearchResponse
}