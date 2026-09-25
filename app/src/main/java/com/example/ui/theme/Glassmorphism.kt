package com.example.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================================
// 1. DESIGN TOKENS & COLOR PALETTE (From Integration Guide)
// ============================================================================

// Dark Liquid AMOLED Theme
val DarkBaseBackground = Color(0xFF0D0F14) // Deep Space Dark
val DarkAmoledBlack = Color(0xFF000000)
val DarkGlassCanvas = Color(0xFF0D0F14)
val DarkGlassPanel = Color(0x33121B2A)
val DarkGlassBorder = Color(0x40FFFFFF)

// Light Crystal Glass Theme
val LightBaseBackground = Color(0xFFEBF1F5) // Soft Ice White
val LightGlassCanvas = Color(0xFFEBF1F5)
val LightGlassPanel = Color(0xF2FFFFFF)
val LightGlassBorder = Color(0x90CBD5E1)

// Liquid Glow Accents
val CyanLiquidGlow = Color(0xFF00E5FF)
val CyanLiquidDeep = Color(0xFF0284C7)
val PurpleGlassGlow = Color(0xFFA855F7)
val PurpleGlassDeep = Color(0xFF7E22CE)

// ============================================================================
// 2. CUSTOM LIQUID GLASS MODIFIER (Section 2.A of Integration Guide)
// ============================================================================

fun Modifier.liquidGlass(
    shape: Shape,
    isDark: Boolean = true,
    borderColor: Color = if (isDark) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.8f),
    glassAlpha: Float = if (isDark) 0.15f else 0.45f
): Modifier = this.drawBehind {
    val outline = shape.createOutline(size, layoutDirection, this)

    // Glass Surface Background Gradient
    val glassBg = Brush.linearGradient(
        colors = if (isDark) {
            listOf(
                Color.White.copy(alpha = glassAlpha),
                Color.White.copy(alpha = glassAlpha * 0.3f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = glassAlpha),
                Color(0xFFE2E8F0).copy(alpha = glassAlpha * 0.4f)
            )
        }
    )

    // Specular Rim Highlight (Refraction edge)
    val specularRim = Brush.verticalGradient(
        colors = listOf(
            borderColor,
            borderColor.copy(alpha = 0.05f)
        )
    )

    drawOutline(
        outline = outline,
        brush = glassBg
    )

    drawOutline(
        outline = outline,
        brush = specularRim,
        style = Stroke(width = 1.5.dp.toPx())
    )
}

// ============================================================================
// 3. LIQUID LENS TOGGLE SWITCH (Section 2.B of Integration Guide & Image #3)
// ============================================================================

