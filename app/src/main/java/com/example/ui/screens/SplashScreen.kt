package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TokenManager
import com.example.data.repository.AuthRepository
import com.example.ui.components.RobotAvatar
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val authRepository = remember { AuthRepository(tokenManager) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(1500)
        val token = tokenManager.getAccessToken()
        if (!token.isNullOrBlank()) {
            onNavigate(Screen.Dashboard.route)
        } else {
            onNavigate(Screen.Login.route)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianNavy)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            RobotAvatar(size = 140.dp)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "MALSZ AI ROBOT",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = HologramCyan,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "PREMIUM CYBER COMPANION",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ArmorGold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Connecting to Cyber Network...",
                fontSize = 11.sp,
                color = RobotSilver.copy(alpha = 0.7f)
            )
        }
    }
}
