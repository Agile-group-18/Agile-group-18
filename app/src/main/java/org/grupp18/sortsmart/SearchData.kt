package org.grupp18.sortsmart

data class ItemSearchResponse(
    val total: Int,
    val results: List<SearchItem>
)