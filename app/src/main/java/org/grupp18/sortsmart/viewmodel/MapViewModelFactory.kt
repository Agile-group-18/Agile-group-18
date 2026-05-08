package org.grupp18.sortsmart.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.grupp18.sortsmart.data.repository.StationRepository

class MapViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(
                repository = StationRepository(context.applicationContext)
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}