package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.local.TokenManager
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val tokenManager: TokenManager) {

    private val api get() = ApiClient.getService(tokenManager)

    suspend fun checkServerHealth(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.checkHealth()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Server returned status ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(username: String, password: String): Result<UserDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.login(LoginRequest(username = username, password = password))
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                tokenManager.saveSession(
                    accessToken = data.accessToken,
                    refreshToken = data.refreshToken,
                    id = data.user.id,
                    username = data.user.username,
                    role = data.user.role,
                    status = data.user.status,
                    expiresAt = data.user.expires_at
                )
                Result.success(data.user)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Login failed (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val refreshToken = tokenManager.getRefreshToken()
            if (!refreshToken.isNullOrBlank()) {
                api.logout(RefreshTokenRequest(refreshToken))
            }
            tokenManager.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            tokenManager.clearSession()
            Result.success(Unit)
        }
    }

    suspend fun changePassword(oldPass: String, newPass: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.changePassword(ChangePasswordRequest(oldPass, newPass))
            if (response.isSuccessful) {
                tokenManager.clearSession()
                Result.success(response.body()?.message ?: "Password changed successfully")
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Password change failed (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
