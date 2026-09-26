package com.example.ui.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ContinueWatchingCard
import com.example.ui.components.LongVideoFeedCard
import com.example.ui.components.ShortVideoShelfCard
import com.example.ui.components.StoragePermissionHandler
import com.example.ui.theme.LiquidGlassButton
import com.example.ui.theme.LiquidGlassCircleButton
import com.example.ui.theme.LiquidGlassFilterChip
import com.example.ui.theme.LiquidLensSwitch
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LocalLiquidPreset
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.getPaletteForPreset
import com.example.ui.theme.liquidGlass
import com.example.viewmodel.HomeCategory
import com.example.viewmodel.NavTab
import com.example.viewmodel.VideoPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: VideoPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val isPermissionGranted by viewModel.isPermissionGranted.collectAsStateWithLifecycle()
    val homeCategory by viewModel.selectedHomeCategory.collectAsStateWithLifecycle()
    val continueWatching by viewModel.continueWatching.collectAsStateWithLifecycle()
    val shortVideos by viewModel.shortVideos.collectAsStateWithLifecycle()
    val longVideos by viewModel.longVideos.collectAsStateWithLifecycle()
    val allVideos by viewModel.allVideos.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignore if provider doesn't support persistent grant
            }
            viewModel.importVideoUri(uri)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("home_brand_header")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .shadow(6.dp, CircleShape, spotColor = palette.primaryGlow.copy(alpha = 0.5f))
                                .clip(CircleShape)
                                .background(palette.primaryGlow)
                                .border(1.2.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "LocalFlow",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                },
                actions = {
                    LiquidGlassCircleButton(
                        onClick = { filePickerLauncher.launch(arrayOf("video/*")) },
                        size = 38.dp,
                        modifier = Modifier.testTag("home_pick_file_btn")
                    ) {
                        Icon(Icons.Default.FileOpen, contentDescription = "Open video file", tint = TextPrimary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    LiquidGlassCircleButton(
                        onClick = { viewModel.scanVideos() },
                        size = 38.dp,
                        modifier = Modifier.testTag("home_refresh_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Rescan media", tint = TextPrimary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    LiquidGlassCircleButton(
                        onClick = { viewModel.openSearch() },
                        size = 38.dp,
                        modifier = Modifier.testTag("home_search_btn")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextPrimary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    LiquidGlassCircleButton(
                        onClick = { viewModel.setNavTab(NavTab.MUSIC) },
                        size = 38.dp,
                        modifier = Modifier.testTag("home_music_mode_btn")
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = "Music Mode", tint = palette.primaryGlow, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    LiquidLensSwitch(
                        isDarkMode = isDarkTheme,
                        onModeChanged = { viewModel.toggleTheme() },
                        modifier = Modifier.testTag("home_theme_toggle_btn")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. Horizontal Category Filter Chips (Liquid Glass)
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val categories = listOf(
                        Pair(HomeCategory.ALL, "All"),
                        Pair(HomeCategory.RECENTLY_ADDED, "Recently Added"),
                        Pair(HomeCategory.CONTINUE_WATCHING, "Continue Watching"),
                        Pair(HomeCategory.SHORTS, "Shorts"),
                        Pair(HomeCategory.LONG_VIDEOS, "Long Videos")
                    )
                    items(categories) { (cat, label) ->
                        val isSelected = homeCategory == cat
                        LiquidGlassFilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setHomeCategory(cat) },
                            label = label,
                            modifier = Modifier.testTag("home_chip_${cat.name.lowercase()}")
                        )
                    }
                }
            }

            // Storage Permission Warning Banner if not granted
            if (!isPermissionGranted) {
                item {
                    StoragePermissionHandler(
                        isPermissionGranted = isPermissionGranted,
                        onPermissionResult = { viewModel.onPermissionResult(it) },
                        onPickVideoFile = { viewModel.importVideoUri(it) }
                    )
                }
            }

            // 2. Continue Watching Shelf
            if (continueWatching.isNotEmpty() && (homeCategory == HomeCategory.ALL || homeCategory == HomeCategory.CONTINUE_WATCHING)) {
                item {
                    SectionHeader(title = "Continue Watching", onSeeAll = {})
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(continueWatching, key = { "cw_${it.id}" }) { video ->
                            ContinueWatchingCard(
                                video = video,
                                onClick = { viewModel.playVideo(video) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // 3. Short Videos 9:16 Shelf
            if (shortVideos.isNotEmpty() && (homeCategory == HomeCategory.ALL || homeCategory == HomeCategory.SHORTS)) {
                item {
                    SectionHeader(title = "Short Videos", onSeeAll = { viewModel.setHomeCategory(HomeCategory.SHORTS) })
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(shortVideos, key = { "sv_${it.id}" }) { short ->
                            ShortVideoShelfCard(
                                video = short,
                                onClick = { viewModel.playVideo(short) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // 4. Recently Added & Long Videos Feed
            if (homeCategory != HomeCategory.SHORTS) {
                item {
                    val feedTitle = if (homeCategory == HomeCategory.RECENTLY_ADDED) "Recently Added" else "Long Videos"
                    SectionHeader(title = feedTitle, onSeeAll = null)
                }

                val feedVideos = when (homeCategory) {
                    HomeCategory.LONG_VIDEOS -> longVideos
                    HomeCategory.RECENTLY_ADDED -> allVideos
                    else -> if (longVideos.isNotEmpty()) longVideos else allVideos
                }

                if (feedVideos.isEmpty()) {
                    item {
                        EmptyFeedPlaceholder(
                            onScan = { viewModel.scanVideos() },
                            onPickFile = { filePickerLauncher.launch(arrayOf("video/*")) }
                        )
                    }
                } else {
                    items(feedVideos, key = { "feed_${it.id}" }) { video ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            LongVideoFeedCard(
                                video = video,
                                onClick = { viewModel.playVideo(video) },
                                onWatchLaterToggle = { viewModel.toggleWatchLater(video) },
                                onAddToAlbum = { viewModel.openAddToAlbum(video) },
                                onShowDetails = { viewModel.showDetails(video) },
                                onDelete = { viewModel.deleteVideo(video) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAll: (() -> Unit)?
) {
    val isDark = LocalIsDarkTheme.current
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        if (onSeeAll != null) {
            Text(
                text = "See All",
                fontSize = 12.sp,
                color = if (isDark) palette.primaryGlow else palette.deepAccent,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onSeeAll)
            )
        }
    }
}

@Composable
private fun EmptyFeedPlaceholder(
    onScan: () -> Unit,
    onPickFile: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.VideoLibrary,
            contentDescription = null,
            tint = palette.primaryGlow,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "No videos yet",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Scan your device storage or pick a video file to watch offline.",
            fontSize = 13.sp,
            color = TextMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LiquidGlassButton(
                text = "Scan Storage",
                onClick = onScan,
                accentColor = palette.primaryGlow,
                icon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)) }
            )
            LiquidGlassButton(
                text = "Pick Video",
                onClick = onPickFile,
                accentColor = palette.deepAccent,
                icon = { Icon(Icons.Default.FileOpen, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)) }
            )
        }
    }
}
