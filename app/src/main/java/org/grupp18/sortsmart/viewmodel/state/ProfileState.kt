package org.grupp18.sortsmart.viewmodel.state

import org.grupp18.sortsmart.data.api.dto.ProfileResponse

sealed class ProfileState {
    data object Idle : ProfileState()
    data object Loading : ProfileState()
    data class Loaded(val profile: ProfileResponse) : ProfileState()
    data class Error(val message: String) : ProfileState()
}