package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Android System Font Typography for Ronova Mesh PRD v2.1.
 * Complies with Section 4.3:
 * - Uses the Android system font (FontFamily.Default).
 * - No custom font files bundled.
 * - Supports system font scaling and accessibility.
 * - Line heights at least 1.4x for Bengali conjuncts and clear rendering.
 */
fun getAppTypography(isBangla: Boolean = false): Typography {
    val family = FontFamily.Default

    return Typography(
        displayLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 52.sp,
            lineHeight = if (isBangla) 74.sp else 60.sp,
            letterSpacing = if (isBangla) 0.sp else (-0.25).sp
        ),
        displayMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 42.sp,
            lineHeight = if (isBangla) 60.sp else 50.sp,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 34.sp,
            lineHeight = if (isBangla) 48.sp else 40.sp,
            letterSpacing = 0.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            lineHeight = if (isBangla) 42.sp else 36.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = if (isBangla) 36.sp else 32.sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = if (isBangla) 30.sp else 28.sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            lineHeight = if (isBangla) 28.sp else 26.sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = if (isBangla) 24.sp else 22.sp,
            letterSpacing = if (isBangla) 0.sp else 0.15.sp
        ),
        titleSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = if (isBangla) 21.sp else 20.sp,
            letterSpacing = if (isBangla) 0.sp else 0.1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = if (isBangla) 23.sp else 22.sp,
            letterSpacing = if (isBangla) 0.sp else 0.25.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 13.5.sp,
            lineHeight = if (isBangla) 21.sp else 19.sp,
            letterSpacing = if (isBangla) 0.sp else 0.2.sp
        ),
        bodySmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = if (isBangla) 18.sp else 16.sp,
            letterSpacing = if (isBangla) 0.sp else 0.3.sp
        ),
        labelLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = if (isBangla) 21.sp else 18.sp,
            letterSpacing = if (isBangla) 0.sp else 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = if (isBangla) 18.sp else 15.sp,
            letterSpacing = if (isBangla) 0.sp else 0.4.sp
        ),
        labelSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = if (isBangla) 16.sp else 14.sp,
            letterSpacing = if (isBangla) 0.sp else 0.4.sp
        )
    )
}

val Typography = getAppTypography(isBangla = false)

// Alias for components referencing AlataFontFamily
val AlataFontFamily: FontFamily = FontFamily.Default
