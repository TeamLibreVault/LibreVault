package org.librevault.presentation.screens.gallery.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.librevault.common.state.SelectState
import org.librevault.common.state.UiState
import org.librevault.domain.model.gallery.MediaId
import org.librevault.domain.model.vault.FolderName
import org.librevault.domain.model.vault.TempFile
import org.librevault.domain.model.vault.mediaId
import org.librevault.presentation.aliases.DeleteSelectionState

private const val TAG = "MediaGrid"

@Composable
fun MediaGrid(
    state: UiState.Success<List<TempFile>>,
    allFolderThumbsState:  Map<FolderName, List<TempFile>>,
    deleteFilesSelectionState: DeleteSelectionState,
    onLongSelect: (MediaId) -> Unit,
    onPreview: (MediaId) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = state.data,
            key = { it.mediaId() }
        ) { thumbnail ->
            val context = LocalContext.current
            val mediaId = thumbnail.mediaId()

            PreviewCard(
                context = context,
                isVideo = allFolderThumbsState[FolderName.VIDEOS]
                    ?.contains(thumbnail) == true,
                thumb = thumbnail,
                selected = mediaId in deleteFilesSelectionState.currentSelection,
                onLongClick = {
                    onLongSelect(MediaId(mediaId))
                }
            ) {
                val isSelecting =
                    deleteFilesSelectionState is SelectState.Selecting

                if (isSelecting) {
                    Log.d(
                        TAG,
                        "Content: Delete media selection: $mediaId"
                    )
                    onLongSelect(MediaId(mediaId))
                    return@PreviewCard
                }

                Log.d(
                    TAG,
                    "Content: Previewing media: $mediaId"
                )
                onPreview(MediaId(mediaId))
            }
        }
    }
}
