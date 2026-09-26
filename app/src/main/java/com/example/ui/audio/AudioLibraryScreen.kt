package com.example.ui.audio

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.AudioEntity
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.LiquidGlassButton
import com.example.ui.theme.LiquidGlassCircleButton
import com.example.ui.theme.LiquidGlassFilterChip
import com.example.ui.theme.LocalLiquidPreset
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.getPaletteForPreset
import com.example.ui.theme.liquidGlass
import com.example.viewmodel.AudioFilterTab
import com.example.viewmodel.AudioPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioLibraryScreen(
    viewModel: AudioPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allTracks by viewModel.allAudioTracks.collectAsStateWithLifecycle()
    val favoriteTracks by viewModel.favoriteAudioTracks.collectAsStateWithLifecycle()
    val recentTracks by viewModel.recentlyPlayedAudioTracks.collectAsStateWithLifecycle()
    val filterTab by viewModel.currentFilterTab.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()

    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)

    val displayedTracks = when (filterTab) {
        AudioFilterTab.ALL -> allTracks
        AudioFilterTab.FAVORITES -> favoriteTracks
        AudioFilterTab.RECENT -> recentTracks
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
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
            viewModel.scanAudio()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("audio_library_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .shadow(6.dp, CircleShape, spotColor = palette.primaryGlow.copy(alpha = 0.5f))
                                .clip(CircleShape)
                                .liquidGlass(
                                    shape = CircleShape,
                                    borderColor = palette.primaryGlow.copy(alpha = 0.6f),
                                    glassAlpha = 0.3f
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = palette.primaryGlow,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Music Library",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${displayedTracks.size} Tracks",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                },
                actions = {
                    // Rescan Device Music
                    LiquidGlassCircleButton(
                        onClick = { viewModel.scanAudio() },
                        size = 38.dp
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Scan Audio Files",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))

                    // Open / Pick Music File
                    LiquidGlassCircleButton(
                        onClick = { audioPickerLauncher.launch(arrayOf("audio/*")) },
                        size = 38.dp
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FileOpen,
                            contentDescription = "Pick Music File",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter Tabs (All, Favorites, Recent)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LiquidGlassFilterChip(
                    selected = filterTab == AudioFilterTab.ALL,
                    onClick = { viewModel.setFilterTab(AudioFilterTab.ALL) },
                    label = "All Songs (${allTracks.size})"
                )
                LiquidGlassFilterChip(
                    selected = filterTab == AudioFilterTab.FAVORITES,
                    onClick = { viewModel.setFilterTab(AudioFilterTab.FAVORITES) },
                    label = "Favorites (${favoriteTracks.size})"
                )
                LiquidGlassFilterChip(
                    selected = filterTab == AudioFilterTab.RECENT,
                    onClick = { viewModel.setFilterTab(AudioFilterTab.RECENT) },
                    label = "Recently Played"
                )
            }

            if (isScanning) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = palette.primaryGlow, strokeWidth = 3.dp)
                }
            }

            if (displayedTracks.isEmpty() && !isScanning) {
                // Empty state card
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val cardShape = RoundedCornerShape(24.dp)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlass(
                                shape = cardShape,
                                borderColor = palette.primaryGlow.copy(alpha = 0.4f),
                                glassAlpha = 0.2f
                            )
                            .clip(cardShape)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = palette.primaryGlow,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Audio Tracks Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Scan your device storage or pick an audio file (.mp3, .m4a, .flac, .wav) to start listening.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        LiquidGlassButton(
                            onClick = { viewModel.scanAudio() },
                            modifier = Modifier.height(44.dp),
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Text(
                                text = "Scan Music on Device",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayedTracks, key = { it.id }) { track ->
                        val isCurrentPlaying = currentTrack?.id == track.id
                        AudioTrackCard(
                            track = track,
                            isCurrentPlaying = isCurrentPlaying,
                            isPlaying = isCurrentPlaying && isPlaying,
                            onPlay = { viewModel.playTrack(track, displayedTracks) },
                            onToggleFavorite = { viewModel.toggleFavorite(track) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AudioTrackCard(
    track: AudioEntity,
    isCurrentPlaying: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)
    val cardShape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = cardShape,
                borderColor = if (isCurrentPlaying) palette.primaryGlow.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.15f),
                glassAlpha = if (isCurrentPlaying) 0.3f else 0.14f,
                accentGlow = if (isCurrentPlaying) palette.primaryGlow.copy(alpha = 0.25f) else null
            )
            .clip(cardShape)
            .clickable(onClick = onPlay)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album Art / Vinyl Thumbnail
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF141A24)),
            contentAlignment = Alignment.Center
        ) {
            if (!track.albumArtUri.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(track.albumArtUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = if (isCurrentPlaying) palette.primaryGlow else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title & Artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = if (isCurrentPlaying) palette.primaryGlow else TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${track.artist} • ${track.album}",
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${track.formattedDuration()} • ${track.formattedSize()}",
                color = TextMuted,
                fontSize = 10.sp
            )
        }

        // Favorite Toggle Button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable(onClick = onToggleFavorite),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (track.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (track.isFavorite) Color(0xFFFF4081) else TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Play Button
        Box(
            modifier = Modifier
                .size(38.dp)
                .liquidGlass(
                    shape = CircleShape,
                    borderColor = palette.primaryGlow.copy(alpha = 0.5f),
                    glassAlpha = 0.25f
                )
                .clip(CircleShape)
                .clickable(onClick = onPlay),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play",
                tint = if (isCurrentPlaying) palette.primaryGlow else TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
