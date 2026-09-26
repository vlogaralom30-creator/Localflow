package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LocalLiquidPreset
import com.example.ui.theme.getPaletteForPreset
import com.example.viewmodel.NavTab

@Composable
fun LocalFlowBottomBar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)

    val items = listOf(
        Triple(NavTab.HOME, "Home", Pair(Icons.Filled.Home, Icons.Outlined.Home)),
        Triple(NavTab.SHORTS, "Shorts", Pair(Icons.Filled.PlayCircle, Icons.Outlined.PlayCircle)),
        Triple(NavTab.MUSIC, "Music", Pair(Icons.Filled.MusicNote, Icons.Outlined.MusicNote)),
        Triple(NavTab.LIBRARY, "Library", Pair(Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary)),
        Triple(NavTab.ALBUMS, "Albums", Pair(Icons.Filled.FolderSpecial, Icons.Outlined.FolderSpecial)),
        Triple(NavTab.SETTINGS, "Settings", Pair(Icons.Filled.Settings, Icons.Outlined.Settings))
    )

    val selectedIndex = items.indexOfFirst { it.first == currentTab }.coerceAtLeast(0)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("bottom_nav_bar"),
        contentAlignment = Alignment.Center
    ) {
        val capsuleShape = RoundedCornerShape(26.dp)

        // Outer Frosted Glass Container with specular top rim light and depth shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = if (isDark) 16.dp else 10.dp,
                    shape = capsuleShape,
                    spotColor = if (isDark) palette.primaryGlow.copy(alpha = 0.35f) else Color(0x30000000),
                    ambientColor = if (isDark) Color(0x3A000000) else Color(0x14000000)
                )
                .clip(capsuleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(
                                Color(0xE00F172A),
                                Color(0xF2070D18)
                            )
                        } else {
                            listOf(
                                Color(0xF4FFFFFF),
                                Color(0xEEF1F5F9)
                            )
                        }
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(
                                Color.White.copy(alpha = 0.25f),
                                palette.primaryGlow.copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.04f)
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.90f),
                                palette.primaryGlow.copy(alpha = 0.20f),
                                Color(0x2094A3B8)
                            )
                        }
                    ),
                    shape = capsuleShape
                )
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                val totalWidth = maxWidth
                val tabWidth = totalWidth / items.size

                // Smooth Sliding Liquid Glass Capsule Indicator (Shift Animation)
                val indicatorOffset by animateFloatAsState(
                    targetValue = selectedIndex.toFloat(),
                    animationSpec = spring(
                        dampingRatio = 0.78f,
                        stiffness = 380f
                    ),
                    label = "tab_indicator_offset"
                )

                val pillShape = RoundedCornerShape(20.dp)

                Box(
                    modifier = Modifier
                        .offset(x = tabWidth * indicatorOffset)
                        .width(tabWidth)
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp, vertical = 5.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = pillShape,
                            spotColor = palette.primaryGlow.copy(alpha = if (isDark) 0.55f else 0.35f)
                        )
                        .clip(pillShape)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = if (isDark) {
                                    listOf(
                                        palette.primaryGlow.copy(alpha = 0.28f),
                                        palette.deepAccent.copy(alpha = 0.14f)
                                    )
                                } else {
                                    listOf(
                                        palette.primaryGlow.copy(alpha = 0.20f),
                                        palette.deepAccent.copy(alpha = 0.10f)
                                    )
                                }
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = if (isDark) 0.40f else 0.65f),
                                    palette.primaryGlow.copy(alpha = 0.15f)
                                )
                            ),
                            shape = pillShape
                        )
                )

                // Tab items laid out horizontally
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEach { (tab, label, icons) ->
                        val isSelected = currentTab == tab
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()

                        val tabScale by animateFloatAsState(
                            targetValue = if (isPressed) 0.88f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "tab_press_scale_${tab.name}"
                        )

                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.15f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "icon_scale_${tab.name}"
                        )

                        val activeColor = if (isDark) palette.primaryGlow else palette.deepAccent
                        val inactiveColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)

                        val contentColor by animateColorAsState(
                            targetValue = if (isSelected) activeColor else inactiveColor,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            label = "tab_color_${tab.name}"
                        )

                        val dotScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.0f else 0.0f,
                            animationSpec = spring(dampingRatio = 0.65f, stiffness = 420f),
                            label = "dot_scale_${tab.name}"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .graphicsLayer {
                                    scaleX = tabScale
                                    scaleY = tabScale
                                }
                                .clip(pillShape)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = { onTabSelected(tab) }
                                )
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) icons.first else icons.second,
                                    contentDescription = label,
                                    tint = contentColor,
                                    modifier = Modifier
                                        .size(21.dp)
                                        .graphicsLayer {
                                            scaleX = iconScale
                                            scaleY = iconScale
                                        }
                                )
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = contentColor,
                                    letterSpacing = 0.15.sp,
                                    maxLines = 1,
                                    modifier = Modifier.padding(top = 1.dp)
                                )
                                // Active glowing indicator dot
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .graphicsLayer {
                                            scaleX = dotScale
                                            scaleY = dotScale
                                            alpha = dotScale
                                        }
                                        .clip(CircleShape)
                                        .background(activeColor)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
