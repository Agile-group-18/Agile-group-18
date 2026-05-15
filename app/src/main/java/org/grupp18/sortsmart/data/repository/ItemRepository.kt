package org.grupp18.sortsmart.data.repository

import org.grupp18.sortsmart.data.api.ItemApiService
import org.grupp18.sortsmart.data.api.RetrofitClient

class ItemRepository {
    private val api: ItemApiService = RetrofitClient.itemService

    suspend fun searchItems(query: String) = api.searchItems(query)
    suspend fun getItemBySlug(slug: String) = api.getItemBySlug(slug)
}