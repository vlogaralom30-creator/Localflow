package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AlbumWithCount
import com.example.data.local.VideoEntity
import com.example.ui.theme.CardElevated
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
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

            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onDismiss()
                        onPlay()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = androidx.compose.ui.graphics.Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Play", color = androidx.compose.ui.graphics.Color.Black)
                }

                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = video.mimeType ?: "video/*"
                            putExtra(Intent.EXTRA_STREAM, Uri.parse(video.uri))
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
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
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
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
                    Icon(Icons.Default.Add, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Album", color = CyanAccent)
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
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CardElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onAlbumSelected(album.id, album.name) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderSpecial,
                                    contentDescription = null,
                                    tint = CyanAccent,
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
                                    tint = CyanAccent
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

@Composable
fun CreateAlbumDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var albumName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Album", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = albumName,
                onValueChange = { albumName = it },
                label = { Text("Album Name") },
                placeholder = { Text("e.g. Movies, Anime, Music...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("new_album_input")
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (albumName.isNotBlank()) {
                        onConfirm(albumName.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text("Create", color = androidx.compose.ui.graphics.Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
fun RenameVideoDialog(
    initialTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Video", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Video Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("rename_video_input")
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text("Save", color = androidx.compose.ui.graphics.Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(
    videoTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove Video", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Text(
                text = "Are you sure you want to remove \"$videoTitle\" from LocalFlow?",
                color = TextSecondary,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
            ) {
                Text("Remove", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

