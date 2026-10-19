package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = RonovaPinkPrimary,
    onPrimary = Color.White,
    primaryContainer = RonovaPinkContainer,
    onPrimaryContainer = RonovaTextPrimary,
    secondary = RonovaPinkSecondary,
    onSecondary = RonovaTextPrimary,
    secondaryContainer = RonovaPinkLight,
    onSecondaryContainer = RonovaTextPrimary,
    tertiary = RonovaPinkDark,
    onTertiary = Color.White,
    background = RonovaBackground,
    onBackground = RonovaTextPrimary,
    surface = RonovaSurface,
    onSurface = RonovaTextPrimary,
    surfaceVariant = RonovaSurfaceVariant,
    onSurfaceVariant = RonovaTextSecondary,
    error = RonovaError,
    onError = Color.White,
    errorContainer = RonovaErrorContainer,
    onErrorContainer = RonovaError,
    outline = RonovaPinkSecondary,
    outlineVariant = RonovaPinkLight
)

private val DarkColorScheme = darkColorScheme(
    primary = RonovaPinkPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF50132E),
    onPrimaryContainer = RonovaPinkSecondary,
    secondary = RonovaPinkSecondary,
    onSecondary = Color(0xFF3F2936),
    secondaryContainer = Color(0xFF351224),
    onSecondaryContainer = Color.White,
    tertiary = RonovaPinkDark,
    onTertiary = Color.White,
    background = Color(0xFF1E121B),
    onBackground = Color(0xFFFCE7F3),
    surface = Color(0xFF261722),
    onSurface = Color(0xFFFCE7F3),
    surfaceVariant = Color(0xFF3B2433),
    onSurfaceVariant = RonovaPinkSecondary,
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    outline = Color(0xFF834469),
    outlineVariant = Color(0xFF502741)
)

@Composable
fun RonovaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    RonovaTheme(darkTheme = darkTheme, content = content)
}
