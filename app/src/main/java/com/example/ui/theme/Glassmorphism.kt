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
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val LocalLiquidPreset = compositionLocalOf { LiquidGlassPreset.CYAN }

// Backward compatibility color aliases
val DarkBaseBackground = LiquidGlassColors.DarkBaseBackground
val DarkAmoledBlack = LiquidGlassColors.DarkAmoledBlack
val DarkGlassCanvas = LiquidGlassColors.DarkBaseBackground
val DarkGlassPanel = Color(0x33121B2A)
val DarkGlassBorder = Color(0x40FFFFFF)

val LightBaseBackground = LiquidGlassColors.LightBaseBackground
val LightGlassCanvas = LiquidGlassColors.LightBaseBackground
val LightGlassPanel = Color(0xF2FFFFFF)
val LightGlassBorder = Color(0x90CBD5E1)

val CyanLiquidGlow = LiquidGlassColors.CyanGlow
val CyanLiquidDeep = LiquidGlassColors.CyanDeep
val PurpleGlassGlow = LiquidGlassColors.PurpleGlow
val PurpleGlassDeep = LiquidGlassColors.PurpleDeep

// ============================================================================
// 1. ADVANCED LIQUID GLASS MODIFIER
// ============================================================================

/**
 * Applies multi-layer liquid glass styling:
 * - Semi-transparent gradient glass surface
 * - Specular rim highlight (refraction edge)
 * - Subtle inner specular highlight
 */
fun Modifier.liquidGlass(
    shape: Shape,
    isDark: Boolean = true,
    borderColor: Color = if (isDark) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.85f),
    glassAlpha: Float = if (isDark) 0.14f else 0.55f,
    accentGlow: Color? = null,
    borderWidth: Dp = LiquidGlassDimens.BorderWidthStandard
): Modifier = this.drawBehind {
    val outline = shape.createOutline(size, layoutDirection, this)

    // 1. Semi-transparent glass surface background gradient
    val glassBg = Brush.linearGradient(
        colors = if (isDark) {
            listOf(
                Color.White.copy(alpha = glassAlpha),
                Color(0xFF0F172A).copy(alpha = glassAlpha * 0.7f),
                Color(0xFF080C14).copy(alpha = glassAlpha * 0.35f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = glassAlpha),
                Color(0xFFF8FAFC).copy(alpha = glassAlpha * 0.85f),
                Color(0xFFE2E8F0).copy(alpha = glassAlpha * 0.45f)
            )
        },
        start = Offset(0f, 0f),
        end = Offset(size.width, size.height)
    )

    // Optional subtle accent wash
    if (accentGlow != null) {
        val accentWash = Brush.radialGradient(
            colors = listOf(
                accentGlow.copy(alpha = if (isDark) 0.16f else 0.10f),
                Color.Transparent
            ),
            center = Offset(size.width * 0.8f, size.height * 0.2f),
            radius = size.width * 0.8f
        )
        drawOutline(outline = outline, brush = accentWash)
    }

    drawOutline(outline = outline, brush = glassBg)

    // 2. Specular Top/Rim Highlight Gradient
    val specularRim = Brush.verticalGradient(
        colors = listOf(
            borderColor,
            borderColor.copy(alpha = 0.15f),
            accentGlow?.copy(alpha = 0.35f) ?: borderColor.copy(alpha = 0.05f)
        )
    )

    drawOutline(
        outline = outline,
        brush = specularRim,
        style = Stroke(width = borderWidth.toPx())
    )
}

// ============================================================================
// 2. REUSABLE LIQUID GLASS CONTAINERS (Surface, Card)
// ============================================================================

