package org.grupp18.sortsmart

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ItemApiService {

    @GET("api/v1/items/search")
    suspend fun searchItems(
        @Query("q") query: String
    ): ItemSearchResponse

    @GET("api/v1/items/{slug}")
    suspend fun getItemBySlug(
        @Path("slug") slug: String
    ): ItemDetail
}