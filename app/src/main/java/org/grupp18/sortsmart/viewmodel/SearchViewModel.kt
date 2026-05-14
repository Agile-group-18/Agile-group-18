package org.grupp18.sortsmart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.grupp18.sortsmart.data.model.ItemDetail
import org.grupp18.sortsmart.data.model.SearchItem
import org.grupp18.sortsmart.data.repository.ItemRepository

@OptIn(FlowPreview::class)
class SearchViewModel : ViewModel() {

    private val repository = ItemRepository()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _suggestions = MutableStateFlow<List<SearchItem>>(emptyList())
    val suggestions: StateFlow<List<SearchItem>> = _suggestions

    private val _selectedItem = MutableStateFlow<ItemDetail?>(null)
    val selectedItem: StateFlow<ItemDetail?> = _selectedItem

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        _query
            .debounce(400)
            .distinctUntilChanged()
            .filter { it.length >= 2 }
            .onEach { fetchSuggestions(it) }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(text: String) {
        _query.value = text
        _selectedItem.value = null
        if (text.length < 2) {
            _suggestions.value = emptyList()
            _error.value = null
        }
    }

    fun selectItem(item: SearchItem) {
        _query.value = item.name
        _suggestions.value = emptyList()
        _selectedItem.value = null
        viewModelScope.launch {
            try {
                _selectedItem.value = repository.getItemBySlug(item.slug)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Could not load item details"
            }
        }
    }

    fun clearSelection() {
        _selectedItem.value = null
        _suggestions.value = emptyList()
        _query.value = ""
        _error.value = null
    }

    private suspend fun fetchSuggestions(query: String) {
        try {
            _suggestions.value = repository.searchItems(query).results
            _error.value = null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _error.value = e.localizedMessage ?: "Unknown error"
            _suggestions.value = emptyList()
        }
    }
}