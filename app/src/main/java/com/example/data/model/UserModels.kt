package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateUserRequest(
    val username: String,
    val password: String,
    val role: String? = null,
    val expirationDays: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateUserResponse(
    val message: String,
    val user: UserDto
)

@JsonClass(generateAdapter = true)
data class BanUserRequest(
    val reason: String? = null
)

@JsonClass(generateAdapter = true)
data class UnbanUserRequest(
    val reason: String? = null
)

@JsonClass(generateAdapter = true)
data class PaginationDto(
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int
)

@JsonClass(generateAdapter = true)
data class UserListResponse(
    val users: List<UserDto>,
    val pagination: PaginationDto
)

@JsonClass(generateAdapter = true)
data class SystemStatsDto(
    val totalUsers: Int,
    val activeUsers: Int,
    val bannedUsers: Int,
    val expiredUsers: Int
)
