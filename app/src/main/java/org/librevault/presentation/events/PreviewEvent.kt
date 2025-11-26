package org.librevault.presentation.events

import org.librevault.presentation.aliases.MediaContent
import org.librevault.presentation.aliases.MediaInfo

sealed class PreviewEvent {
    data class RestoreImage(
        val mediaInfo: MediaInfo,
        val mediaContent: MediaContent,
    ) : PreviewEvent()

    data class LoadMediaInfo(val id: String?) : PreviewEvent()
    data class ShowDetailsDialog(val mediaInfo: MediaInfo) : PreviewEvent()
    data object HideDetailsDialog : PreviewEvent()
    data object ShowErrorInfoDialog : PreviewEvent()
    data object HideErrorInfoDialog : PreviewEvent()
    data class DecryptMedia(val id: String?) : PreviewEvent()
}