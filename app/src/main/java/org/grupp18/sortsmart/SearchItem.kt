package org.grupp18.sortsmart

data class ItemCategory(
    val id: Int? = null,
    val name: String,
    val image_url: String? = null
)

data class SearchItem(
    val slug: String,
    val name: String,
    val category: ItemCategory? = null,
    val score: Double? = null
)