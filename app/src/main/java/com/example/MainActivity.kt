package com.example

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.LocalFlowApp
import com.example.ui.theme.NaxxivoTheme
import com.example.viewmodel.AudioPlayerViewModel
import com.example.viewmodel.VideoPlayerViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: VideoPlayerViewModel by viewModels()
    private val audioViewModel: AudioPlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle incoming video files from external file managers or share intents
        handleIntent(intent)

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            val liquidPreset by viewModel.liquidPreset.collectAsStateWithLifecycle()
            val isFullscreen by viewModel.isFullscreen.collectAsStateWithLifecycle()
            val currentPlayingVideo by viewModel.currentPlayingVideo.collectAsStateWithLifecycle()

            // Handle Fullscreen orientation & immersive mode
            LaunchedEffect(isFullscreen, currentPlayingVideo) {
                val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
                if (isFullscreen && currentPlayingVideo != null) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                } else {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
                }
            }

            NaxxivoTheme(darkTheme = isDarkTheme, preset = liquidPreset) {
                LocalFlowApp(
                    viewModel = viewModel,
                    audioViewModel = audioViewModel,
                    onEnterPip = { enterPipMode() }
                )
            }
        }
    }

    fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                enterPictureInPictureMode(params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (viewModel.currentPlayingVideo.value != null && !viewModel.isMiniPlayerMode.value) {
            enterPipMode()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        viewModel.setInSystemPip(isInPictureInPictureMode)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            if (uri != null) {
                viewModel.importVideoUri(uri)
            }
        }
    }
}
