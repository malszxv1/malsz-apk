package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatMessageDto(
    val id: String,
    val user_id: String,
    val message: String,
    val created_at: String,
    val deleted_at: String? = null,
    val username: String,
    val role: String,
    val status: String
)

@JsonClass(generateAdapter = true)
data class ChatListResponse(
    val messages: List<ChatMessageDto>
)

@JsonClass(generateAdapter = true)
data class CreateMessageResponse(
    val message: ChatMessageDto
)

@JsonClass(generateAdapter = true)
data class SendMessageRequest(
    val message: String
)

@JsonClass(generateAdapter = true)
data class ReportMessageRequest(
    val reason: String
)