@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LiquidGlassDimens.RadiusCard),
    elevation: Dp = 8.dp,
    accentGlow: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)
    val glow = accentGlow ?: palette.primaryGlow

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = if (isDark) glow.copy(alpha = 0.35f) else Color(0x25000000),
                ambientColor = if (isDark) Color(0x35000000) else Color(0x15000000)
            )
            .clip(shape)
            .liquidGlass(
                shape = shape,
                isDark = isDark,
                accentGlow = glow,
                borderColor = if (isDark) Color.White.copy(alpha = 0.32f) else Color.White.copy(alpha = 0.9f)
            ),
        content = content
    )
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LiquidGlassDimens.RadiusCard),
    onClick: (() -> Unit)? = null,
    elevation: Dp = 6.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) LiquidGlassMotion.PressScaleFactor else 1.0f,
        animationSpec = LiquidGlassMotion.SpringBouncy,
        label = "card_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = if (isDark) Color(0x3500E5FF) else Color(0x20000000)
            )
            .clip(shape)
            .liquidGlass(
                shape = shape,
                isDark = isDark,
                borderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.85f)
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            ),
        content = content
    )
}

// ============================================================================
// 3. LIQUID LENS TOGGLE SWITCH
// ============================================================================

@Composable
fun LiquidLensSwitch(
    isDarkMode: Boolean,
    onModeChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackWidth = 118.dp
    val trackHeight = 44.dp
    val lensSize = 38.dp

    val lensOffset by animateDpAsState(
        targetValue = if (isDarkMode) (trackWidth - lensSize - 3.dp) else 3.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "LensSlideAnimation"
    )

    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)

    Box(
        modifier = modifier
            .width(trackWidth)
            .height(trackHeight)
            .shadow(
                elevation = if (isDarkMode) 8.dp else 4.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = if (isDarkMode) palette.primaryGlow.copy(alpha = 0.45f) else Color(0x30000000),
                ambientColor = Color(0x20000000)
            )
            .clip(RoundedCornerShape(22.dp))
            .liquidGlass(
                shape = RoundedCornerShape(22.dp),
                isDark = isDarkMode,
                accentGlow = palette.primaryGlow
            )
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
                color = if (!isDarkMode) Color(0xFF0F172A) else Color.White.copy(alpha = 0.45f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Dark",
                color = if (isDarkMode) Color.White else Color(0xFF0F172A).copy(alpha = 0.45f),
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
                    elevation = 8.dp,
                    shape = CircleShape,
                    spotColor = if (isDarkMode) palette.primaryGlow.copy(alpha = 0.6f) else Color(0x40000000)
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = if (isDarkMode) {
                            listOf(
                                palette.primaryGlow.copy(alpha = 0.5f),
                                palette.deepAccent.copy(alpha = 0.35f),
                                Color(0x25000000)
                            )
                        } else {
                            listOf(
                                Color(0xFFFFFFFF),
                                Color(0xF2F8FAFC),
                                Color(0xC0CBD5E1)
                            )
                        },
                        radius = 42f
                    )
                )
                .liquidGlass(
                    shape = CircleShape,
                    isDark = isDarkMode,
                    borderColor = if (isDarkMode) Color.White.copy(alpha = 0.95f) else Color.White
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDarkMode) {
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = "Dark Mode Active",
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.LightMode,
                    contentDescription = "Light Mode Active",
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

// Alias for backwards compatibility
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
// 4. SPECULAR GLASS BUTTONS
// ============================================================================

@Composable
fun LiquidGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    icon: (@Composable () -> Unit)? = null
) {
    val isDark = LocalIsDarkTheme.current
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)
    val activeAccent = accentColor ?: palette.primaryGlow

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) LiquidGlassMotion.PressScaleFactor else 1.0f,
        animationSpec = LiquidGlassMotion.SpringBouncy,
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
                shape = RoundedCornerShape(LiquidGlassDimens.RadiusPill),
                spotColor = activeAccent.copy(alpha = 0.45f)
            )
            .clip(RoundedCornerShape(LiquidGlassDimens.RadiusPill))
            .background(
                brush = Brush.horizontalGradient(
                    colors = if (isDark) {
                        listOf(
                            activeAccent.copy(alpha = 0.4f),
                            activeAccent.copy(alpha = 0.18f)
                        )
                    } else {
                        listOf(
                            activeAccent.copy(alpha = 0.85f),
                            palette.deepAccent.copy(alpha = 0.75f)
                        )
                    }
                )
            )
            .liquidGlass(
                shape = RoundedCornerShape(LiquidGlassDimens.RadiusPill),
                isDark = isDark,
                borderColor = activeAccent.copy(alpha = 0.75f)
            )
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
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) LiquidGlassMotion.PressScaleFactor else 1.0f,
        animationSpec = LiquidGlassMotion.SpringBouncy,
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
                    if (isDark) palette.primaryGlow.copy(alpha = 0.55f) else palette.deepAccent.copy(alpha = 0.4f)
                } else {
                    if (isDark) palette.primaryGlow.copy(alpha = 0.25f) else Color(0x20000000)
                }
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isProminent) {
                        if (isDark) {
                            listOf(
                                palette.primaryGlow.copy(alpha = 0.85f),
                                palette.deepAccent.copy(alpha = 0.5f),
                                Color(0x300369A1)
                            )
                        } else {
                            listOf(
                                palette.primaryGlow,
                                palette.deepAccent,
                                Color(0xFF0369A1)
                            )
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
                borderColor = if (isProminent) Color.White.copy(alpha = 0.9f) else (if (isDark) Color.White.copy(alpha = 0.35f) else Color.White)
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
// 5. 3D LIQUID CIRCLE BUTTON
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
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)
    val glow = tintColor ?: palette.primaryGlow

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) LiquidGlassMotion.DeepPressScale else 1.0f,
        animationSpec = LiquidGlassMotion.SpringBouncy,
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
                spotColor = if (isDark) glow.copy(alpha = 0.45f) else Color(0x30000000),
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
                                glow.copy(alpha = 0.35f),
                                Color(0x201E293B),
                                Color(0x350A0F1A)
                            )
                        } else {
                            listOf(
                                glow.copy(alpha = 0.25f),
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
                                glow.copy(alpha = 0.5f)
                            )
                        } else {
                            listOf(
                                Color(0xFFFFFFFF),
                                Color(0x90CBD5E1),
                                glow.copy(alpha = 0.6f)
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
// 6. LIQUID GLASS FILTER CHIP
// ============================================================================

@Composable
fun LiquidGlassFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null
) {
    val isDark = LocalIsDarkTheme.current
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) LiquidGlassMotion.PressScaleFactor else 1.0f,
        animationSpec = LiquidGlassMotion.SpringBouncy,
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
                shape = RoundedCornerShape(LiquidGlassDimens.RadiusPill),
                spotColor = if (selected) (if (isDark) palette.primaryGlow.copy(alpha = 0.6f) else palette.deepAccent.copy(alpha = 0.4f)) else Color(0x15000000)
            )
            .clip(RoundedCornerShape(LiquidGlassDimens.RadiusPill))
            .drawWithContent {
                val w = size.width
                val h = size.height

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = if (selected) {
                            if (isDark) {
                                listOf(
                                    palette.primaryGlow.copy(alpha = 0.8f),
                                    palette.deepAccent.copy(alpha = 0.45f),
                                    Color(0x350369A1)
                                )
                            } else {
                                listOf(
                                    palette.primaryGlow,
                                    palette.deepAccent,
                                    Color(0xFF0369A1)
                                )
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
                            listOf(Color(0xE6FFFFFF), palette.primaryGlow.copy(alpha = 0.8f))
                        } else {
                            listOf(Color(0xFFFFFFFF), palette.primaryGlow.copy(alpha = 0.6f))
                        }
                    } else {
                        if (isDark) {
                            listOf(Color(0x4DFFFFFF), Color(0x15FFFFFF), palette.primaryGlow.copy(alpha = 0.25f))
                        } else {
                            listOf(Color(0xFFFFFFFF), Color(0x90CBD5E1), palette.deepAccent.copy(alpha = 0.35f))
                        }
                    }
                ),
                shape = RoundedCornerShape(LiquidGlassDimens.RadiusPill)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(6.dp))
            }
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
}

