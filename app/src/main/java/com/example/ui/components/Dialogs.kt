package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AlbumWithCount
import com.example.data.local.VideoEntity
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LiquidGlassButton
import com.example.ui.theme.LiquidGlassDimens
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LocalLiquidPreset
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.getPaletteForPreset
import com.example.ui.theme.liquidGlass
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailsBottomSheet(
    video: VideoEntity,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onWatchLaterToggle: () -> Unit,
    onAddToAlbum: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xF00D0F14) else Color(0xF0FFFFFF),
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .testTag("video_details_bottom_sheet")
        ) {
            Text(
                text = "Video Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            DetailItem(label = "Filename", value = video.title)
            DetailItem(label = "Duration", value = video.formattedDuration())
            DetailItem(label = "Resolution", value = video.resolution ?: "${video.width}x${video.height}")
            if (video.fps != null) DetailItem(label = "FPS", value = video.fps)
            DetailItem(label = "Size", value = video.formattedSize())
            DetailItem(label = "MIME Type", value = video.mimeType ?: "video/mp4")
            DetailItem(label = "Folder", value = video.folderName)
            DetailItem(label = "File Path", value = video.filePath)
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(video.dateAdded))
            DetailItem(label = "Date Added", value = dateStr)
            DetailItem(label = "Watch Count", value = "${video.watchCount} times")

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Actions with Liquid Glass Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LiquidGlassButton(
                    text = "Play",
                    onClick = {
                        onDismiss()
                        onPlay()
                    },
                    modifier = Modifier.weight(1f),
                    accentColor = palette.primaryGlow,
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp)) }
                )

                LiquidGlassButton(
                    text = "Share",
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = video.mimeType ?: "video/*"
                            putExtra(Intent.EXTRA_STREAM, Uri.parse(video.uri))
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
                    },
                    modifier = Modifier.weight(1f),
                    accentColor = palette.deepAccent,
                    icon = { Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = TextMuted, modifier = Modifier.width(100.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToAlbumBottomSheet(
    video: VideoEntity,
    albums: List<AlbumWithCount>,
    onDismiss: () -> Unit,
    onAlbumSelected: (Long, String) -> Unit,
    onCreateNewAlbum: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xF00D0F14) else Color(0xF0FFFFFF),
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .testTag("add_to_album_bottom_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add to Album",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                TextButton(onClick = onCreateNewAlbum) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = palette.primaryGlow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Album", color = palette.primaryGlow, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (albums.isEmpty()) {
                Text(
                    text = "No albums created yet. Click 'New Album' above.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(albums) { album ->
                        val itemShape = RoundedCornerShape(LiquidGlassDimens.RadiusSmall)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(itemShape)
                                .liquidGlass(
                                    shape = itemShape,
                                    isDark = isDark
                                )
                                .clickable { onAlbumSelected(album.id, album.name) }
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderSpecial,
                                    contentDescription = null,
                                    tint = palette.primaryGlow,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = album.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${album.videoCount} videos",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.PlaylistAdd,
                                    contentDescription = "Add",
                                    tint = palette.primaryGlow
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlbumDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var albumName by remember { mutableStateOf("") }
    val isDark = LocalIsDarkTheme.current
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)
    val dialogShape = RoundedCornerShape(LiquidGlassDimens.RadiusCard)

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(dialogShape)
                .liquidGlass(
                    shape = dialogShape,
                    isDark = isDark,
                    accentGlow = palette.primaryGlow
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Create New Album",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = albumName,
                    onValueChange = { albumName = it },
                    label = { Text("Album Name") },
                    placeholder = { Text("e.g. Movies, Anime, Music...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.primaryGlow,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_album_input")
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    LiquidGlassButton(
                        text = "Create",
                        onClick = {
                            if (albumName.isNotBlank()) {
                                onConfirm(albumName.trim())
                            }
                        },
                        accentColor = palette.primaryGlow
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameVideoDialog(
    initialTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    val isDark = LocalIsDarkTheme.current
    val preset = LocalLiquidPreset.current
    val palette = getPaletteForPreset(preset)
    val dialogShape = RoundedCornerShape(LiquidGlassDimens.RadiusCard)

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(dialogShape)
                .liquidGlass(
                    shape = dialogShape,
                    isDark = isDark,
                    accentGlow = palette.primaryGlow
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Rename Video",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Video Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.primaryGlow,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_video_input")
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    LiquidGlassButton(
                        text = "Save",
                        onClick = {
                            if (title.isNotBlank()) {
                                onConfirm(title.trim())
                            }
                        },
                        accentColor = palette.primaryGlow
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteConfirmationDialog(
    videoTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val dialogShape = RoundedCornerShape(LiquidGlassDimens.RadiusCard)

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(dialogShape)
                .liquidGlass(
                    shape = dialogShape,
                    isDark = isDark,
                    accentGlow = ErrorRed
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Remove Video",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Are you sure you want to remove \"$videoTitle\" from LocalFlow?",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    LiquidGlassButton(
                        text = "Remove",
                        onClick = onConfirm,
                        accentColor = ErrorRed
                    )
                }
            }
        }
    }
}
