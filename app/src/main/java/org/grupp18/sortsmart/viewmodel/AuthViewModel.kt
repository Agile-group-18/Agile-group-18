package org.grupp18.sortsmart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.grupp18.sortsmart.data.repository.AuthRepository
import org.grupp18.sortsmart.viewmodel.state.AuthState

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _isRestoringSession = MutableStateFlow(true)  // true until DB check completes
    val isRestoringSession: StateFlow<Boolean> = _isRestoringSession

    fun tryRestoreSession() {
        viewModelScope.launch {
            _isRestoringSession.value = true
            val restored = repository.loadSavedSession()
            if (restored) _isLoggedIn.value = true
            _isRestoringSession.value = false
        }
    }

    fun register(username: String, email: String, password: String) = launch {
        val response = repository.register(username, email, password)
        _authState.value = when {
            response.isSuccessful -> AuthState.RegisteredPendingVerification
            response.code() == 400 -> AuthState.Error("Email already exists")
            response.code() == 422 -> AuthState.Error("Password must be minimum 8 characters and username must have 3 minimum characters")
            else -> AuthState.Error("Registration failed: ${response.code()}")
        }
    }

    fun login(username: String, password: String) = launch {
        val response = repository.login(username, password)
        if (response.isSuccessful) {
            response.body()?.let {
                repository.saveTokens(it.accessToken, it.refreshToken)
                _isLoggedIn.value = true
                _authState.value = AuthState.Success("Logged in!")
            }
        } else {
            _authState.value = AuthState.Error(
                when (response.code()) {
                    401 -> "Wrong username or password"
                    403 -> "Please verify your email before logging in"
                    else -> "Login failed: ${response.code()}"
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                repository.logout()
            } catch (_: Exception) { /* best-effort */
            }
            repository.clearTokens()
            _isLoggedIn.value = false
            _authState.value = AuthState.Idle
        }
    }

    fun forgotPassword(usernameOrEmail: String) = launch {
        val response = repository.forgotPassword(usernameOrEmail)
        _authState.value = when {
            response.isSuccessful -> AuthState.Success("Password reset email sent!")
            response.code() == 429 -> AuthState.Error("Too many attempts. Please wait a few minutes and try again.")
            response.code() == 404 -> AuthState.Error("No account found with that username or email.")
            else -> AuthState.Error("Could not send reset email: ${response.code()}")
        }
    }

    fun resetPassword(token: String, newPassword: String) = launch {
        val response = repository.resetPassword(token, newPassword)
        _authState.value = when {
            response.isSuccessful -> AuthState.PasswordResetSuccess
            response.code() == 422 || response.code() == 400 -> AuthState.Error("expired reset")
            else -> AuthState.Error("Reset failed: ${response.code()}")
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                block()
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}