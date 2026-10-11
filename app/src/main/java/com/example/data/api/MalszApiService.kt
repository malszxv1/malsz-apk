package com.example.data.api

import com.example.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface MalszApiService {

    // Health
    @GET("api/health")
    suspend fun checkHealth(): Response<Map<String, Any>>

    // Auth
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @POST("api/auth/logout")
    suspend fun logout(@Body request: RefreshTokenRequest): Response<ApiResponse>

    @POST("api/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse>

    // Profile
    @GET("api/users/me")
    suspend fun getProfile(): Response<UserDto>

    @DELETE("api/users/me")
    suspend fun deleteMyAccount(): Response<ApiResponse>

    // Admin & Reseller Account Management
    @POST("api/admin/users")
    suspend fun createUser(@Body request: CreateUserRequest): Response<CreateUserResponse>

    @GET("api/admin/users")
    suspend fun listUsers(
        @Query("search") search: String? = null,
        @Query("role") role: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<UserListResponse>

    @GET("api/admin/users/{id}")
    suspend fun getUserDetail(@Path("id") id: String): Response<UserDto>

    @POST("api/admin/users/{id}/ban")
    suspend fun banUser(
        @Path("id") id: String,
        @Body request: BanUserRequest
    ): Response<ApiResponse>

    @POST("api/admin/users/{id}/unban")
    suspend fun unbanUser(
        @Path("id") id: String,
        @Body request: UnbanUserRequest
    ): Response<ApiResponse>

    @DELETE("api/admin/users/{id}")
    suspend fun deleteUser(@Path("id") id: String): Response<ApiResponse>

    @GET("api/admin/stats")
    suspend fun getStats(): Response<SystemStatsDto>

    // Global Chat
    @GET("api/chat/messages")
    suspend fun listMessages(
        @Query("limit") limit: Int = 50,
        @Query("before") before: String? = null
    ): Response<ChatListResponse>

    @POST("api/chat/messages")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<CreateMessageResponse>

    @DELETE("api/chat/messages/{id}")
    suspend fun deleteMessage(@Path("id") id: String): Response<ApiResponse>

    @POST("api/chat/messages/{id}/report")
    suspend fun reportMessage(
        @Path("id") id: String,
        @Body request: ReportMessageRequest
    ): Response<ApiResponse>

    // Floating AI
    @POST("api/ai/ask")
    suspend fun askAI(@Body request: AskRequest): Response<AskResponse>

    @POST("api/ai/solve-screen")
    suspend fun solveScreen(@Body request: SolveScreenRequest): Response<SolveScreenResponse>

    @POST("api/ai/fill-answer")
    suspend fun fillAnswer(@Body request: FillAnswerRequest): Response<FillAnswerResponse>
}
