package com.example.ui.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.theme.LiquidGlassDimens
import com.example.ui.theme.LiquidGlassFilterChip
import com.example.ui.theme.LiquidGlassMotion
import com.example.ui.theme.LiquidGlassPreset
import com.example.ui.theme.LiquidLensSwitch
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LocalLiquidPreset
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.getPaletteForPreset
import com.example.ui.theme.liquidGlass
import com.example.viewmodel.VideoPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: VideoPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val speed by viewModel.playbackSpeed.collectAsStateWithLifecycle()
    val autoPlay by viewModel.isAutoPlayNext.collectAsStateWithLifecycle()
    val rememberPos by viewModel.rememberPosition.collectAsStateWithLifecycle()
    val bgAudio by viewModel.backgroundAudio.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val currentPreset by viewModel.liquidPreset.collectAsStateWithLifecycle()

    val isDark = LocalIsDarkTheme.current
    val palette = getPaletteForPreset(currentPreset)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // 1. Appearance Section
            item {
                Spacer(modifier = Modifier.height(6.dp))
                SectionLabel(title = "Appearance & Glass Theme", color = palette.primaryGlow)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Dark / Light Refraction Switch Card
            item {
                val cardShape = RoundedCornerShape(LiquidGlassDimens.RadiusCard)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(cardShape)
                        .liquidGlass(
                            shape = cardShape,
                            isDark = isDark,
                            accentGlow = palette.primaryGlow
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = if (isDarkTheme) palette.primaryGlow else Color(0xFFF59E0B),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = if (isDarkTheme) "Dark Liquid Theme" else "Light Crystal Theme",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isDarkTheme) "Deep space AMOLED with specular highlights" else "Pearl white glass with subtle refractions",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        LiquidLensSwitch(
                            isDarkMode = isDarkTheme,
                            onModeChanged = { viewModel.toggleTheme() }
                        )
                    }
                }
            }

            // Liquid Glass Accent Preset Selector Card
            item {
                Spacer(modifier = Modifier.height(8.dp))
                val presetShape = RoundedCornerShape(LiquidGlassDimens.RadiusCard)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(presetShape)
                        .liquidGlass(
                            shape = presetShape,
                            isDark = isDark,
                            accentGlow = palette.primaryGlow
                        )
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = palette.primaryGlow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Liquid Accent Preset",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Customizes the ambient backdrop, glowing specular rims, and lens highlights",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(LiquidGlassPreset.values()) { preset ->
                                val isSelected = currentPreset == preset
                                val pPalette = getPaletteForPreset(preset)
                                LiquidGlassFilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setLiquidPreset(preset) },
                                    label = preset.displayName,
                                    icon = {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(androidx.compose.foundation.shape.CircleShape)
                                                .background(pPalette.primaryGlow)
                                        )
                                    },
                                    modifier = Modifier.testTag("preset_${preset.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 2. Player Preferences Section
            item {
                SectionLabel(title = "Player Settings", color = palette.primaryGlow)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                SettingsSwitchRow(
                    icon = Icons.Default.Autorenew,
                    title = "Auto Play",
                    subtitle = "Automatically start video on tap",
                    checked = autoPlay,
                    onCheckedChange = { viewModel.toggleAutoPlayNext() }
                )
                SettingsSwitchRow(
                    icon = Icons.Default.History,
                    title = "Remember Position",
                    subtitle = "Resume videos where you left off",
                    checked = rememberPos,
                    onCheckedChange = { viewModel.toggleRememberPosition() }
                )
                SettingsSwitchRow(
                    icon = Icons.Default.Headphones,
                    title = "Background Playback",
                    subtitle = "Keep audio playing when app is minimized",
                    checked = bgAudio,
                    onCheckedChange = { viewModel.toggleBackgroundAudio() }
                )
                SettingsSwitchRow(
                    icon = Icons.Default.SkipNext,
                    title = "Auto Next Video",
                    subtitle = "Play next related video when finished",
                    checked = autoPlay,
                    onCheckedChange = { viewModel.toggleAutoPlayNext() }
                )
            }

            // 3. Media Library Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionLabel(title = "Media Library & Info", color = palette.primaryGlow)
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                val cardShape = RoundedCornerShape(LiquidGlassDimens.RadiusMedium)

                // Rescan Media Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(cardShape)
                        .liquidGlass(
                            shape = cardShape,
                            isDark = isDark
                        )
                        .clickable { viewModel.scanVideos() }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = palette.primaryGlow)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Rescan Media",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Scan device storage for new video files",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                // App Info Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(cardShape)
                        .liquidGlass(
                            shape = cardShape,
                            isDark = isDark
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LocalFlow Liquid Glass",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Version 2.0 • Offline YouTube Experience with Liquid Glass UI",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(title: String, color: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = color,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
    )
}

@Composable
private fun SettingsSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)
    val cardShape = RoundedCornerShape(LiquidGlassDimens.RadiusMedium)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(cardShape)
            .liquidGlass(
                shape = cardShape,
                isDark = isDark
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = palette.primaryGlow, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = if (isDark) AmoledBlack else Color.White,
                    checkedTrackColor = if (isDark) palette.primaryGlow else palette.deepAccent,
                    uncheckedThumbColor = if (isDark) TextMuted else Color(0xFF94A3B8),
                    uncheckedTrackColor = if (isDark) Color(0x33FFFFFF) else Color(0xFFCBD5E1)
                )
            )
        }
    }
}
