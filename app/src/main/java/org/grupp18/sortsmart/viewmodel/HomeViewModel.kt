package org.grupp18.sortsmart.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.grupp18.sortsmart.data.api.dto.StationDetailDto
import org.grupp18.sortsmart.data.repository.StationRepository
import kotlin.coroutines.resume

sealed class NearestStationState {
    data object Idle : NearestStationState()
    data object Loading : NearestStationState()
    data class Ready(val station: StationDetailDto?) : NearestStationState()
    data object Unavailable : NearestStationState()
}

sealed class DailyTipState {
    data object Idle : DailyTipState()
    data object Loading : DailyTipState()
    data class Ready(val tip: String) : DailyTipState()
    data object Unavailable : DailyTipState()
}

class HomeViewModel(
    private val stationRepository: StationRepository
) : ViewModel() {

    private val _nearestStation = MutableStateFlow<NearestStationState>(NearestStationState.Idle)
    val nearestStation: StateFlow<NearestStationState> = _nearestStation.asStateFlow()

    private val _dailyTip = MutableStateFlow<DailyTipState>(DailyTipState.Idle)
    val dailyTip: StateFlow<DailyTipState> = _dailyTip.asStateFlow()

    fun loadRandomTip() {
        if (_dailyTip.value !is DailyTipState.Idle) return
        viewModelScope.launch {
            _dailyTip.value = DailyTipState.Loading
            stationRepository.getRandomTip()
                .onSuccess { tip ->
                    _dailyTip.value = if (!tip.isNullOrBlank()) DailyTipState.Ready(tip)
                    else DailyTipState.Unavailable
                }
                .onFailure { _dailyTip.value = DailyTipState.Unavailable }
        }
    }

    @SuppressLint("MissingPermission")
    fun loadNearestStation(context: Context) {
        if (_nearestStation.value !is NearestStationState.Idle) return
        viewModelScope.launch {
            _nearestStation.value = NearestStationState.Loading
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                val cts = CancellationTokenSource()
                val location = suspendCancellableCoroutine { continuation ->
                    continuation.invokeOnCancellation { cts.cancel() }
                    fusedClient.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        cts.token
                    ).addOnSuccessListener { loc ->
                        continuation.resume(loc)
                    }.addOnFailureListener {
                        continuation.resume(null)
                    }
                }

                if (location == null) {
                    _nearestStation.value = NearestStationState.Unavailable
                    return@launch
                }

                stationRepository.getNearestStation(location.latitude, location.longitude)
                    .onSuccess { _nearestStation.value = NearestStationState.Ready(it) }
                    .onFailure { _nearestStation.value = NearestStationState.Unavailable }

            } catch (e: Exception) {
                _nearestStation.value = NearestStationState.Unavailable
            }
        }
    }

    fun refresh(context: Context) {
        _nearestStation.value = NearestStationState.Idle
        _dailyTip.value = DailyTipState.Idle
        loadRandomTip()
        loadNearestStation(context)
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(StationRepository(context.applicationContext)) as T
    }
}