@Composable
fun LiquidLensSwitch(
    isDarkMode: Boolean,
    onModeChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackWidth = 112.dp
    val trackHeight = 42.dp
    val lensSize = 36.dp

    val lensOffset by animateDpAsState(
        targetValue = if (isDarkMode) (trackWidth - lensSize - 3.dp) else 3.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "LensSlideAnimation"
    )

    Box(
        modifier = modifier
            .width(trackWidth)
            .height(trackHeight)
            .shadow(
                elevation = if (isDarkMode) 8.dp else 4.dp,
                shape = RoundedCornerShape(21.dp),
                spotColor = if (isDarkMode) Color(0x6000E5FF) else Color(0x33000000),
                ambientColor = Color(0x20000000)
            )
            .clip(RoundedCornerShape(21.dp))
            .liquidGlass(RoundedCornerShape(21.dp), isDark = isDarkMode)
            .clickable { onModeChanged(!isDarkMode) }
            .padding(3.dp)
            .testTag("liquid_shift_theme_toggle"),
        contentAlignment = Alignment.CenterStart
    ) {
        // Track Labels
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Light",
                color = if (!isDarkMode) Color(0xFF0F172A) else Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Dark",
                color = if (isDarkMode) Color.White else Color(0xFF0F172A).copy(alpha = 0.4f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Sliding High-Refraction Glass Lens Bulb
        Box(
            modifier = Modifier
                .offset(x = lensOffset)
                .size(lensSize)
                .shadow(
                    elevation = 6.dp,
                    shape = CircleShape,
                    spotColor = if (isDarkMode) Color(0x8000E5FF) else Color(0x40000000)
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = if (isDarkMode) {
                            listOf(
                                Color(0x7038BDF8),
                                Color(0x350284C7),
                                Color(0x20000000)
                            )
                        } else {
                            listOf(
                                Color(0xFFFFFFFF),
                                Color(0xE0F8FAFC),
                                Color(0xB0CBD5E1)
                            )
                        },
                        radius = 40f
                    )
                )
                .liquidGlass(
                    shape = CircleShape,
                    isDark = isDarkMode,
                    borderColor = if (isDarkMode) Color.White.copy(alpha = 0.9f) else Color.White
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDarkMode) {
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = "Dark Mode Active",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.LightMode,
                    contentDescription = "Light Mode Active",
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// Alias for compatibility
@Composable
fun LiquidShiftLightDarkToggle(
    isDark: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidLensSwitch(
        isDarkMode = isDark,
        onModeChanged = { onToggle() },
        modifier = modifier
    )
}

// ============================================================================
// 4. SPECULAR GLASS BUTTONS (Section 2.C of Integration Guide & Images #1, #2)
// ============================================================================

@Composable
fun LiquidGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = CyanLiquidGlow,
    icon: (@Composable () -> Unit)? = null
) {
    val isDark = LocalIsDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "btn_press_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(48.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = accentColor.copy(alpha = 0.4f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = if (isDark) {
                        listOf(
                            accentColor.copy(alpha = 0.35f),
                            accentColor.copy(alpha = 0.15f)
                        )
                    } else {
                        listOf(
                            Color(0xFF00B4D8).copy(alpha = 0.85f),
                            Color(0xFF0284C7).copy(alpha = 0.75f)
                        )
                    }
                )
            )
            .liquidGlass(RoundedCornerShape(24.dp), isDark = isDark, borderColor = accentColor.copy(alpha = 0.7f))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon?.invoke()
            if (icon != null) Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

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
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "liquid_btn_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isProminent) 10.dp else 4.dp,
                shape = shape,
                spotColor = if (isProminent) {
                    if (isDark) CyanLiquidGlow.copy(alpha = 0.5f) else Color(0x400284C7)
                } else {
                    if (isDark) CyanLiquidGlow.copy(alpha = 0.25f) else Color(0x20000000)
                }
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isProminent) {
                        if (isDark) {
                            listOf(Color(0x8000E5FF), Color(0x450284C7), Color(0x300369A1))
                        } else {
                            listOf(Color(0xFF00B4D8), Color(0xFF0284C7), Color(0xFF0369A1))
                        }
                    } else {
                        if (isDark) {
                            listOf(Color(0x40FFFFFF), Color(0x18FFFFFF), Color(0x280E1726))
                        } else {
                            listOf(Color(0xFFFFFFFF), Color(0xEFF8FAFC), Color(0xD8E2E8F0))
                        }
                    }
                )
            )
            .liquidGlass(
                shape = shape,
                isDark = isDark,
                borderColor = if (isProminent) Color.White.copy(alpha = 0.85f) else (if (isDark) Color.White.copy(alpha = 0.35f) else Color.White)
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

// ============================================================================
// 5. 3D LIQUID CIRCLE BUTTON (Image #2 Actions: Search, Refresh, Pick)
// ============================================================================

@Composable
fun LiquidGlassCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    tintColor: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.89f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "droplet_btn_scale"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isDark) 6.dp else 4.dp,
                shape = CircleShape,
                spotColor = tintColor ?: if (isDark) Color(0x5000E5FF) else Color(0x30000000),
                ambientColor = Color(0x18000000)
            )
            .clip(CircleShape)
            .drawWithContent {
                val w = this.size.width
                val h = this.size.height

                // Spherical Gradient Body
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = if (isDark) {
                            listOf(
                                tintColor?.copy(alpha = 0.4f) ?: Color(0x45FFFFFF),
                                Color(0x201E293B),
                                Color(0x350A0F1A)
                            )
                        } else {
                            listOf(
                                tintColor?.copy(alpha = 0.35f) ?: Color(0xFFFFFFFF),
                                Color(0xF0F8FAFC),
                                Color(0xD8E2E8F0)
                            )
                        },
                        center = Offset(w * 0.35f, h * 0.35f),
                        radius = w * 0.7f
                    )
                )

                drawContent()

                // Top-Left Caustic Glare
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xCCFFFFFF),
                            Color(0x30FFFFFF),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.28f, h * 0.28f),
                        radius = w * 0.26f
                    )
                )

                // Specular rim
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = if (isDark) {
                            listOf(
                                Color(0xD9FFFFFF),
                                Color(0x20FFFFFF),
                                tintColor?.copy(alpha = 0.5f) ?: Color(0x4000E5FF)
                            )
                        } else {
                            listOf(
                                Color(0xFFFFFFFF),
                                Color(0x90CBD5E1),
                                tintColor?.copy(alpha = 0.6f) ?: Color(0x6000B4D8)
                            )
                        },
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    ),
                    radius = (w / 2f) - 0.75.dp.toPx(),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

