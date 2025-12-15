package org.librevault.presentation.activities.preview.components

import android.util.Log
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import cafe.adriel.voyager.navigator.currentOrThrow
import org.librevault.R
import org.librevault.presentation.aliases.MediaInfo
import java.io.File
import java.io.FileOutputStream

@Composable
fun VideoPlayer(mediaInfo: MediaInfo, byteArray: ByteArray, modifier: Modifier = Modifier) {
    val activity = LocalActivity.currentOrThrow
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var tempFile: File? by remember { mutableStateOf(null) }

    // Create a temp file from byte array
    LaunchedEffect(key1 = byteArray) {
        tempFile = File.createTempFile("temp_video", ".${mediaInfo.fileExtension}", context.cacheDir).apply {
            deleteOnExit()
            FileOutputStream(this).use { it.write(byteArray) }
            Log.d("VideoPlayer", "Video file created: $absolutePath")
        }
    }

    BackHandler(tempFile != null) {
        tempFile?.delete()
        tempFile = null
        activity.finish()
    }

    Box(modifier = modifier) {
        tempFile?.let { file ->
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoPath(file.absolutePath)
                        setOnPreparedListener { player ->
                            start()
                            isPlaying = player.isPlaying
                        }
                        setOnCompletionListener {
                            isPlaying = false
                        }
                    }
                },
                update = { videoView ->
                    // Control play/pause
                    if (isPlaying && !videoView.isPlaying) videoView.start()
                    if (!isPlaying && videoView.isPlaying) videoView.pause()
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier
                .background(Color.Transparent)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    modifier = Modifier.size(48.dp),
                    onClick = { isPlaying = !isPlaying }
                ) {
                    val state =
                        if (isPlaying) R.drawable.baseline_pause_24 to R.string.pause else R.drawable.baseline_play_arrow_24 to R.string.play
                    Icon(
                        modifier = Modifier.fillMaxSize(),
                        painter = painterResource(state.first),
                        contentDescription = stringResource(state.second)
                    )
                }
            }
        }
    }
}