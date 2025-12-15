package org.librevault.presentation.events

import org.librevault.domain.model.gallery.MediaId
import org.librevault.domain.model.vault.FolderName
import java.io.File

sealed class GalleryEvent {
    data class LoadFolder(val folderName: FolderName) : GalleryEvent()
    data class LoadThumbnails(
        val ids: List<MediaId> = emptyList(),
        val forceRefresh: Boolean = false
    ) : GalleryEvent()
    object SelectFiles : GalleryEvent()
    object UnselectFiles : GalleryEvent()
    data class EncryptFiles(val files: List<File>) : GalleryEvent()
    data class PreviewMedia(val id: String) : GalleryEvent()
    data class SetDeleteSelection(val id: MediaId, val autoDeselect: Boolean = true) : GalleryEvent()
    data object ConfirmDeleteSelection : GalleryEvent()
    data object DeleteSelectedFiles : GalleryEvent()
    data object ClearDeleteSelection : GalleryEvent()
    data object CancelDeleteSelection : GalleryEvent()
}