package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.engine.RelatedVideosEngine
import com.example.data.local.NaxxivoDatabase
import com.example.data.local.VideoDao
import com.example.data.local.VideoEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: NaxxivoDatabase
    private lateinit var videoDao: VideoDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, NaxxivoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        videoDao = db.videoDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("LocalFlow", appName)
    }

    @Test
    fun `insert and retrieve video metadata in Room`() = runBlocking {
        val testVideo = VideoEntity(
            uri = "content://media/external/video/media/1",
            filePath = "/storage/emulated/0/DCIM/Camera/sample.mp4",
            title = "My Vacation Clip",
            durationMs = 65000L,
            sizeBytes = 10485760L,
            width = 1080,
            height = 1920,
            isShort = true,
            resolution = "1080x1920",
            mimeType = "video/mp4",
            thumbnailUri = "content://media/external/video/media/1",
            folderName = "Camera"
        )
        val id = videoDao.insertVideo(testVideo)
        val allVideos = videoDao.getAllVideos().first()

        assertEquals(1, allVideos.size)
        assertEquals("My Vacation Clip", allVideos[0].title)
        assertEquals(true, allVideos[0].isShort)
        assertEquals("01:05", allVideos[0].formattedDuration())
        assertEquals("10.0 MB", allVideos[0].formattedSize())
    }

    @Test
    fun `related videos recommendation engine finds same folder and keyword matches`() {
        val current = VideoEntity(
            id = 1,
            uri = "file://movies/horizon_part1.mp4",
            filePath = "/movies/horizon_part1.mp4",
            title = "The Last Horizon Part 1",
            durationMs = 3600000L,
            folderName = "Movies"
        )

        val candidate1 = VideoEntity(
            id = 2,
            uri = "file://movies/horizon_part2.mp4",
            filePath = "/movies/horizon_part2.mp4",
            title = "The Last Horizon Part 2",
            durationMs = 3800000L,
            folderName = "Movies"
        )

        val candidate2 = VideoEntity(
            id = 3,
            uri = "file://music/dance.mp4",
            filePath = "/music/dance.mp4",
            title = "Dance Party",
            durationMs = 180000L,
            folderName = "Music"
        )

        val related = RelatedVideosEngine.getRelatedVideos(current, listOf(current, candidate1, candidate2))
        assertEquals(2, related.size)
        assertEquals("The Last Horizon Part 2", related[0].title)
    }
}
