package org.librevault.presentation.activities.preview.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import org.librevault.R
import org.librevault.domain.model.vault.TempFile
import org.librevault.presentation.aliases.MediaInfo

@Composable
fun ZoomableImage(
    mediaContent: TempFile,
    mediaInfo: MediaInfo,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    onUiVisibilityToggle: () -> Unit
) {
    val zoomState = rememberZoomState()
    val image = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(mediaContent)
            .diskCachePolicy(CachePolicy.DISABLED)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .placeholder(R.drawable.outline_image_24)
            .error(R.drawable.outline_broken_image_24)
            .build()
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            modifier = Modifier
                .fillMaxSize()
                .zoomable(zoomState)
                .padding(innerPadding)
                .clickable(
                    interactionSource = null,
                    indication = null,
                    onClick = onUiVisibilityToggle
                ),
            painter = image,
            contentDescription = mediaInfo.fileName,
            contentScale = ContentScale.Fit
        )
    }
}