// ============================================================================
// 7. LIQUID GLASS SEARCH FIELD
// ============================================================================

@Composable
fun LiquidGlassSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onSearch: (() -> Unit)? = null,
    onClear: (() -> Unit)? = null
) {
    val isDark = LocalIsDarkTheme.current
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val glowAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.6f else 0.2f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "search_glow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(
                elevation = if (isFocused) 8.dp else 3.dp,
                shape = RoundedCornerShape(LiquidGlassDimens.RadiusCapsule),
                spotColor = palette.primaryGlow.copy(alpha = glowAlpha)
            )
            .clip(RoundedCornerShape(LiquidGlassDimens.RadiusCapsule))
            .liquidGlass(
                shape = RoundedCornerShape(LiquidGlassDimens.RadiusCapsule),
                isDark = isDark,
                accentGlow = if (isFocused) palette.primaryGlow else null,
                borderColor = if (isFocused) palette.primaryGlow.copy(alpha = 0.9f) else (if (isDark) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.8f))
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = if (isFocused) palette.primaryGlow else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    interactionSource = interactionSource,
                    textStyle = TextStyle(
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(palette.primaryGlow),
                    keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("liquid_search_input")
                )
            }

            if (value.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onValueChange("")
                        onClear?.invoke()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ============================================================================
// 8. LIQUID GLASS SNACKBAR / TOAST
// ============================================================================

@Composable
fun LiquidGlassSnackbar(
    message: String,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(LiquidGlassDimens.RadiusPill),
                spotColor = palette.primaryGlow.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(LiquidGlassDimens.RadiusPill))
            .liquidGlass(
                shape = RoundedCornerShape(LiquidGlassDimens.RadiusPill),
                isDark = isDark,
                accentGlow = palette.primaryGlow,
                borderColor = palette.primaryGlow.copy(alpha = 0.8f)
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(palette.primaryGlow)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                color = if (isDark) Color.White else Color(0xFF0F172A),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ============================================================================
// 9. AMBIENT LIQUID BACKDROP (Slow animated aurora refractions)
// ============================================================================

@Composable
fun AmbientLiquidBackdrop(
    modifier: Modifier = Modifier,
    preset: LiquidGlassPreset = LocalLiquidPreset.current,
    content: @Composable () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val palette = getPaletteForPreset(preset)

    val infiniteTransition = rememberInfiniteTransition(label = "liquid_aurora")

    val phaseX by infiniteTransition.animateFloat(
        initialValue = -35f,
        targetValue = 45f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase_x"
    )

    val phaseY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 65f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase_y"
    )

    val bgColor = if (isDark) LiquidGlassColors.DarkBaseBackground else LiquidGlassColors.LightBaseBackground

    Box(
        modifier = modifier
            .background(bgColor)
            .drawBehind {
                val canvasWidth = size.width
                val canvasHeight = size.height

                if (isDark) {
                    // 1. Primary Glow Blob (Top Right)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                palette.primaryGlow.copy(alpha = 0.18f),
                                palette.deepAccent.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = Offset(canvasWidth * 0.85f + phaseX, canvasHeight * 0.15f + phaseY),
                            radius = canvasWidth * 0.8f
                        )
                    )

                    // 2. Secondary Refraction Blob (Bottom Left)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PurpleGlassGlow.copy(alpha = 0.14f),
                                PurpleGlassDeep.copy(alpha = 0.06f),
                                Color.Transparent
                            ),
                            center = Offset(canvasWidth * 0.15f - phaseX, canvasHeight * 0.7f - phaseY),
                            radius = canvasWidth * 0.85f
                        )
                    )

                    // 3. Center subtle refraction pool
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                palette.primaryGlow.copy(alpha = 0.07f),
                                Color.Transparent
                            ),
                            center = Offset(canvasWidth * 0.5f, canvasHeight * 0.45f + phaseY * 0.4f),
                            radius = canvasWidth * 0.55f
                        )
                    )
                } else {
                    // Light Mode - Soft Ice Azure & Lavender Pools
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                palette.primaryGlow.copy(alpha = 0.22f),
                                palette.deepAccent.copy(alpha = 0.10f),
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
                            center = Offset(canvasWidth * 0.15f - phaseX, canvasHeight * 0.7f - phaseY),
                            radius = canvasWidth * 0.85f
                        )
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                palette.deepAccent.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            center = Offset(canvasWidth * 0.5f, canvasHeight * 0.45f + phaseY * 0.4f),
                            radius = canvasWidth * 0.6f
                        )
                    )
                }
            }
    ) {
        content()
    }
}
