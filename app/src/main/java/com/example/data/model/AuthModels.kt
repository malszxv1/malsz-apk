package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: String,
    val username: String,
    val role: String,
    val status: String,
    val created_by: String? = null,
    val created_at: String,
    val expires_at: String? = null,
    val banned_at: String? = null,
    val ban_reason: String? = null,
    val creator_username: String? = null
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val username: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val message: String,
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto
)

@JsonClass(generateAdapter = true)
data class RefreshTokenRequest(
    val refreshToken: String
)

@JsonClass(generateAdapter = true)
data class RefreshTokenResponse(
    val accessToken: String
)

@JsonClass(generateAdapter = true)
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

@JsonClass(generateAdapter = true)
data class ApiResponse(
    val message: String? = null,
    val error: String? = null
)
