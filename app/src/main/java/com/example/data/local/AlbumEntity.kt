package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val coverUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "video_album_cross_ref",
    primaryKeys = ["albumId", "videoId"],
    indices = [
        Index("albumId"),
        Index("videoId")
    ]
)
data class VideoAlbumCrossRef(
    val albumId: Long,
    val videoId: Long,
    val addedAt: Long = System.currentTimeMillis()
)
