package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MalszDarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = ObsidianNavy,
    primaryContainer = MidnightBlue,
    onPrimaryContainer = HologramCyan,
    secondary = HologramCyan,
    onSecondary = ObsidianNavy,
    secondaryContainer = SurfaceElevated,
    onSecondaryContainer = RobotSilver,
    tertiary = ArmorGold,
    onTertiary = ObsidianNavy,
    background = ObsidianNavy,
    onBackground = TextPrimary,
    surface = MidnightBlue,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = RobotSilver,
    outline = CardBorder
)

@Composable
fun MalszAITheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MalszDarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MalszAITheme(content = content)
}
