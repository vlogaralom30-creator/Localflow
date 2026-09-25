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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.outlined.Home
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
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanAccentLight
import com.example.ui.theme.LiquidCyanAccent
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextMutedLight
import com.example.viewmodel.NavTab

@Composable
fun LocalFlowBottomBar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val items = listOf(
        Triple(NavTab.HOME, "Home", Pair(Icons.Filled.Home, Icons.Outlined.Home)),
        Triple(NavTab.SHORTS, "Shorts", Pair(Icons.Filled.PlayCircle, Icons.Outlined.PlayCircle)),
        Triple(NavTab.LIBRARY, "Library", Pair(Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary)),
        Triple(NavTab.ALBUMS, "Albums", Pair(Icons.Filled.FolderSpecial, Icons.Outlined.FolderSpecial)),
        Triple(NavTab.SETTINGS, "Settings", Pair(Icons.Filled.Settings, Icons.Outlined.Settings))
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("bottom_nav_bar"),
        contentAlignment = Alignment.Center
    ) {
        // Floating Frosted Glass Capsule Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isDark) 16.dp else 8.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = if (isDark) Color(0x6600E5FF) else Color(0x3300B4D8),
                    ambientColor = if (isDark) Color(0x40000000) else Color(0x1A000000)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(
                                Color(0x38FFFFFF),
                                Color(0x2E131E30),
                                Color(0x520A0F1A)
                            )
                        } else {
                            listOf(
                                Color(0xF2FFFFFF),
                                Color(0xE6F8FAFC),
                                Color(0xD9F1F5F9)
                            )
                        }
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(
                                Color(0x7AFFFFFF),
                                Color(0x22FFFFFF),
                                Color(0x4400E5FF)
                            )
                        } else {
                            listOf(
                                Color(0xFFFFFFFF),
                                Color(0x80CBD5E1),
                                Color(0x4000B4D8)
                            )
                        }
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (tab, label, icons) ->
                val isSelected = currentTab == tab
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()

                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.88f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "tab_scale_${tab.name}"
                )

                val selectedColor = if (isDark) LiquidCyanAccent else CyanAccentLight
                val unselectedColor = if (isDark) TextMuted else TextMutedLight

                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) selectedColor else unselectedColor,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "tab_color_${tab.name}"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            brush = if (isSelected) {
                                if (isDark) {
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0x4D00E5FF),
                                            Color(0x2400E5FF),
                                            Color(0x150077B6)
                                        )
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0x3800B4D8),
                                            Color(0x1F00B4D8),
                                            Color(0x1038BDF8)
                                        )
                                    )
                                }
                            } else {
                                Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                            }
                        )
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    width = 1.dp,
                                    brush = Brush.verticalGradient(
                                        if (isDark) {
                                            listOf(Color(0x80FFFFFF), Color(0x4D00E5FF))
                                        } else {
                                            listOf(Color(0xFFFFFFFF), Color(0x8000B4D8))
                                        }
                                    ),
                                    shape = RoundedCornerShape(22.dp)
                                )
                            } else {
                                Modifier
                            }
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onTabSelected(tab) }
                        )
                        .padding(vertical = 8.dp)
                        .testTag("nav_tab_${tab.name.lowercase()}"),
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
                            modifier = Modifier.size(23.dp)
                        )
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor,
                            letterSpacing = 0.2.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
