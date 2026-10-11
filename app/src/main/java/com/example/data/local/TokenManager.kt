package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("malsz_secure_session", Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(getAccessToken() != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun getRole(): String = prefs.getString(KEY_ROLE, "MEMBER") ?: "MEMBER"

    fun getStatus(): String = prefs.getString(KEY_STATUS, "ACTIVE") ?: "ACTIVE"

    fun getExpiresAt(): String? = prefs.getString(KEY_EXPIRES_AT, null)

    fun getServerBaseUrl(): String {
        return prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
    }

    fun setServerBaseUrl(url: String) {
        val sanitized = if (url.endsWith("/")) url else "$url/"
        prefs.edit().putString(KEY_BASE_URL, sanitized).apply()
    }

    fun saveSession(
        accessToken: String,
        refreshToken: String,
        id: String,
        username: String,
        role: String,
        status: String,
        expiresAt: String?
    ) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_USER_ID, id)
            .putString(KEY_USERNAME, username)
            .putString(KEY_ROLE, role)
            .putString(KEY_STATUS, status)
            .putString(KEY_EXPIRES_AT, expiresAt)
            .apply()
        _isLoggedIn.value = true
    }

    fun updateAccessToken(accessToken: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, accessToken).apply()
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USERNAME)
            .remove(KEY_ROLE)
            .remove(KEY_STATUS)
            .remove(KEY_EXPIRES_AT)
            .apply()
        _isLoggedIn.value = false
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_ROLE = "role"
        private const val KEY_STATUS = "status"
        private const val KEY_EXPIRES_AT = "expires_at"
        private const val KEY_BASE_URL = "server_base_url"

        // Default: 10.0.2.2 is Android Emulator's loopback to host's 127.0.0.1:5000
        const val DEFAULT_BASE_URL = "http://10.0.2.2:5000/"
    }
}
