package org.librevault.presentation.activities.preview.components

import android.widget.VideoView
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import org.librevault.R
import org.librevault.domain.model.vault.TempFile
import org.librevault.presentation.aliases.MediaInfo
import java.util.concurrent.TimeUnit

@Composable
fun VideoPlayer(
    mediaInfo: MediaInfo,
    tempFile: TempFile,
    modifier: Modifier = Modifier,
    onUiVisibilityToggle: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val onUiToggle by rememberUpdatedState(onUiVisibilityToggle)

    var isPlaying by remember { mutableStateOf(true) }
    var isControlsVisible by remember { mutableStateOf(true) }

    var videoView by remember { mutableStateOf<VideoView?>(null) }
    var currentPosition by remember { mutableIntStateOf(0) }
    var duration by remember { mutableIntStateOf(0) }

    /* ---------------- Temp file ---------------- */

    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    isPlaying = false
                    videoView?.pause()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    videoView?.stopPlayback()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    /* ---------------- Lifecycle ---------------- */

    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    videoView?.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (isPlaying) videoView?.start()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    videoView?.stopPlayback()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    /* ---------------- Auto-hide controls ---------------- */

    LaunchedEffect(key1 = isControlsVisible) {
        if (isControlsVisible) {
            delay(3_000)
            isControlsVisible = false
            onUiToggle()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isControlsVisible = !isControlsVisible
                onUiToggle()
            }
    ) {

        /* ---------------- Video ---------------- */

        tempFile.let { file ->
            AndroidView(
                factory = {
                    VideoView(it).apply {
                        setVideoPath(file.absolutePath)

                        setOnPreparedListener { player ->
                            duration = player.duration
                            start()
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

        /* ---------------- Controls ---------------- */

        AnimatedVisibility(
            visible = isControlsVisible,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        modifier = Modifier.size(48.dp),
                        onClick = { isPlaying = !isPlaying }
                    ) {
                        Icon(
                            modifier = Modifier.fillMaxSize(),
                            painter = painterResource(
                                if (isPlaying)
                                    R.drawable.baseline_pause_24
                                else
                                    R.drawable.baseline_play_arrow_24
                            ),
                            contentDescription = null
                        )
                    }
                }

                Column {
                    Slider(
                        value = if (duration > 0)
                            currentPosition.toFloat() / duration
                        else 0f,
                        onValueChange = {
                            val pos = (it * duration).toInt()
                            videoView?.seekTo(pos)
                            currentPosition = pos
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatMillis(currentPosition), color = Color.White)
                        Text(formatMillis(duration), color = Color.White)
                    }
                }
            }
        }
    }

    /* ---------------- Progress updates ---------------- */

    LaunchedEffect(isPlaying, videoView) {
        while (isPlaying && videoView != null) {
            currentPosition = videoView!!.currentPosition
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