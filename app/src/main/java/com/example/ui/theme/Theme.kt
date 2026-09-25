package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalIsDarkTheme = compositionLocalOf { true }

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) TextPrimaryDark else TextPrimaryLight

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) TextSecondaryDark else TextSecondaryLight

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) TextMutedDark else TextMutedLight

val CardDark: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) RawCardDark else CardLight

val CardElevated: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) RawCardElevated else CardLightElevated

val BorderDark: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) RawBorderDark else BorderLightMode

val DividerDark: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) RawDividerDark else DividerLightMode

val SurfaceDark: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) RawSurfaceDark else SurfaceLight

private val NaxxivoDarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = AmoledBlack,
    primaryContainer = RawCardElevated,
    onPrimaryContainer = CyanAccent,
    secondary = BlueAccent,
    onSecondary = TextPrimaryDark,
    secondaryContainer = RawCardDark,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = SuccessGreen,
    onTertiary = AmoledBlack,
    background = AmoledBlack,
    onBackground = TextPrimaryDark,
    surface = RawSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = RawCardDark,
    onSurfaceVariant = TextSecondaryDark,
    surfaceTint = CyanAccent,
    outline = RawBorderDark,
    outlineVariant = RawDividerDark,
    error = ErrorRed,
    onError = TextPrimaryDark
)

private val NaxxivoLightColorScheme = lightColorScheme(
    primary = CyanAccentLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F7FA),
    onPrimaryContainer = Color(0xFF006064),
    secondary = BlueAccentLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEFF6FF),
    onSecondaryContainer = Color(0xFF1E3A8A),
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    background = CanvasLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = TextSecondaryLight,
    surfaceTint = CyanAccentLight,
    outline = BorderLightMode,
    outlineVariant = DividerLightMode,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun NaxxivoTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NaxxivoDarkColorScheme else NaxxivoLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val bgArgb = (if (darkTheme) AmoledBlack else CanvasLight).toArgb()
                window.statusBarColor = bgArgb
                window.navigationBarColor = bgArgb
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
