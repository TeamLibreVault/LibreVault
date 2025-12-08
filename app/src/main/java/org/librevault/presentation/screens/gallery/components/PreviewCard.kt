package org.librevault.presentation.screens.gallery.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import org.librevault.R
import org.librevault.domain.model.gallery.FileType
import org.librevault.presentation.aliases.ThumbnailInfo

private const val TAG = "PreviewCard"

@Composable
fun PreviewCard(
    context: Context,
    thumb: ByteArray,
    info: ThumbnailInfo,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit,
) {
    Log.d(TAG, "PreviewCard: $info")

    Card(
        border = BorderStroke(
            width = if (selected) 1.5.dp else 0.dp,
            color = MaterialTheme.colorScheme.primary
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data(thumb)
                .diskCachePolicy(CachePolicy.DISABLED)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .build()
        )

        Box {
            Image(
                painter = painter,
                contentDescription = info.fileName,
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .combinedClickable(
                        onLongClick = onLongClick,
                        onClick = onClick
                    )
            )

            if (info.fileType == FileType.VIDEO) {
                Image(
                    modifier = Modifier
                        .size(76.dp)
                        .align(Alignment.Center),
                    painter = painterResource(R.drawable.baseline_play_arrow_24),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(Color.White),
                    contentDescription = null
                )
            }
        }
    }
}