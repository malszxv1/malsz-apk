package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.local.TokenManager
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val tokenManager: TokenManager) {

    private val api get() = ApiClient.getService(tokenManager)

    suspend fun getProfile(): Result<UserDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProfile()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to fetch profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMyAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteMyAccount()
            if (response.isSuccessful) {
                tokenManager.clearSession()
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to delete account"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUser(
        username: String,
        password: String,
        role: String?,
        expirationDays: String?
    ): Result<UserDto> = withContext(Dispatchers.IO) {
        try {
            val req = CreateUserRequest(username, password, role, expirationDays)
            val response = api.createUser(req)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.user)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to create user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listUsers(
        search: String? = null,
        role: String? = null,
        status: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): Result<UserListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.listUsers(search, role, status, page, limit)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to list users"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun banUser(userId: String, reason: String?): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.banUser(userId, BanUserRequest(reason))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to ban user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unbanUser(userId: String, reason: String?): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.unbanUser(userId, UnbanUserRequest(reason))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to unban user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteUser(userId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to delete user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStats(): Result<SystemStatsDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.getStats()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to get stats"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
