package com.subhunt.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Anime Dark Theme — the only theme (anime apps are always dark)
private val AnimeDarkScheme = darkColorScheme(
    primary = AnimeNeonPurple,
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF3700B3),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = AnimeNeonPink,
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF3D0030),
    onSecondaryContainer = Color(0xFFFFD9EC),
    tertiary = AnimeNeonBlue,
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF003D4D),
    onTertiaryContainer = Color(0xFFB8EAFF),
    background = AnimeVoid,
    onBackground = AnimeTextPrimary,
    surface = AnimeSurface,
    onSurface = AnimeTextPrimary,
    surfaceVariant = AnimeSurfaceLight,
    onSurfaceVariant = AnimeTextSecondary,
    error = AnimeNeonRed,
    onError = Color(0xFFFFFFFF),
    outline = AnimeTextMuted,
    outlineVariant = Color(0xFF303050)
)

// Anime Light Theme (rarely used but included for completeness)
private val AnimeLightScheme = lightColorScheme(
    primary = Color(0xFF6C3CE1),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005E),
    secondary = Color(0xFFD4447A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD9EC),
    onSecondaryContainer = Color(0xFF3D0030),
    background = Color(0xFFF8F7FF),
    onBackground = Color(0xFF1A1A2E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A2E),
    surfaceVariant = Color(0xFFF0EEFF),
    onSurfaceVariant = Color(0xFF49454F),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF)
)

@Composable
fun SubHuntTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled — we use our custom anime palette
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> AnimeDarkScheme
        else -> AnimeLightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AnimeTypography,
        content = content
    )
}
