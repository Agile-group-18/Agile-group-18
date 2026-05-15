package org.grupp18.sortsmart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.grupp18.sortsmart.viewmodel.state.ProfileState

class UserSessionViewModel(
    private val profileViewModel: ProfileViewModel
) : ViewModel() {

    val displayName: StateFlow<String?> = profileViewModel.profileState
        .map { state ->
            (state as? ProfileState.Loaded)?.profile
                ?.let { it.displayName?.takeIf { n -> n.isNotBlank() } ?: it.username }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val avatarUrl: StateFlow<String?> = profileViewModel.profileState
        .map { (it as? ProfileState.Loaded)?.profile?.avatarUrl }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}