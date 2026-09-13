package com.tiantian.school.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3EAFF),
    onPrimaryContainer = BrandBlueDeep,
    secondary = BrandOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEEDC),
    onSecondaryContainer = Color(0xFF8A4B00),
    tertiary = BrandCyan,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight,
    outlineVariant = OutlineLight,
    error = DangerRed,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FA6FF),
    onPrimary = Color(0xFF0B1330),
    primaryContainer = Color(0xFF2A3A78),
    onPrimaryContainer = Color(0xFFDCE4FF),
    secondary = Color(0xFFFFB877),
    onSecondary = Color(0xFF3A1F00),
    secondaryContainer = Color(0xFF573400),
    onSecondaryContainer = Color(0xFFFFE0C2),
    tertiary = Color(0xFF7FD4FF),
    onTertiary = Color(0xFF00303F),
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
    outlineVariant = OutlineDark,
    error = Color(0xFFFF8A8A),
    onError = Color(0xFF3A0000)
)

@Composable
fun TianTianTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
