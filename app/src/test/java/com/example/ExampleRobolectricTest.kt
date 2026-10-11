package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.TokenManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches Malsz AI Robot`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Malsz AI Robot", appName)
    }

    @Test
    fun `token manager session lifecycle`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tokenManager = TokenManager(context)

        tokenManager.saveSession(
            accessToken = "test_access_jwt",
            refreshToken = "test_refresh_token",
            id = "user-123",
            username = "malsz",
            role = "OWNER",
            status = "ACTIVE",
            expiresAt = null
        )

        assertEquals("test_access_jwt", tokenManager.getAccessToken())
        assertEquals("malsz", tokenManager.getUsername())
        assertEquals("OWNER", tokenManager.getRole())
        assertEquals("ACTIVE", tokenManager.getStatus())

        tokenManager.clearSession()
        assertEquals(null, tokenManager.getAccessToken())
        assertEquals(null, tokenManager.getUsername())
    }

    @Test
    fun `default server base url is set to emulator loopback`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tokenManager = TokenManager(context)
        assertEquals("http://10.0.2.2:5000/", tokenManager.getServerBaseUrl())
    }
}
