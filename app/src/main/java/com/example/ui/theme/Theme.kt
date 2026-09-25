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

private fun getDarkColorScheme(palette: LiquidPalette) = darkColorScheme(
    primary = palette.primaryGlow,
    onPrimary = AmoledBlack,
    primaryContainer = RawCardElevated,
    onPrimaryContainer = palette.primaryGlow,
    secondary = palette.deepAccent,
    onSecondary = TextPrimaryDark,
    secondaryContainer = RawCardDark,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = SuccessGreen,
    onTertiary = AmoledBlack,
    background = DarkBaseBackground,
    onBackground = TextPrimaryDark,
    surface = RawSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = RawCardDark,
    onSurfaceVariant = TextSecondaryDark,
    surfaceTint = palette.primaryGlow,
    outline = RawBorderDark,
    outlineVariant = RawDividerDark,
    error = ErrorRed,
    onError = TextPrimaryDark
)

private fun getLightColorScheme(palette: LiquidPalette) = lightColorScheme(
    primary = palette.deepAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F7FA),
    onPrimaryContainer = Color(0xFF006064),
    secondary = palette.primaryGlow,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEFF6FF),
    onSecondaryContainer = Color(0xFF1E3A8A),
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    background = LightBaseBackground,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = TextSecondaryLight,
    surfaceTint = palette.deepAccent,
    outline = BorderLightMode,
    outlineVariant = DividerLightMode,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun NaxxivoTheme(
    darkTheme: Boolean = true,
    preset: LiquidGlassPreset = LiquidGlassPreset.CYAN,
    content: @Composable () -> Unit
) {
    val palette = getPaletteForPreset(preset)
    val colorScheme = if (darkTheme) getDarkColorScheme(palette) else getLightColorScheme(palette)
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalIsDarkTheme provides darkTheme,
        LocalLiquidPreset provides preset
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
