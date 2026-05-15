package org.grupp18.sortsmart.data.api

import org.grupp18.sortsmart.data.api.dto.AuthResponse
import org.grupp18.sortsmart.data.api.dto.ForgotPasswordRequest
import org.grupp18.sortsmart.data.api.dto.LoginRequest
import org.grupp18.sortsmart.data.api.dto.MessageResponse
import org.grupp18.sortsmart.data.api.dto.ProfileResponse
import org.grupp18.sortsmart.data.api.dto.RefreshRequest
import org.grupp18.sortsmart.data.api.dto.RegisterRequest
import org.grupp18.sortsmart.data.api.dto.ResetPasswordRequest
import org.grupp18.sortsmart.data.api.dto.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<MessageResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<MessageResponse>

    @GET("auth/verify-email")
    suspend fun verifyEmail(@Query("token") token: String): Response<MessageResponse>

    @GET("profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @PATCH("profile")
    suspend fun patchProfile(@Body body: UpdateProfileRequest): Response<ProfileResponse>

    @DELETE("profile")
    suspend fun deleteProfile(): Response<Unit>
}