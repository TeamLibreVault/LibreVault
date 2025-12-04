package org.librevault.presentation.screens.gallery.components.media_picker

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells.Fixed
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.bitmapConfig
import coil3.video.VideoFrameDecoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.librevault.R
import org.librevault.domain.model.gallery.FileType
import org.librevault.presentation.screens.gallery.components.media_picker.model.MediaFile
import java.io.File

@Composable
fun MediaPickerDialog(
    onDismissRequest: () -> Unit,
    onFilesSelected: (List<File>) -> Unit,
) {
    val context = LocalContext.current

    var folders by remember { mutableStateOf(listOf<String>()) }
    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var mediaFiles by remember { mutableStateOf(listOf<MediaFile>()) }
    var selectedFiles by remember { mutableStateOf(setOf<MediaFile>()) }

    // Load folders from MediaStore
    LaunchedEffect(key1 = Unit) {
        folders = loadMediaFolders(context)
    }

    // Load media when folder changes
    LaunchedEffect(selectedFolder) {
        selectedFolder?.let {
            mediaFiles = loadMediaInFolder(context, it)
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = selectedFolder ?: "Select Folder",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))

                if (selectedFolder == null) {
                    // Show folders
                    LazyColumn {
                        items(folders) { folder ->
                            ListItem(
                                modifier = Modifier.clickable {
                                    selectedFolder = folder
                                },
                                headlineContent = { Text(folder) }
                            )
                        }
                    }
                } else {
                    // Show media inside folder
                    LazyVerticalGrid(
                        columns = Fixed(3),
                        modifier = Modifier.height(400.dp)
                    ) {
                        items(items = mediaFiles, key = { it.uri }) { media ->
                            val isSelected = selectedFiles.contains(media)

                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable {
                                        selectedFiles = if (isSelected) {
                                            selectedFiles - media
                                        } else {
                                            selectedFiles + media
                                        }
                                    }
                            ) {
                                val imageRequest = remember {
                                    ImageRequest.Builder(context)
                                        .data(media.uri)
                                        .size(128)
                                        .diskCachePolicy(CachePolicy.DISABLED)
                                        .memoryCachePolicy(CachePolicy.DISABLED)
                                        .bitmapConfig(Bitmap.Config.RGB_565)
                                        .build()
                                }

                                if (FileType.parse(media.file) == FileType.VIDEO) {
                                    val imageLoader = remember {
                                        ImageLoader.Builder(context)
                                            .components {
                                                add(VideoFrameDecoder.Factory())
                                            }
                                            .build()
                                    }

                                    AsyncImage(
                                        model = imageRequest,
                                        imageLoader = imageLoader,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(100.dp)
                                    )

                                    Icon(
                                        painter = painterResource(R.drawable.baseline_play_arrow_24),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .align(Alignment.Center),
                                    )
                                } else {
                                    AsyncImage(
                                        model = imageRequest,
                                        contentDescription = null,
                                        modifier = Modifier.size(100.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = {
                                selectedFolder = null
                                mediaFiles = emptyList()
                            }
                        ) {
                            Text("Back")
                        }
                        TextButton(onClick = {
                            val files = selectedFiles.mapNotNull { it.file }
                            onFilesSelected(files)
                            onDismissRequest()
                        }) {
                            Text("Select (${selectedFiles.size})")
                        }
                    }
                }
            }
        }
    }
}

private suspend fun loadMediaFolders(context: Context): List<String> =
    withContext(Dispatchers.IO) {
        val folders = mutableSetOf<String>()

        val projection = arrayOf(
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )

        val selection = """
            ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}
            OR
            ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO}
        """.trimIndent()

        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            null,
            null
        )?.use { cursor ->
            val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            while (cursor.moveToNext()) {
                val path = cursor.getString(dataIndex)
                val parent = File(path).parentFile?.name
                parent?.let { folders.add(it) }
            }
        }

        folders.toList()
    }

private suspend fun loadMediaInFolder(context: Context, folderName: String): List<MediaFile> =
    withContext(Dispatchers.IO) {
        val mediaFiles = mutableListOf<MediaFile>()

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )

        val selection = """
            (${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}
            OR
            ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO})
            AND
            ${MediaStore.Files.FileColumns.DATA} LIKE ?
        """.trimIndent()

        val selectionArgs = arrayOf("%/$folderName/%")

        context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val typeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val path = cursor.getString(dataIndex)
                val mediaType = cursor.getInt(typeIndex)
                val file = File(path)

                val uri = when (mediaType) {
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE ->
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO ->
                        ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

                    else -> null
                }

                if (uri != null) {
                    mediaFiles.add(MediaFile(uri, if (file.exists()) file else null))
                }
            }
        }

        mediaFiles
    }
