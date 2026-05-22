package org.grupp18.sortsmart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.grupp18.sortsmart.data.api.AuthRetrofitClient
import org.grupp18.sortsmart.data.api.dto.UpdateProfileRequest
import org.grupp18.sortsmart.viewmodel.state.ProfileState

class ProfileViewModel : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState

    fun loadProfile() {
        if (_profileState.value is ProfileState.Loading ||
            _profileState.value is ProfileState.Loaded
        ) return
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val response = AuthRetrofitClient.api.getProfile()
                if (response.isSuccessful) {
                    response.body()?.let { _profileState.value = ProfileState.Loaded(it) }
                } else {
                    _profileState.value =
                        ProfileState.Error("Failed to load profile: ${response.code()}")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun updateProfile(username: String, email: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val response = AuthRetrofitClient.api.patchProfile(
                    UpdateProfileRequest(username = username, email = email)
                )

                if (response.isSuccessful) {
                    response.body()?.let {
                        _profileState.value = ProfileState.Loaded(it)
                    }
                } else {
                    _profileState.value = ProfileState.Error(
                        response.message().takeIf { it.isNotBlank() }
                            ?: response.errorBody()?.string()
                            ?: "HTTP ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun deleteProfile(onDeleted: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = AuthRetrofitClient.api.deleteProfile()
                if (response.isSuccessful) {
                    _profileState.value = ProfileState.Idle
                    onDeleted()
                } else {
                    _profileState.value = ProfileState.Error("Delete failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun resetProfile() {
        _profileState.value = ProfileState.Idle
    }
}