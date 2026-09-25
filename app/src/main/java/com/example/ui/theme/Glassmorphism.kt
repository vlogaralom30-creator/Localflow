package com.example.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Liquid Glass Palette (Dark)
val GlassBgTranslucent = Color(0x2E141D2D)
val GlassBgDeep = Color(0x3B0C121E)
val GlassBgInteractive = Color(0x4A1E293B)
val GlassBgActivePill = Color(0x4000E5FF)

val GlassBorderLight = Color(0x40FFFFFF)
val GlassBorderSubtle = Color(0x1FFFFFFF)
val GlassBorderAccent = Color(0x5200E5FF)

val LiquidCyanAccent = Color(0xFF00E5FF)
val LiquidElectricBlue = Color(0xFF2563EB)
val LiquidVioletAccent = Color(0xFF8B5CF6)
val LiquidGlowAmber = Color(0x33FFB020)

/**
 * Interactive Liquid Glass Button with iOS/HyperOS spring bounce effect on press.
 * Seamlessly adapts to Light and Dark liquid themes.
 */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    isProminent: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "glass_button_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isProminent) 8.dp else 3.dp,
                shape = shape,
                spotColor = if (isProminent) (if (isDark) LiquidCyanAccent else Color(0x6600B4D8)) else (if (isDark) Color(0x3300E5FF) else Color(0x20000000))
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isProminent) {
                        if (isDark) {
                            listOf(Color(0x8000E5FF), Color(0x4000B4D8), Color(0x300077B6))
                        } else {
                            listOf(Color(0xFF00B4D8), Color(0xFF0096B4), Color(0xFF0077B6))
                        }
                    } else {
                        if (isDark) {
                            listOf(Color(0x45FFFFFF), Color(0x201E293B), Color(0x350A0E1A))
                        } else {
                            listOf(Color(0xE6FFFFFF), Color(0xB3F1F5F9), Color(0x99E2E8F0))
                        }
                    }
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = if (isProminent) {
                        if (isDark) {
                            listOf(Color(0xB3FFFFFF), Color(0x6600E5FF))
                        } else {
                            listOf(Color(0xFFFFFFFF), Color(0x8000E5FF))
                        }
                    } else {
                        if (isDark) {
                            listOf(Color(0x66FFFFFF), Color(0x20FFFFFF), Color(0x3300E5FF))
                        } else {
                            listOf(Color(0xFFFFFFFF), Color(0x80CBD5E1), Color(0x4000B4D8))
                        }
                    }
                ),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Interactive Liquid Glass Filter Chip with tactile spring bounce.
 * Automatically styles for Dark / Light theme.
 */
@Composable
fun LiquidGlassFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "chip_scale_$label"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (selected) 6.dp else 2.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = if (selected) (if (isDark) LiquidCyanAccent else Color(0x4D00B4D8)) else Color(0x15000000)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = if (selected) {
                        if (isDark) {
                            listOf(Color(0x7300E5FF), Color(0x4000B4D8), Color(0x330077B6))
                        } else {
                            listOf(Color(0xFF00B4D8), Color(0xFF0096B4), Color(0xFF0284C7))
                        }
                    } else {
                        if (isDark) {
                            listOf(Color(0x35FFFFFF), Color(0x24172338), Color(0x3B0C1322))
                        } else {
                            listOf(Color(0xF2FFFFFF), Color(0xD9F8FAFC), Color(0xB3E2E8F0))
                        }
                    }
                )
            )
            .border(
                width = if (selected) 1.2.dp else 0.8.dp,
                brush = Brush.verticalGradient(
                    colors = if (selected) {
                        if (isDark) {
                            listOf(Color(0xE6FFFFFF), Color(0x8000E5FF))
                        } else {
                            listOf(Color(0xFFFFFFFF), Color(0x8038BDF8))
                        }
                    } else {
                        if (isDark) {
                            listOf(Color(0x4DFFFFFF), Color(0x18FFFFFF), Color(0x2B00E5FF))
                        } else {
                            listOf(Color(0xFFFFFFFF), Color(0x60CBD5E1), Color(0x3000B4D8))
                        }
                    }
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
            color = if (selected) {
                if (isDark) Color.Black else Color.White
            } else {
                if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155)
            }
        )
    }
}

/**
 * Animated Ambient Liquid Mesh backdrop simulating fluid light pools.
 * In Dark mode: Deep AMOLED canvas with glowing Cyan & Violet aurora pools.
 * In Light mode: Ultra-clean frosted Pearl canvas with luminous Sky Cyan & Lavender pools.
 */
@Composable
fun AmbientLiquidBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_aurora")

    val phaseX by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase_x"
    )

    val phaseY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase_y"
    )

    val bgColor = if (isDark) Color(0xFF07090E) else Color(0xFFF1F5F9)

    Box(
        modifier = modifier
            .background(bgColor)
            .drawBehind {
                val canvasWidth = size.width
                val canvasHeight = size.height

                if (isDark) {
                    // Liquid Cyan Fluid Pool 1 (Top right)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x2400E5FF),
                                Color(0x1000B4D8),
                                Color.Transparent
                            ),
                            center = Offset(canvasWidth * 0.85f + phaseX, canvasHeight * 0.15f + phaseY),
                            radius = canvasWidth * 0.75f
                        )
                    )

                    // Liquid Violet / Deep Indigo Pool 2 (Bottom left)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x228B5CF6),
                                Color(0x103B82F6),
                                Color.Transparent
                            ),
                            center = Offset(canvasWidth * 0.15f - phaseX, canvasHeight * 0.65f - phaseY),
                            radius = canvasWidth * 0.85f
                        )
                    )

                    // Center subtle highlight
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x1200D9FF),
                                Color.Transparent
                            ),
                            center = Offset(canvasWidth * 0.5f, canvasHeight * 0.45f + phaseY * 0.5f),
                            radius = canvasWidth * 0.5f
                        )
                    )
                } else {
                    // Light mode luminous pastel Cyan pool
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x2B00B4D8),
                                Color(0x1438BDF8),
                                Color.Transparent
                            ),
                            center = Offset(canvasWidth * 0.85f + phaseX, canvasHeight * 0.15f + phaseY),
                            radius = canvasWidth * 0.85f
                        )
                    )

                    // Light mode luminous lavender pool
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x24C084FC),
                                Color(0x10818CF8),
                                Color.Transparent
                            ),
                            center = Offset(canvasWidth * 0.15f - phaseX, canvasHeight * 0.65f - phaseY),
                            radius = canvasWidth * 0.85f
                        )
                    )

                    // Light mode soft azure center
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x1A0284C7),
                                Color.Transparent
                            ),
                            center = Offset(canvasWidth * 0.5f, canvasHeight * 0.45f + phaseY * 0.5f),
                            radius = canvasWidth * 0.6f
                        )
                    )
                }
            }
    ) {
        content()
    }
}
