package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LiquidGlassDimens
import com.example.ui.theme.LiquidGlassMotion
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LocalLiquidPreset
import com.example.ui.theme.getPaletteForPreset
import com.example.ui.theme.liquidGlass
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
        Triple(NavTab.LIBRARY, "Library", Pair(Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary)),
        Triple(NavTab.ALBUMS, "Albums", Pair(Icons.Filled.FolderSpecial, Icons.Outlined.FolderSpecial)),
        Triple(NavTab.SETTINGS, "Settings", Pair(Icons.Filled.Settings, Icons.Outlined.Settings))
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("bottom_nav_bar"),
        contentAlignment = Alignment.Center
    ) {
        // Floating Frosted Glass Capsule Bar
        val capsuleShape = RoundedCornerShape(LiquidGlassDimens.RadiusCapsule)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isDark) 16.dp else 8.dp,
                    shape = capsuleShape,
                    spotColor = if (isDark) palette.primaryGlow.copy(alpha = 0.45f) else Color(0x30000000),
                    ambientColor = if (isDark) Color(0x40000000) else Color(0x18000000)
                )
                .clip(capsuleShape)
                .liquidGlass(
                    shape = capsuleShape,
                    isDark = isDark,
                    accentGlow = palette.primaryGlow,
                    borderColor = if (isDark) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.9f)
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
                    targetValue = if (isPressed) LiquidGlassMotion.PressScaleFactor else 1.0f,
                    animationSpec = LiquidGlassMotion.SpringBouncy,
                    label = "tab_scale_${tab.name}"
                )

                val selectedContentColor = if (isDark) palette.primaryGlow else palette.deepAccent
                val unselectedContentColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)

                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) selectedContentColor else unselectedContentColor,
                    animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessMedium),
                    label = "tab_color_${tab.name}"
                )

                val tabPillShape = RoundedCornerShape(LiquidGlassDimens.RadiusPill)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(tabPillShape)
                        .then(
                            if (isSelected) {
                                Modifier.liquidGlass(
                                    shape = tabPillShape,
                                    isDark = isDark,
                                    accentGlow = palette.primaryGlow,
                                    glassAlpha = if (isDark) 0.25f else 0.4f,
                                    borderColor = if (isDark) Color.White.copy(alpha = 0.55f) else palette.primaryGlow.copy(alpha = 0.7f),
                                    borderWidth = 1.2.dp
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
                        .padding(vertical = 7.dp),
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
                            modifier = Modifier.size(22.dp)
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
