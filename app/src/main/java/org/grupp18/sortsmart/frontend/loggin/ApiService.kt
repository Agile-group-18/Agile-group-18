package org.grupp18.sortsmart.frontend.loggin

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

//Request bodies

data class RegisterRequest(
    val username: String, // Krav: 3-50 tecken, regex ^[a-zA-Z0-9_-]+$
    val email: String,
    val password: String  // Krav: 8-128 tecken
)

data class LoginRequest(
    @SerializedName("username_or_email") val usernameOrEmail: String,
    val password: String
)

data class RefreshRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class ForgotPasswordRequest(
    @SerializedName("username_or_email") val usernameOrEmail: String
)

data class ResetPasswordRequest(
    val token: String,
    @SerializedName("new_password") val newPassword: String
)

data class UpdateProfileRequest(
    @SerializedName("display_name") val displayName: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    val username: String? = null,
    val email: String? = null
)

//Response bodies

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("token_type") val tokenType: String = "bearer"
)

data class MessageResponse(val message: String)

data class ProfileResponse(
    val id: String,
    val username: String,
    val email: String,
    @SerializedName("display_name") val displayName: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("is_verified") val isVerified: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("report_count") val reportCount: Int
)

data class StationResponse(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("waste_types") val wasteTypes: List<String>,
    @SerializedName("distance_km") val distanceKm: Double?,
    @SerializedName("reported_status") val reportedStatus: String,
    val address: String?
)

data class StationListResponse(
    val total: Int,
    val stations: List<StationResponse>,
    @SerializedName("query_lat") val queryLat: Double,
    @SerializedName("query_lon") val queryLon: Double
)

data class CategoryResponse(
    val categories: List<String>,
    @SerializedName("total_stations_per_category") val totalPerCategory: Map<String, Int>
)

data class StationReportRequest(
    val status: String, // "operational", "full", "not_working", "unknown"
    val note: String? = null
)

//Retrofit interface

interface ApiService {

    //Auth

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

    // ── Profile ──

    @GET("profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @PATCH("profile")
    suspend fun patchProfile(@Body body: UpdateProfileRequest): Response<ProfileResponse>

    @DELETE("profile")
    suspend fun deleteProfile(): Response<Unit>
}