// ============================================================================
// 6. LIQUID GLASS FILTER CHIP (Image #2 Tabs & Filter Pills)
// ============================================================================

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
        targetValue = if (isPressed) 0.93f else 1.0f,
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
                spotColor = if (selected) (if (isDark) Color(0x6000E5FF) else Color(0x4000B4D8)) else Color(0x15000000)
            )
            .clip(RoundedCornerShape(22.dp))
            .drawWithContent {
                val w = size.width
                val h = size.height

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = if (selected) {
                            if (isDark) {
                                listOf(Color(0x8000E5FF), Color(0x450284C7), Color(0x350369A1))
                            } else {
                                listOf(Color(0xFF00B4D8), Color(0xFF0284C7), Color(0xFF0369A1))
                            }
                        } else {
                            if (isDark) {
                                listOf(Color(0x38FFFFFF), Color(0x181E293B), Color(0x2E0B1322))
                            } else {
                                listOf(Color(0xFFFFFFFF), Color(0xF0F8FAFC), Color(0xDEE2E8F0))
                            }
                        }
                    ),
                    cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx())
                )

                drawContent()

                // Top Specular Highlight Line
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x99FFFFFF),
                            Color.Transparent
                        )
                    ),
                    start = Offset(w * 0.2f, 1.dp.toPx()),
                    end = Offset(w * 0.8f, 1.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .border(
                width = if (selected) 1.2.dp else 1.dp,
                brush = Brush.verticalGradient(
                    colors = if (selected) {
                        if (isDark) {
                            listOf(Color(0xE6FFFFFF), Color(0x8000E5FF))
                        } else {
                            listOf(Color(0xFFFFFFFF), Color(0x8038BDF8))
                        }
                    } else {
                        if (isDark) {
                            listOf(Color(0x4DFFFFFF), Color(0x15FFFFFF), Color(0x2800E5FF))
                        } else {
                            listOf(Color(0xFFFFFFFF), Color(0x90CBD5E1), Color(0x5000B4D8))
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
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) {
                if (isDark) Color.Black else Color.White
            } else {
                if (isDark) Color(0xFFE2E8F0) else Color(0xFF0F172A)
            }
        )
    }
}

// ============================================================================
// 7. AMBIENT LIQUID BACKDROP (Liquid Aurora Blobs: #00E5FF & #A855F7)
// ============================================================================

@Composable
fun AmbientLiquidBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_aurora")

    val phaseX by infiniteTransition.animateFloat(
        initialValue = -40f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase_x"
    )

    val phaseY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 80f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase_y"
    )

    val bgColor = if (isDark) DarkGlassCanvas else LightGlassCanvas

    Box(
        modifier = modifier
            .background(bgColor)
            .drawBehind {
                val canvasWidth = size.width
                val canvasHeight = size.height

                if (isDark) {
                    // 1. Cyan Liquid Blob (Top Right)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                CyanLiquidGlow.copy(alpha = 0.18f),
                                CyanLiquidDeep.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = Offset(canvasWidth * 0.85f + phaseX, canvasHeight * 0.15f + phaseY),
                            radius = canvasWidth * 0.75f
                        )
                    )

                    // 2. Neon Purple Glass Blob (Bottom Left)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PurpleGlassGlow.copy(alpha = 0.16f),
                                PurpleGlassDeep.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = Offset(canvasWidth * 0.15f - phaseX, canvasHeight * 0.65f - phaseY),
                            radius = canvasWidth * 0.85f
                        )
                    )

                    // 3. Center subtle refraction pool
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                CyanLiquidGlow.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = Offset(canvasWidth * 0.5f, canvasHeight * 0.45f + phaseY * 0.5f),
                            radius = canvasWidth * 0.5f
                        )
                    )
                } else {
                    // Light Mode - Soft Ice Azure & Lavender Pools
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x2800B4D8),
                                Color(0x1238BDF8),
                                Color.Transparent
                            ),
                            center = Offset(canvasWidth * 0.85f + phaseX, canvasHeight * 0.15f + phaseY),
                            radius = canvasWidth * 0.85f
                        )
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x22C084FC),
                                Color(0x10818CF8),
                                Color.Transparent
                            ),
                            center = Offset(canvasWidth * 0.15f - phaseX, canvasHeight * 0.65f - phaseY),
                            radius = canvasWidth * 0.85f
                        )
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x180284C7),
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
