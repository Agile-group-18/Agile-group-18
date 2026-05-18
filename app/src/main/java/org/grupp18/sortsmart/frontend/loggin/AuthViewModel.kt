package org.grupp18.sortsmart.frontend.loggin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
    object RegisteredPendingVerification : AuthState()
    object PasswordResetSuccess : AuthState()
}

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    // Try to restore session from database on app start
    fun tryRestoreSession() {
        viewModelScope.launch {
            val restored = RetrofitClient.loadTokensFromDb()
            if (restored) {
                _isLoggedIn.value = true
            }
        }
    }

    // Register

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.api.register(
                    RegisterRequest(username, email, password)
                )
                if (response.isSuccessful) {
                    _authState.value = AuthState.RegisteredPendingVerification
                } else if (response.code() == 400) {
                    _authState.value = AuthState.Error("Email already exists")
                } else if (response.code() == 422) {
                    _authState.value = AuthState.Error("Password must be minimum 8 characters and username must have 3 minimum characters")
                } else {
                    _authState.value = AuthState.Error("Registration failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    // Login

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.api.login(
                    LoginRequest(usernameOrEmail = username, password = password)
                )
                if (response.isSuccessful) {
                    response.body()?.let { auth ->
                        // Save both tokens to memory and database
                        RetrofitClient.saveTokens(auth.accessToken, auth.refreshToken)
                        _isLoggedIn.value = true
                        _authState.value = AuthState.Success("Logged in!")
                    }
                } else {
                    _authState.value = AuthState.Error(
                        when (response.code()) {
                            401  -> "Wrong username or password"
                            403  -> "Please verify your email before logging in"
                            else -> "Login failed: ${response.code()}"
                        }
                    )
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    // Logout

    fun logout() {
        viewModelScope.launch {
            try {
                RetrofitClient.api.logout()
            } catch (_: Exception) { /* best-effort */ }
            RetrofitClient.clearTokens()
            _isLoggedIn.value = false
            _authState.value = AuthState.Idle
        }
    }

    // Forgot password

    fun forgotPassword(usernameOrEmail: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.api.forgotPassword(
                    ForgotPasswordRequest(usernameOrEmail = usernameOrEmail)
                )
                if (response.isSuccessful) {
                    _authState.value = AuthState.Success("Password reset email sent!")
                } else {
                    _authState.value = AuthState.Error(
                        when (response.code()) {
                            429  -> "Too many attempts. Please wait a few minutes and try again."
                            404  -> "No account found with that username or email."
                            else -> "Could not send reset email: ${response.code()}"
                        }
                    )
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    // Reset password

    fun resetPassword(token: String, newPassword: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.api.resetPassword(
                    ResetPasswordRequest(token = token, newPassword = newPassword)
                )
                if (response.isSuccessful) {
                    _authState.value = AuthState.PasswordResetSuccess
                } else if (response.code() == 422 || response.code() == 400) {
                    _authState.value = AuthState.Error("expired reset")
                } else {
                    _authState.value = AuthState.Error("Reset failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
    fun verifyEmail(token: String, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.verifyEmail(token)
                onResult(response.code())
            } catch (e: Exception) {
                onResult(-1)
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

}