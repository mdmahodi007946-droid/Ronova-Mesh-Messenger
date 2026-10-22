package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightColorScheme = lightColorScheme(
    primary = RonovaGoldPrimary,
    onPrimary = RonovaTextPrimary,
    primaryContainer = RonovaGoldLight,
    onPrimaryContainer = RonovaTextPrimary,
    secondary = RonovaGoldSecondary,
    onSecondary = RonovaTextPrimary,
    secondaryContainer = RonovaGoldContainer,
    onSecondaryContainer = RonovaTextPrimary,
    tertiary = RonovaGoldDark,
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
    outlineVariant = RonovaGoldSecondary
)

@Composable
fun RonovaTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = false,
    isBangla: Boolean = false,
    content: @Composable () -> Unit
) {
    // English-first modern typography and clean M3 light color scheme
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = getAppTypography(isBangla = false),
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
