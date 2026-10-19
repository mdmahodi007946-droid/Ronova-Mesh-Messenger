package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightColorScheme = lightColorScheme(
    primary = RonovaPinkPrimary,
    onPrimary = Color.White,
    primaryContainer = RonovaPinkContainer,
    onPrimaryContainer = RonovaPinkDark,
    secondary = RonovaVioletAccent,
    onSecondary = Color.White,
    secondaryContainer = RonovaVioletLight,
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
    outline = RonovaCardBorder,
    outlineVariant = RonovaPinkContainer
)

@Composable
fun RonovaTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    // Strictly Light Mode only - no dark theme per user requirement
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    RonovaTheme(content = content)
}
