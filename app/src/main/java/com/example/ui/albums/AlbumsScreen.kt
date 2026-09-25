package com.example.ui.albums

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.AlbumWithCount
import com.example.ui.components.CreateAlbumDialog
import com.example.ui.components.LongVideoFeedCard
import com.example.ui.theme.LiquidGlassButton
import com.example.ui.theme.LiquidGlassCircleButton
import com.example.ui.theme.LiquidGlassDimens
import com.example.ui.theme.LiquidGlassMotion
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LocalLiquidPreset
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.getPaletteForPreset
import com.example.ui.theme.liquidGlass
import com.example.viewmodel.VideoPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(
    viewModel: VideoPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val albums by viewModel.albumsWithCount.collectAsStateWithLifecycle()
    val selectedAlbum by viewModel.selectedAlbum.collectAsStateWithLifecycle()
    val albumVideos by viewModel.videosForSelectedAlbum.collectAsStateWithLifecycle()
    val preset by viewModel.liquidPreset.collectAsStateWithLifecycle()

    val isDark = LocalIsDarkTheme.current
    val palette = getPaletteForPreset(preset)

    var showCreateDialog by remember { mutableStateOf(false) }

    if (showCreateDialog) {
        CreateAlbumDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                viewModel.createAlbum(name)
                showCreateDialog = false
            }
        )
    }

    if (selectedAlbum != null) {
        // Detail view for selected album
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = selectedAlbum!!.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${albumVideos.size} videos in album",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    },
                    navigationIcon = {
                        LiquidGlassCircleButton(
                            onClick = { viewModel.selectAlbum(null) },
                            size = 38.dp,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary, modifier = Modifier.size(18.dp))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            if (albumVideos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No videos in this album yet.\nUse 'Add to Album' on any video to add it here.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(albumVideos, key = { "album_vid_${it.id}" }) { video ->
                        LongVideoFeedCard(
                            video = video,
                            onClick = { viewModel.playVideo(video) },
                            onWatchLaterToggle = { viewModel.toggleWatchLater(video) },
                            onAddToAlbum = { viewModel.openAddToAlbum(video) },
                            onShowDetails = { viewModel.showDetails(video) },
                            onDelete = { viewModel.deleteVideo(video) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
        return
    }

    // Main Albums Grid
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("albums_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Albums",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                actions = {
                    LiquidGlassCircleButton(
                        onClick = { showCreateDialog = true },
                        size = 38.dp,
                        modifier = Modifier.testTag("create_album_header_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create Album", tint = palette.primaryGlow, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            LiquidGlassButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier.size(56.dp).testTag("create_album_fab"),
                shape = CircleShape,
                isProminent = true
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Album", tint = Color.Black, modifier = Modifier.size(28.dp))
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(albums, key = { it.id }) { album ->
                AlbumCard(
                    album = album,
                    onClick = { viewModel.selectAlbum(album) }
                )
            }
        }
    }
}

@Composable
private fun AlbumCard(
    album: AlbumWithCount,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) LiquidGlassMotion.SubtlePressScale else 1.0f,
        animationSpec = LiquidGlassMotion.SpringBouncy,
        label = "album_press_scale"
    )

    val cardShape = RoundedCornerShape(LiquidGlassDimens.RadiusCard)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isDark) 8.dp else 4.dp,
                shape = cardShape,
                spotColor = palette.primaryGlow.copy(alpha = 0.3f)
            )
            .clip(cardShape)
            .liquidGlass(
                shape = cardShape,
                isDark = isDark,
                borderColor = if (isDark) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.85f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("album_card_${album.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = LiquidGlassDimens.RadiusCard, topEnd = LiquidGlassDimens.RadiusCard))
                    .background(Color(0x33101826))
            ) {
                if (album.coverUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(album.coverUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = album.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(palette.primaryGlow.copy(alpha = 0.35f), Color(0x33101826))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderSpecial,
                            contentDescription = null,
                            tint = palette.primaryGlow,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Dark gradient scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                startY = 100f
                            )
                        )
                )

                // Label at bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = album.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${album.videoCount} videos",
                        fontSize = 11.sp,
                        color = palette.primaryGlow,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
