package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.data.local.NaxxivoDatabase
import com.example.data.repository.VideoRepository

class NaxxivoApp : Application(), ImageLoaderFactory {
    lateinit var database: NaxxivoDatabase
        private set
    lateinit var videoRepository: VideoRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = NaxxivoDatabase.getInstance(this)
        videoRepository = VideoRepository(this, database.videoDao(), database.albumDao())
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("video_thumbnails"))
                    .maxSizeBytes(60L * 1024 * 1024)
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
