package org.grupp18.sortsmart.data.repository

import org.grupp18.sortsmart.data.api.AuthApiService
import org.grupp18.sortsmart.data.api.AuthRetrofitClient
import org.grupp18.sortsmart.data.api.dto.ForgotPasswordRequest
import org.grupp18.sortsmart.data.api.dto.LoginRequest
import org.grupp18.sortsmart.data.api.dto.RegisterRequest
import org.grupp18.sortsmart.data.api.dto.ResetPasswordRequest
import org.grupp18.sortsmart.data.api.dto.UpdateProfileRequest

class AuthRepository {
    private val api: AuthApiService = AuthRetrofitClient.api

    suspend fun login(username: String, password: String) =
        api.login(LoginRequest(usernameOrEmail = username, password = password))

    suspend fun register(username: String, email: String, password: String) =
        api.register(RegisterRequest(username, email, password))

    suspend fun logout() = api.logout()

    suspend fun forgotPassword(usernameOrEmail: String) =
        api.forgotPassword(ForgotPasswordRequest(usernameOrEmail))

    suspend fun resetPassword(token: String, newPassword: String) =
        api.resetPassword(ResetPasswordRequest(token, newPassword))

    suspend fun getProfile() = api.getProfile()

    suspend fun updateProfile(body: UpdateProfileRequest) = api.patchProfile(body)

    suspend fun deleteProfile() = api.deleteProfile()

    suspend fun saveTokens(access: String, refresh: String) =
        AuthRetrofitClient.saveTokens(access, refresh)

    suspend fun loadSavedSession() = AuthRetrofitClient.loadTokensFromDb()

    suspend fun clearTokens() = AuthRetrofitClient.clearTokens()

    suspend fun verifyEmail(token: String) = api.verifyEmail(token)
}