package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.LocalFlowApp
import com.example.ui.theme.NaxxivoTheme
import com.example.viewmodel.VideoPlayerViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: VideoPlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle incoming video files from external file managers or share intents
        handleIntent(intent)

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            NaxxivoTheme(darkTheme = isDarkTheme) {
                LocalFlowApp(viewModel = viewModel)
            }
        }
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
