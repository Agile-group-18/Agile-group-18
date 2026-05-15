package org.grupp18.sortsmart.data.api.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    @SerializedName("username_or_email")
    val usernameOrEmail: String,
    val password: String,
)

data class RefreshRequest(
    @SerializedName("refresh_token")
    val refreshToken: String
)

data class ForgotPasswordRequest(
    @SerializedName("username_or_email")
    val usernameOrEmail: String
)

data class ResetPasswordRequest(
    val token: String,
    @SerializedName("new_password")
    val newPassword: String,
)

data class UpdateProfileRequest(
    @SerializedName("display_name")
    val displayName: String? = null,
    @SerializedName("avatar_url")
    val avatarUrl: String? = null,
    val username: String? = null,
    val email: String? = null,
)

data class AuthResponse(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("token_type")
    val tokenType: String = "bearer",
)

data class MessageResponse(
    val message: String
)

data class ProfileResponse(
    val id: String,
    val username: String,
    val email: String,
    @SerializedName("display_name")
    val displayName: String?,
    @SerializedName("avatar_url")
    val avatarUrl: String?,
    @SerializedName("is_verified")
    val isVerified: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("report_count")
    val reportCount: Int,
)