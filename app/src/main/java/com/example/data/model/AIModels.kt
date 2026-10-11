package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatHistoryItem(
    val text: String,
    val isUser: Boolean
)

@JsonClass(generateAdapter = true)
data class AskRequest(
    val question: String,
    val history: List<ChatHistoryItem>? = null
)

@JsonClass(generateAdapter = true)
data class AskResponse(
    val answer: String,
    val provider: String,
    val timestamp: String
)

@JsonClass(generateAdapter = true)
data class SolveScreenRequest(
    val image: String,
    val mimeType: String = "image/jpeg"
)

@JsonClass(generateAdapter = true)
data class SolveScreenResponse(
    val answer: String,
    val reason: String,
    val question: String? = null,
    val provider: String,
    val timestamp: String
)

@JsonClass(generateAdapter = true)
data class FillAnswerRequest(
    val fieldHint: String?,
    val screenContext: String?
)

@JsonClass(generateAdapter = true)
data class FillAnswerResponse(
    val fill_text: String,
    val delay_seconds: Int = 2,
    val provider: String,
    val timestamp: String
)
