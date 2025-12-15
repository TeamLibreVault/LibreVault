package org.librevault.presentation.activities.preview.components

import android.util.Log
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
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
import kotlinx.coroutines.delay
import org.librevault.R
import org.librevault.presentation.aliases.MediaInfo
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

@Composable
fun VideoPlayer(
    mediaInfo: MediaInfo,
    byteArray: ByteArray,
    modifier: Modifier = Modifier
) {
    val activity = LocalActivity.currentOrThrow
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var tempFile: File? by remember { mutableStateOf(null) }
    var videoView by remember { mutableStateOf<VideoView?>(null) }
    var currentPosition by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf(0) }

    // Create a temp file from byte array
    LaunchedEffect(key1 = byteArray) {
        tempFile =
            File.createTempFile("temp_video", ".${mediaInfo.fileExtension}", context.cacheDir)
                .apply {
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
                            isPlaying = true
                            duration = player.duration
                        }
                        setOnCompletionListener {
                            isPlaying = false
                        }
                        videoView = this
                    }
                },
                update = { view ->
                    videoView = view
                    // Play/pause control
                    if (isPlaying && !view.isPlaying) view.start()
                    if (!isPlaying && view.isPlaying) view.pause()

                    // Update current position
                    currentPosition = view.currentPosition
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top Center Play/Pause Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .align(Alignment.Center),
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

        // Bottom Progress Bar + Time
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(8.dp)
        ) {
            Slider(
                value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                onValueChange = { fraction ->
                    val pos = (fraction * duration).toInt()
                    videoView?.seekTo(pos)
                    currentPosition = pos
                },
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.Gray
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formatMillis(currentPosition), color = Color.White)
                Text(text = formatMillis(duration), color = Color.White)
            }
        }
    }

    // Update current position only while videoView exists and isPlaying is true
    LaunchedEffect(key1 = videoView, key2 = isPlaying) {
        while (videoView != null && isPlaying) {
            val pos = videoView!!.currentPosition
            if (pos != currentPosition) { // only update if changed
                currentPosition = pos
            }
            delay(300)
        }
    }
}

// Helper to format milliseconds to mm:ss
private fun formatMillis(millis: Int): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis.toLong())
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis.toLong()) % 60
    return "%02d:%02d".format(minutes, seconds)
}