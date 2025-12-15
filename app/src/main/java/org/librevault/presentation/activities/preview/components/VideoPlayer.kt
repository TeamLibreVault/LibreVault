package org.librevault.presentation.activities.preview.components

import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
    modifier: Modifier = Modifier,
    onUiVisibilityToggle: () -> Unit
) {
    val activity = LocalActivity.currentOrThrow
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var tempFile: File? by remember { mutableStateOf(null) }
    var videoView by remember { mutableStateOf<VideoView?>(null) }
    var currentPosition by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf(0) }

    var isControlsVisible by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // Create temp file
    LaunchedEffect(byteArray) {
        tempFile =
            File.createTempFile("temp_video", ".${mediaInfo.fileExtension}", context.cacheDir)
                .apply {
                    deleteOnExit()
                    FileOutputStream(this).use { it.write(byteArray) }
                }
    }

    // Auto-hide controls after 3 seconds
    LaunchedEffect(lastInteractionTime) {
        while (true) {
            delay(3000)
            if (System.currentTimeMillis() - lastInteractionTime >= 3000) {
                if (isControlsVisible) {
                    isControlsVisible = false
                    onUiVisibilityToggle()
                }
            }
        }
    }

    BackHandler(tempFile != null) {
        tempFile?.delete()
        tempFile = null
        activity.finish()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = MutableInteractionSource(),
                indication = null,
                onClick = {
                    isControlsVisible = !isControlsVisible
                    lastInteractionTime = System.currentTimeMillis()
                    onUiVisibilityToggle()
                }
            )
    ) {
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
                    if (isPlaying && !view.isPlaying) view.start()
                    if (!isPlaying && view.isPlaying) view.pause()
                    currentPosition = view.currentPosition
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Animated Controls
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Center Play/Pause Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        modifier = Modifier.size(48.dp),
                        onClick = {
                            isPlaying = !isPlaying
                            lastInteractionTime = System.currentTimeMillis()
                        }
                    ) {
                        val state =
                            if (isPlaying) R.drawable.baseline_pause_24 to R.string.pause
                            else R.drawable.baseline_play_arrow_24 to R.string.play
                        Icon(
                            modifier = Modifier.fillMaxSize(),
                            painter = painterResource(state.first),
                            contentDescription = stringResource(state.second)
                        )
                    }
                }

                // Bottom Slider + Time
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Slider(
                        value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                        onValueChange = { fraction ->
                            val pos = (fraction * duration).toInt()
                            videoView?.seekTo(pos)
                            currentPosition = pos
                            lastInteractionTime = System.currentTimeMillis()
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
        }
    }

    // Update position
    LaunchedEffect(videoView, isPlaying) {
        while (videoView != null && isPlaying) {
            val pos = videoView!!.currentPosition
            if (pos != currentPosition) currentPosition = pos
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