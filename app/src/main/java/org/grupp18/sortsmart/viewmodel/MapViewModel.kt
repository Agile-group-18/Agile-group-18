package org.grupp18.sortsmart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.grupp18.sortsmart.RecyclingStation
import org.grupp18.sortsmart.data.StationRepository

class MapViewModel : ViewModel() {

    private val repository = StationRepository()

    private val _stations = MutableStateFlow<List<RecyclingStation>>(emptyList())
    val stations: StateFlow<List<RecyclingStation>> = _stations.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchStations()
    }

    private fun fetchStations() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getStations()

            result.onSuccess { data ->
                _stations.value = data
                _errorMessage.value = null
            }.onFailure { error ->
                _errorMessage.value = "Network error: ${error.localizedMessage}"
            }

            _isLoading.value = false
        }
    }


    fun updateStationReport(stationId: String, reportedBins: List<String>) {
        _stations.value = _stations.value.map { station ->
            if (station.externalId == stationId) {
                station.copy(fullBins = reportedBins)
            } else {
                station
            }
        }
    }
}