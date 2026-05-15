package org.grupp18.sortsmart.data.api

import org.grupp18.sortsmart.data.model.ItemDetail
import org.grupp18.sortsmart.data.model.ItemSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ItemApiService {

    @GET("items/search")
    suspend fun searchItems(
        @Query("q") query: String
    ): ItemSearchResponse

    @GET("items/{slug}")
    suspend fun getItemBySlug(
        @Path("slug") slug: String
    ): ItemDetail
}