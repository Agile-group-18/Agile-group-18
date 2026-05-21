package org.grupp18.sortsmart.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.grupp18.sortsmart.data.model.RecyclingStationDetail
import org.grupp18.sortsmart.data.model.RecyclingStationMarker
import org.grupp18.sortsmart.data.model.WasteCategory
import org.grupp18.sortsmart.data.repository.StationRepository
import android.content.Context
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.withContext
import org.grupp18.sortsmart.data.model.ItemDetail
import org.grupp18.sortsmart.RouteOptimizer
import org.grupp18.sortsmart.util.MapNavigationUtil

class MapViewModel(
    private val repository: StationRepository
) : ViewModel() {

    private val _stationMarkers = MutableStateFlow<List<RecyclingStationMarker>>(emptyList())
    val stationMarkers: StateFlow<List<RecyclingStationMarker>> = _stationMarkers.asStateFlow()

    private val _categories = MutableStateFlow<List<WasteCategory>>(emptyList())
    val categories: StateFlow<List<WasteCategory>> = _categories.asStateFlow()

    private val _selectedCategoryIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedCategoryIds: StateFlow<Set<Int>> = _selectedCategoryIds.asStateFlow()

    private val _problemOnly = MutableStateFlow(false)
    val problemOnly: StateFlow<Boolean> = _problemOnly.asStateFlow()

    private val _selectedStation = MutableStateFlow<RecyclingStationDetail?>(null)
    val selectedStation: StateFlow<RecyclingStationDetail?> = _selectedStation.asStateFlow()

    private val _isLoadingMarkers = MutableStateFlow(false)
    val isLoadingMarkers: StateFlow<Boolean> = _isLoadingMarkers.asStateFlow()

    private val _isSyncingMarkers = MutableStateFlow(false)
    val isSyncingMarkers: StateFlow<Boolean> = _isSyncingMarkers.asStateFlow()

    private val _isLoadingStationDetail = MutableStateFlow(false)
    val isLoadingStationDetail: StateFlow<Boolean> = _isLoadingStationDetail.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch(Dispatchers.IO) {
            loadCachedDataInternal()

            val shouldSync = repository.shouldInitialSync() || repository.shouldRefreshStations()
            if (shouldSync) {
                syncStationsFromServer()
            }
        }
    }

    fun loadCachedData() {
        viewModelScope.launch(Dispatchers.IO) {
            loadCachedDataInternal()
        }
    }

    private suspend fun loadCachedDataInternal() {
        _isLoadingMarkers.value = true

        val cachedCategories = repository.getCachedCategories()
        val cachedMarkers = repository.getCachedMapMarkers(
            selectedCategoryIds = _selectedCategoryIds.value,
            problemOnly = _problemOnly.value
        )

        Log.d("SortSmartCache", "Loaded ${cachedCategories.size} categories from Room")
        Log.d("SortSmartCache", "Loaded ${cachedMarkers.size} station markers from Room")

        _categories.value = cachedCategories
        _stationMarkers.value = cachedMarkers

        _isLoadingMarkers.value = false
    }

    fun syncStationsFromServer() {
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncingMarkers.value = true

            Log.d("SortSmartCache", "Starting backend sync")

            val result = repository.syncInitialData()

            result.onSuccess {
                val cachedCategories = repository.getCachedCategories()
                val cachedMarkers = repository.getCachedMapMarkers(
                    selectedCategoryIds = _selectedCategoryIds.value,
                    problemOnly = _problemOnly.value
                )

                Log.d("SortSmartCache", "Sync complete")
                Log.d("SortSmartCache", "Room now has ${cachedCategories.size} categories")
                Log.d("SortSmartCache", "Room now has ${cachedMarkers.size} station markers")

                _categories.value = cachedCategories
                _stationMarkers.value = cachedMarkers
                _errorMessage.value = null
            }.onFailure { error ->
                Log.e("SortSmartCache", "Sync failed", error)
                _errorMessage.value = "Could not refresh stations: ${error.localizedMessage}"
            }

            _isSyncingMarkers.value = false
        }
    }

    fun toggleCategory(categoryId: Int) {
        val current = _selectedCategoryIds.value

        _selectedCategoryIds.value = if (current.contains(categoryId)) {
            current - categoryId
        } else {
            current + categoryId
        }

        reloadMarkersFromCache()
    }

    fun clearCategoryFilters() {
        _selectedCategoryIds.value = emptySet()
        reloadMarkersFromCache()
    }

    fun setProblemOnly(enabled: Boolean) {
        _problemOnly.value = enabled
        reloadMarkersFromCache()
    }

    private fun reloadMarkersFromCache() {
        viewModelScope.launch(Dispatchers.IO) {
            _stationMarkers.value = repository.getCachedMapMarkers(
                selectedCategoryIds = _selectedCategoryIds.value,
                problemOnly = _problemOnly.value
            )
        }
    }

    fun selectStation(stationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingStationDetail.value = true
            _selectedStation.value = null

            val result = repository.getStationDetail(stationId)

            result.onSuccess { station ->
                _selectedStation.value = station
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = "Could not load station details: ${error.localizedMessage}"
            }

            _isLoadingStationDetail.value = false
        }
    }

    fun clearSelectedStation() {
        _selectedStation.value = null
    }

    fun updateSelectedStationReportLocally(
        reportedCategories: List<WasteCategory>
    ) {
        val currentStation = _selectedStation.value ?: return

        _selectedStation.value = currentStation.copy(
            fullBins = reportedCategories.map { it.name }
        )

        _stationMarkers.value = _stationMarkers.value.map { marker ->
            if (marker.id == currentStation.id) {
                marker.copy(hasProblemReport = reportedCategories.isNotEmpty())
            } else {
                marker
            }
        }
    }

    fun reportStationCategories(
        stationId: String,
        categories: List<WasteCategory>,
        status: String = "full",
        note: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val failedReports = mutableListOf<Int>()

            categories.forEach { category ->
                val result = repository.reportStationCategory(
                    stationId = stationId,
                    categoryId = category.id,
                    status = status,
                    note = note
                )

                if (result.isFailure) {
                    failedReports.add(category.id)
                }
            }

            if (failedReports.isEmpty()) {
                syncStationsFromServer()
                _errorMessage.value = null
            } else {
                _errorMessage.value = "Could not submit all reports."
            }
        }
    }

    private val _isCalculatingRoute = MutableStateFlow(false)
    val isCalculatingRoute: StateFlow<Boolean> = _isCalculatingRoute.asStateFlow()

    private val routeOptimizer = RouteOptimizer()

    fun calculateAndShowRoute(
        context: Context,
        basketItems: List<ItemDetail>,
        strategy: RouteOptimizer.OptimizationStrategy
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isCalculatingRoute.value = true

            try {
                val allCategories = repository.getCachedCategories()

                val basketCategoryNames = basketItems
                    .mapNotNull { it.category?.name?.lowercase() }
                    .toSet()

                val basketCategories = allCategories.filter { it.name.lowercase() in basketCategoryNames }

                if (basketCategories.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Basket is empty or categories are missing!", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val mockedStartLocation = LatLng(57.708870, 11.974560)

                val availableStations = repository.getStationsForRouting()

                if (availableStations.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No stations downloaded yet! Please open the Map tab once to sync.", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                val route = routeOptimizer.calculateOptimalRoute(
                    currentLocation = mockedStartLocation,
                    basketCategories = basketCategories,
                    availableStations = availableStations,
                    strategy = strategy
                )

                withContext(Dispatchers.Main) {
                    MapNavigationUtil.launchGoogleMapsRoute(
                        context = context,
                        route = route,
                        origin = mockedStartLocation
                    )
                }

            } catch (e: RouteOptimizer.RouteNotFoundException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "No stations found that accept this combination of waste.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "An error occurred calculating the route.", Toast.LENGTH_SHORT).show()
                }
            } finally {
                _isCalculatingRoute.value = false
            }
        }
    }
}