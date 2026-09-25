package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.data.local.VideoEntity
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardElevated
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Horizontal "Continue Watching" card showing thumbnail, title, and progress bar
 */
@Composable
fun ContinueWatchingCard(
    video: VideoEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val progress = (video.lastPositionMs.toFloat() / video.durationMs.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
    val percent = video.watchProgressPercent()

    Box(
        modifier = modifier
            .width(230.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(
                            Color(0x38FFFFFF),
                            Color(0x2E162234),
                            Color(0x450D1420)
                        )
                    } else {
                        listOf(
                            Color(0xF5FFFFFF),
                            Color(0xEBF8FAFC),
                            Color(0xE0F1F5F9)
                        )
                    }
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    if (isDark) {
                        listOf(Color(0x66FFFFFF), Color(0x18FFFFFF), Color(0x3300E5FF))
                    } else {
                        listOf(Color(0xFFFFFFFF), Color(0x80CBD5E1), Color(0x4000B4D8))
                    }
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .testTag("continue_card_${video.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(Color(0x33101826))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(video.thumbnailUri ?: video.uri)
                        .videoFrameMillis(2000)
                        .crossfade(true)
                        .build(),
                    contentDescription = video.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Liquid glass quality badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x60070B14))
                        .border(0.8.dp, Color(0x4DFFFFFF), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = video.qualityBadge(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Liquid glass Play icon
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x8000E5FF))
                        .border(1.dp, Color(0xB3FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Progress Bar at bottom of thumbnail
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.5.dp)
                        .align(Alignment.BottomCenter),
                    color = CyanAccent,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$percent% watched",
                        fontSize = 11.sp,
                        color = CyanAccent,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = video.formattedDuration(),
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

/**
 * 9:16 Vertical Short Video Card for Shorts Shelf on Home
 */
@Composable
fun ShortVideoShelfCard(
    video: VideoEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .width(136.dp)
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0x33101826))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color(0x55FFFFFF), Color(0x18FFFFFF), Color(0x3300E5FF))
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .testTag("short_card_${video.id}")
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(video.thumbnailUri ?: video.uri)
                .videoFrameMillis(1500)
                .crossfade(true)
                .build(),
            contentDescription = video.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Liquid dark gradient overlay at bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x400A0F1A),
                            Color(0xEB060A12)
                        ),
                        startY = 140f
                    )
                )
        )

        // Frosted glass duration badge top right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(7.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x66080D18))
                .border(0.8.dp, Color(0x4DFFFFFF), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = video.formattedDuration(),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Title and folder badge at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
            Text(
                text = video.title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = video.folderName,
                color = CyanAccent,
                fontSize = 10.sp,
                maxLines = 1,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * YouTube-style Long Video Feed item (16:9 thumbnail, title, badges, more menu)
 */
@Composable
fun LongVideoFeedCard(
    video: VideoEntity,
    onClick: () -> Unit,
    onWatchLaterToggle: () -> Unit,
    onAddToAlbum: () -> Unit,
    onShowDetails: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
            .testTag("feed_card_${video.id}")
    ) {
        // 16:9 Thumbnail
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0x33101826))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color(0x40FFFFFF), Color(0x10FFFFFF), Color(0x2800E5FF))
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(video.thumbnailUri ?: video.uri)
                    .videoFrameMillis(2500)
                    .crossfade(true)
                    .build(),
                contentDescription = video.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Frosted glass duration badge bottom right
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x73060A14))
                    .border(0.8.dp, Color(0x59FFFFFF), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = video.formattedDuration(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Liquid glass quality badge top left
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x8000E5FF))
                    .border(1.dp, Color(0xB3FFFFFF), RoundedCornerShape(8.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = video.qualityBadge(),
                    color = Color.Black,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title and actions row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = video.formattedSize(),
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(text = "•", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = video.folderName,
                        fontSize = 11.sp,
                        color = CyanAccent,
                        fontWeight = FontWeight.Medium
                    )
                    if (video.fps != null) {
                        Text(text = "•", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = video.fps,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (video.isWatchLater) "Remove from Watch Later" else "Save to Watch Later") },
                        onClick = {
                            showMenu = false
                            onWatchLaterToggle()
                        },
                        leadingIcon = {
                            Icon(
                                if (video.isWatchLater) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = CyanAccent
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add to Album") },
                        onClick = {
                            showMenu = false
                            onAddToAlbum()
                        },
                        leadingIcon = { Icon(Icons.Default.PlaylistAdd, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Details & Info") },
                        onClick = {
                            showMenu = false
                            onShowDetails()
                        },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete from LocalFlow", color = ErrorRed) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) }
                    )
                }
            }
        }
    }
}

/**
 * Compact horizontal related video item shown underneath YouTube-style player
 */
@Composable
fun RelatedVideoCard(
    video: VideoEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
            .testTag("related_card_${video.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(CardElevated)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(video.thumbnailUri ?: video.uri)
                    .videoFrameMillis(2000)
                    .crossfade(true)
                    .build(),
                contentDescription = video.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Surface(
                shape = RoundedCornerShape(3.dp),
                color = AmoledBlack.copy(alpha = 0.8f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
            ) {
                Text(
                    text = video.formattedDuration(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = video.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${video.formattedDuration()} • ${video.qualityBadge()} • ${video.formattedSize()}",
                fontSize = 11.sp,
                color = TextMuted
            )
            Text(
                text = "📁 ${video.folderName}",
                fontSize = 10.sp,
                color = CyanAccent
            )
        }
    }
}
