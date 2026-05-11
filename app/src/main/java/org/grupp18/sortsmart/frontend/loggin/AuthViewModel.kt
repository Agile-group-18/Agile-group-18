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
}

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    //Register

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.api.register(
                    RegisterRequest(username, email, password)
                )
                if (response.isSuccessful) {
                    _authState.value = AuthState.RegisteredPendingVerification
                } else if(response.code() == 400){
                    _authState.value = AuthState.Error("Email already exists")
                }
                else if(response.code() == 422){
                    _authState.value = AuthState.Error("Password must be minimum 8 characters and username must have 3 minimum characters")
                }
                else{
                    _authState.value = AuthState.Error("Registration failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    //Login

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.api.login(
                    LoginRequest(usernameOrEmail = username, password = password)
                )
                if (response.isSuccessful) {
                    response.body()?.let { auth ->
                        RetrofitClient.accessToken = auth.accessToken
                        _isLoggedIn.value = true
                        _authState.value = AuthState.Success("Logged in!")
                    }
                } else {
                    _authState.value = AuthState.Error(
                        if (response.code() == 401) "Wrong username or password"
                        else "Login failed: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    //Logout

    fun logout() {
        viewModelScope.launch {
            try {
                RetrofitClient.api.logout()
            } catch (_: Exception) { /* best-effort */ }
            RetrofitClient.accessToken = null
            _isLoggedIn.value = false
            _authState.value = AuthState.Idle
        }
    }

    //Forgot password

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = RetrofitClient.api.forgotPassword(
                    ForgotPasswordRequest(usernameOrEmail = email)
                )
                if (response.isSuccessful) {
                    _authState.value = AuthState.Success("Password reset email sent!")
                } else {
                    _authState.value = AuthState.Error("Could not send reset email: ${response.code()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}