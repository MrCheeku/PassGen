package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ElegantDarkPrimary,
    onPrimary = ElegantDarkOnPrimary,
    primaryContainer = ElegantDarkPrimary,
    onPrimaryContainer = ElegantDarkOnPrimary,
    secondary = ElegantDarkBorder,
    onSecondary = ElegantDarkPrimary,
    secondaryContainer = ElegantDarkBorder,
    onSecondaryContainer = ElegantDarkPrimary,
    tertiary = ElegantDarkPrimaryMedium,
    onTertiary = Color.White,
    background = ElegantDarkBg,
    onBackground = ElegantDarkTextPrimary,
    surface = ElegantDarkSurface,
    onSurface = ElegantDarkTextPrimary,
    surfaceVariant = ElegantDarkSurfaceElevated,
    onSurfaceVariant = ElegantDarkTextMuted,
    outline = ElegantDarkBorder,
    error = ElegantDarkWeak,
    onError = ElegantDarkOnWeak
)

private val LightColorScheme = lightColorScheme(
    primary = CyberPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFBAE6FD),
    onSecondaryContainer = Color(0xFF075985),
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    background = CyberBackgroundLight,
    onBackground = Color(0xFF0F172A),
    surface = CyberSurfaceLight,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = CyberCardLight,
    onSurfaceVariant = Color(0xFF475569),
    outline = CyberBorderLight,
    error = SecurityRed
)

@Composable
fun PassGenTheme(
    appThemeSetting: String = "system",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = when (appThemeSetting) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Kept for backward-compatibility with template previews
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    PassGenTheme(
        appThemeSetting = if (darkTheme) "dark" else "light",
        dynamicColor = dynamicColor,
        content = content
    )
}

