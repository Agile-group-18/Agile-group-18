package org.grupp18.sortsmart.viewmodel.state

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
    data object RegisteredPendingVerification : AuthState()
    data object PasswordResetSuccess : AuthState()
}