package org.grupp18.sortsmart

import com.google.gson.annotations.SerializedName

// Response containing search results from the API
data class ItemSearchResponse(
    val total: Int,
    val results: List<SearchItem>
)
// Basic information about an item returned from a search
data class SearchItem(
    val slug: String,
    val name: String,
    val score: Double? = null
)

// Detailed information about a specific item
data class ItemDetail(
    val slug: String,
    val name: String,
    val category: Category? = null,

    @SerializedName("leave_at")
    val leaveAt: String? = null,

    val processing: String? = null,

    @SerializedName("last_scraped")
    val lastScraped: String? = null
)

// Category information associated with an item
data class Category(
    val id: Int? = null,
    val name: String,

    @SerializedName("image_url")
    val imageUrl: String? = null
)