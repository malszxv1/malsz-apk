package com.example

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.example.service.ScreenCaptureManager
import com.example.ui.navigation.MalszNavGraph
import com.example.ui.theme.MalszAITheme

class MainActivity : ComponentActivity() {

    private val mediaProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        ScreenCaptureManager.setMediaProjectionResult(result.resultCode, result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Prepare MediaProjection capture intent prompt if needed
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        projectionManager?.let { pm ->
            try {
                mediaProjectionLauncher.launch(pm.createScreenCaptureIntent())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        setContent {
            MalszAITheme {
                MalszNavGraph()
            }
        }
    }
}
