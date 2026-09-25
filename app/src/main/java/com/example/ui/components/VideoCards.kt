package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.data.local.VideoEntity
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LiquidGlassDimens
import com.example.ui.theme.LiquidGlassMotion
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LocalLiquidPreset
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.getPaletteForPreset
import com.example.ui.theme.liquidGlass

/**
 * Horizontal "Continue Watching" card with Liquid Glass styling
 */
@Composable
fun ContinueWatchingCard(
    video: VideoEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
        label = "cw_press_scale"
    )

    val progress = (video.lastPositionMs.toFloat() / video.durationMs.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
    val percent = video.watchProgressPercent()
    val cardShape = RoundedCornerShape(LiquidGlassDimens.RadiusCard)

    Box(
        modifier = modifier
            .width(236.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isDark) 6.dp else 3.dp,
                shape = cardShape,
                spotColor = if (isDark) palette.primaryGlow.copy(alpha = 0.25f) else Color(0x15000000)
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
            .testTag("continue_card_${video.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = LiquidGlassDimens.RadiusCard, topEnd = LiquidGlassDimens.RadiusCard))
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

                // Glass quality badge top right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(LiquidGlassDimens.RadiusSmall))
                        .liquidGlass(
                            shape = RoundedCornerShape(LiquidGlassDimens.RadiusSmall),
                            isDark = true,
                            glassAlpha = 0.3f,
                            borderColor = Color.White.copy(alpha = 0.5f)
                        )
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = video.qualityBadge(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Glass Play Button centered
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(42.dp)
                        .shadow(8.dp, CircleShape, spotColor = palette.primaryGlow.copy(alpha = 0.6f))
                        .clip(CircleShape)
                        .background(palette.primaryGlow)
                        .border(1.2.dp, Color.White.copy(alpha = 0.85f), CircleShape),
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
                    color = palette.primaryGlow,
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
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$percent% watched",
                        fontSize = 11.sp,
                        color = if (isDark) palette.primaryGlow else palette.deepAccent,
                        fontWeight = FontWeight.SemiBold
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
 * 9:16 Vertical Short Video Card for Shorts Shelf
 */
@Composable
fun ShortVideoShelfCard(
    video: VideoEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) LiquidGlassMotion.PressScaleFactor else 1.0f,
        animationSpec = LiquidGlassMotion.SpringBouncy,
        label = "short_shelf_scale"
    )

    val cardShape = RoundedCornerShape(LiquidGlassDimens.RadiusCard)

    Box(
        modifier = modifier
            .width(138.dp)
            .aspectRatio(9f / 16f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isDark) 6.dp else 3.dp,
                shape = cardShape,
                spotColor = palette.primaryGlow.copy(alpha = 0.25f)
            )
            .clip(cardShape)
            .liquidGlass(
                shape = cardShape,
                isDark = isDark,
                borderColor = if (isDark) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.8f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
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

        // Ambient dark scrim at bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x300A0F1A),
                            Color(0xF0060A12)
                        ),
                        startY = 130f
                    )
                )
        )

        // Frosted glass duration badge top right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(7.dp)
                .clip(RoundedCornerShape(LiquidGlassDimens.RadiusSmall))
                .liquidGlass(
                    shape = RoundedCornerShape(LiquidGlassDimens.RadiusSmall),
                    isDark = true,
                    glassAlpha = 0.35f,
                    borderColor = Color.White.copy(alpha = 0.5f)
                )
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
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = video.folderName,
                color = palette.primaryGlow,
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
    onRename: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)

    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) LiquidGlassMotion.SubtlePressScale else 1.0f,
        animationSpec = LiquidGlassMotion.SpringBouncy,
        label = "feed_press_scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp)
            .testTag("feed_card_${video.id}")
    ) {
        // 16:9 Thumbnail Container
        val thumbShape = RoundedCornerShape(LiquidGlassDimens.RadiusCard)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .shadow(
                    elevation = if (isDark) 8.dp else 4.dp,
                    shape = thumbShape,
                    spotColor = palette.primaryGlow.copy(alpha = 0.25f)
                )
                .clip(thumbShape)
                .liquidGlass(
                    shape = thumbShape,
                    isDark = isDark,
                    borderColor = if (isDark) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.8f)
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
                    .clip(RoundedCornerShape(LiquidGlassDimens.RadiusSmall))
                    .liquidGlass(
                        shape = RoundedCornerShape(LiquidGlassDimens.RadiusSmall),
                        isDark = true,
                        glassAlpha = 0.4f,
                        borderColor = Color.White.copy(alpha = 0.6f)
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = video.formattedDuration(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Specular glass quality badge top left
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(LiquidGlassDimens.RadiusSmall))
                    .background(palette.primaryGlow.copy(alpha = 0.85f))
                    .border(1.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(LiquidGlassDimens.RadiusSmall))
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

        Spacer(modifier = Modifier.height(10.dp))

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
                Spacer(modifier = Modifier.height(4.dp))
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
                        color = if (isDark) palette.primaryGlow else palette.deepAccent,
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
                        text = { Text("Share Video") },
                        onClick = {
                            showMenu = false
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = video.mimeType ?: "video/*"
                                val uri = Uri.parse(video.uri)
                                putExtra(Intent.EXTRA_STREAM, uri)
                                putExtra(Intent.EXTRA_SUBJECT, video.title)
                                putExtra(Intent.EXTRA_TEXT, "Watch \"${video.title}\" on LocalFlow")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Video via"))
                        },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = palette.primaryGlow) }
                    )
                    if (onRename != null) {
                        DropdownMenuItem(
                            text = { Text("Rename Video") },
                            onClick = {
                                showMenu = false
                                showRenameDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = palette.primaryGlow) }
                        )
                    }
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
                                tint = palette.primaryGlow
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
                            showDeleteConfirmDialog = true
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) }
                    )
                }
            }
        }
    }

    if (showRenameDialog && onRename != null) {
        RenameVideoDialog(
            initialTitle = video.title,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newTitle ->
                onRename(newTitle)
                showRenameDialog = false
            }
        )
    }

    if (showDeleteConfirmDialog) {
        DeleteConfirmationDialog(
            videoTitle = video.title,
            onDismiss = { showDeleteConfirmDialog = false },
            onConfirm = {
                onDelete()
                showDeleteConfirmDialog = false
            }
        )
    }
}

/**
 * Compact horizontal related video item
 */
@Composable
fun RelatedVideoCard(
    video: VideoEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)

    val itemShape = RoundedCornerShape(LiquidGlassDimens.RadiusSmall)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(itemShape)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
            .testTag("related_card_${video.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(120.dp)
                .aspectRatio(16f / 9f)
                .clip(itemShape)
                .background(Color(0x33101826))
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color(0x40FFFFFF), Color(0x10FFFFFF))
                    ),
                    shape = itemShape
                )
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

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .liquidGlass(
                        shape = RoundedCornerShape(4.dp),
                        isDark = true,
                        glassAlpha = 0.4f,
                        borderColor = Color.White.copy(alpha = 0.5f)
                    )
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = video.formattedDuration(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = video.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "${video.formattedDuration()} • ${video.qualityBadge()} • ${video.formattedSize()}",
                fontSize = 11.sp,
                color = TextMuted
            )
            Text(
                text = "📁 ${video.folderName}",
                fontSize = 10.sp,
                color = if (isDark) palette.primaryGlow else palette.deepAccent,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
