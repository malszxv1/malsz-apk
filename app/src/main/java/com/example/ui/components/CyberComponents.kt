package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    borderColor: Color = CardBorder,
    backgroundColor: Color = MidnightBlue,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun CyberButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    color: Color = ElectricBlue,
    testTag: String = "cyber_button"
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .height(50.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = color.copy(alpha = 0.4f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = ObsidianNavy,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                color = ObsidianNavy,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun RobotAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    hasGlow: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_glow"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        if (hasGlow) {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(HologramCyan.copy(alpha = alpha), Color.Transparent)
                        )
                    )
            )
        }

        Image(
            painter = painterResource(id = R.drawable.img_malsz_robot),
            contentDescription = "Malsz AI Robot Mascot",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size - 6.dp)
                .clip(CircleShape)
                .border(2.dp, ArmorGold, CircleShape)
        )
    }
}

@Composable
fun RoleBadge(role: String, modifier: Modifier = Modifier) {
    val (badgeBg, badgeText, badgeBorder) = when (role.uppercase()) {
        "OWNER" -> Triple(ArmorGold.copy(alpha = 0.15f), ArmorGold, ArmorGold)
        "ADMIN" -> Triple(ElectricBlue.copy(alpha = 0.15f), HologramCyan, ElectricBlue)
        "RESELLER" -> Triple(HologramCyan.copy(alpha = 0.15f), HologramCyan, CardBorderCyan)
        else -> Triple(RobotSilver.copy(alpha = 0.1f), RobotSilver, CardBorder)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = badgeBg,
        border = BorderStroke(1.dp, badgeBorder),
        modifier = modifier
    ) {
        Text(
            text = role.uppercase(),
            color = badgeText,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (statusBg, statusText) = when (status.uppercase()) {
        "ACTIVE" -> Pair(StatusActive.copy(alpha = 0.15f), StatusActive)
        "BANNED" -> Pair(StatusBanned.copy(alpha = 0.15f), StatusBanned)
        "EXPIRED" -> Pair(StatusExpired.copy(alpha = 0.15f), StatusExpired)
        else -> Pair(TextMuted.copy(alpha = 0.15f), TextMuted)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = statusBg,
        border = BorderStroke(1.dp, statusText.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Text(
            text = status.uppercase(),
            color = statusText,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
