package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.local.TokenManager
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AIRepository(private val tokenManager: TokenManager) {

    private val api get() = ApiClient.getService(tokenManager)

    suspend fun ask(question: String, history: List<ChatHistoryItem>? = null): Result<AskResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.askAI(AskRequest(question, history))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to get AI response"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun solveScreen(imageBase64: String): Result<SolveScreenResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.solveScreen(SolveScreenRequest(image = imageBase64, mimeType = "image/jpeg"))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to solve screen"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fillAnswer(fieldHint: String?, screenContext: String?): Result<FillAnswerResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.fillAnswer(FillAnswerRequest(fieldHint, screenContext))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to get fill answer"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
