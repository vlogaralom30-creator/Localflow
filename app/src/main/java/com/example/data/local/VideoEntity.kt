package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing metadata for a local video file in LocalFlow.
 */
@Entity(
    tableName = "videos",
    indices = [Index(value = ["uri"], unique = true)]
)
data class VideoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uri: String,
    val filePath: String,
    val title: String,
    val durationMs: Long = 0L,
    val sizeBytes: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val isShort: Boolean = false,
    val resolution: String? = null,
    val fps: String? = null,
    val mimeType: String? = "video/mp4",
    val thumbnailUri: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val folderName: String = "Videos",
    val lastPositionMs: Long = 0L,
    val watchCount: Int = 0,
    val isWatchLater: Boolean = false,
    val isFavorite: Boolean = false
) {
    /**
     * Formats duration in milliseconds into HH:MM:SS or MM:SS
     */
    fun formattedDuration(): String {
        if (durationMs <= 0) return "00:00"
        val totalSeconds = durationMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    /**
     * Formats file size in bytes to human-readable string (KB, MB, GB)
     */
    fun formattedSize(): String {
        if (sizeBytes <= 0) return "0 B"
        val kb = sizeBytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format("%.1f GB", gb)
            mb >= 1.0 -> String.format("%.1f MB", mb)
            kb >= 1.0 -> String.format("%.0f KB", kb)
            else -> "$sizeBytes B"
        }
    }

    /**
     * Progress percentage (0 to 100)
     */
    fun watchProgressPercent(): Int {
        if (durationMs <= 0 || lastPositionMs <= 0) return 0
        return ((lastPositionMs.toFloat() / durationMs.toFloat()) * 100).toInt().coerceIn(0, 100)
    }

    /**
     * High-level quality label (e.g. 4K, 1080p, 720p)
     */
    fun qualityBadge(): String {
        val res = resolution ?: if (width > 0 && height > 0) "${width}x${height}" else ""
        return when {
            res.contains("3840") || res.contains("2160") || res.contains("4K", ignoreCase = true) -> "4K"
            res.contains("1920") || res.contains("1080") -> "1080p"
            res.contains("1280") || res.contains("720") -> "720p"
            res.contains("480") -> "480p"
            else -> if (isShort) "Short" else "HD"
        }
    }
}
