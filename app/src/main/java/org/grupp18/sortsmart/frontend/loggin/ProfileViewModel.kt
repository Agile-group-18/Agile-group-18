package org.grupp18.sortsmart.frontend.loggin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.grupp18.sortsmart.frontend.loggin.ProfileResponse
import org.grupp18.sortsmart.frontend.loggin.RetrofitClient
import org.grupp18.sortsmart.frontend.loggin.UpdateProfileRequest

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class Loaded(val profile: ProfileResponse) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class ProfileViewModel : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState

    //Load

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val response = RetrofitClient.api.getProfile()
                if (response.isSuccessful) {
                    response.body()?.let { _profileState.value = ProfileState.Loaded(it) }
                } else {
                    _profileState.value = ProfileState.Error("Failed to load profile: ${response.code()}")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    //Update

    fun updateProfile(username: String, email: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val response = RetrofitClient.api.patchProfile(
                    UpdateProfileRequest(username = username, email = email)
                )

                if (response.isSuccessful) {
                    response.body()?.let {
                        _profileState.value = ProfileState.Loaded(it)
                    }
                } else {
                    _profileState.value = ProfileState.Error("Update failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    //Delete

    fun deleteProfile(onDeleted: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.deleteProfile()
                if (response.isSuccessful) {
                    RetrofitClient.accessToken = null
                    onDeleted()
                } else {
                    _profileState.value = ProfileState.Error("Delete failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}