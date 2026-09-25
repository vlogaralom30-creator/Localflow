package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.CardDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
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
    val orientation by viewModel.defaultOrientation.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

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
            // Appearance & Theme
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleSmall,
                    color = CyanAccent,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                SettingsSwitchRow(
                    icon = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                    title = "Dark Theme",
                    subtitle = if (isDarkTheme) "Deep AMOLED liquid glass theme" else "Bright pearl liquid glass theme",
                    checked = isDarkTheme,
                    onCheckedChange = { viewModel.setDarkTheme(it) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text(
                    text = "Player",
                    style = MaterialTheme.typography.titleSmall,
                    color = CyanAccent,
                    fontWeight = FontWeight.Bold
                )
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

            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Library",
                    style = MaterialTheme.typography.titleSmall,
                    color = CyanAccent,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardDark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.scanVideos() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = CyanAccent)
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

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardDark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LocalFlow",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Version 1.0 • Offline YouTube Experience",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0x35FFFFFF),
                        Color(0x20152032),
                        Color(0x350A0F1A)
                    )
                )
            )
            .border(
                width = 0.8.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0x52FFFFFF),
                        Color(0x15FFFFFF),
                        Color(0x2800E5FF)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
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
                Icon(imageVector = icon, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(24.dp))
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
                    checkedThumbColor = AmoledBlack,
                    checkedTrackColor = CyanAccent,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = Color(0x33FFFFFF)
                )
            )
        }
    }
}
