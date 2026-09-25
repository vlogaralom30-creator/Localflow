package com.example.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized Design Tokens for Liquid Glass UI Design System
 */

// ============================================================================
// 1. LIQUID COLOR PRESETS
// ============================================================================

enum class LiquidGlassPreset(val displayName: String) {
    CYAN("Cyan Liquid"),
    PURPLE("Neon Purple"),
    EMERALD("Emerald Glass"),
    AMBER("Solar Amber")
}

data class LiquidPalette(
    val primaryGlow: Color,
    val deepAccent: Color,
    val containerGlow: Color,
    val borderGlint: Color
)

fun getPaletteForPreset(preset: LiquidGlassPreset): LiquidPalette {
    return when (preset) {
        LiquidGlassPreset.CYAN -> LiquidPalette(
            primaryGlow = Color(0xFF00E5FF),
            deepAccent = Color(0xFF0284C7),
            containerGlow = Color(0x3300E5FF),
            borderGlint = Color(0x9900E5FF)
        )
        LiquidGlassPreset.PURPLE -> LiquidPalette(
            primaryGlow = Color(0xFFA855F7),
            deepAccent = Color(0xFF7E22CE),
            containerGlow = Color(0x33A855F7),
            borderGlint = Color(0x99A855F7)
        )
        LiquidGlassPreset.EMERALD -> LiquidPalette(
            primaryGlow = Color(0xFF10B981),
            deepAccent = Color(0xFF047857),
            containerGlow = Color(0x3310B981),
            borderGlint = Color(0x9910B981)
        )
        LiquidGlassPreset.AMBER -> LiquidPalette(
            primaryGlow = Color(0xFFF59E0B),
            deepAccent = Color(0xFFB45309),
            containerGlow = Color(0x33F59E0B),
            borderGlint = Color(0x99F59E0B)
        )
    }
}

// ============================================================================
// 2. BASE SURFACES & CANVAS COLORS
// ============================================================================

object LiquidGlassColors {
    // Dark Liquid AMOLED (Primary)
    val DarkBaseBackground = Color(0xFF0D0F14) // Deep Space Dark
    val DarkAmoledBlack = Color(0xFF000000)   // AMOLED True Black
    val DarkGlassSurfaceStart = Color(0x1FFFFFFF) // ~12% White
    val DarkGlassSurfaceEnd = Color(0x0AFFFFFF)   // ~4% White
    val DarkGlassBorderStart = Color(0x59FFFFFF)  // ~35% White
    val DarkGlassBorderEnd = Color(0x0DFFFFFF)    // ~5% White

    // Light Crystal Glass
    val LightBaseBackground = Color(0xFFEBF1F5) // Soft Ice White
    val LightGlassSurfaceStart = Color(0x99FFFFFF) // ~60% White
    val LightGlassSurfaceEnd = Color(0x33FFFFFF)   // ~20% White
    val LightGlassBorderStart = Color(0xE6FFFFFF)  // ~90% White
    val LightGlassBorderEnd = Color(0x4DFFFFFF)    // ~30% White

    // Default Accents
    val CyanGlow = Color(0xFF00E5FF)
    val CyanDeep = Color(0xFF0284C7)
    val PurpleGlow = Color(0xFFA855F7)
    val PurpleDeep = Color(0xFF7E22CE)
    val EmeraldGlow = Color(0xFF10B981)
    val AmberGlow = Color(0xFFF59E0B)
}

// ============================================================================
// 3. DIMENSIONS, RADII & STROKES
// ============================================================================

object LiquidGlassDimens {
    val BorderWidthThin: Dp = 1.0.dp
    val BorderWidthStandard: Dp = 1.5.dp
    val BorderWidthThick: Dp = 2.0.dp

    val RadiusSmall: Dp = 10.dp
    val RadiusMedium: Dp = 16.dp
    val RadiusCard: Dp = 18.dp
    val RadiusPill: Dp = 22.dp
    val RadiusLarge: Dp = 28.dp
    val RadiusCapsule: Dp = 32.dp

    val MinTouchTarget: Dp = 48.dp
}

// ============================================================================
// 4. ANIMATION PHYSICS CONSTANTS
// ============================================================================

object LiquidGlassMotion {
    const val PressScaleFactor: Float = 0.96f
    const val SubtlePressScale: Float = 0.98f
    const val DeepPressScale: Float = 0.92f

    val SpringBouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val SpringFast = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
}
