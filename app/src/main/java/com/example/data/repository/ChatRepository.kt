package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.local.TokenManager
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRepository(private val tokenManager: TokenManager) {

    private val api get() = ApiClient.getService(tokenManager)

    suspend fun listMessages(limit: Int = 50, before: String? = null): Result<List<ChatMessageDto>> = withContext(Dispatchers.IO) {
        try {
            val response = api.listMessages(limit, before)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.messages)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to load chat messages"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(text: String): Result<ChatMessageDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.sendMessage(SendMessageRequest(text))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to send message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMessage(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteMessage(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to delete message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reportMessage(id: String, reason: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.reportMessage(id, ReportMessageRequest(reason))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to report message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
