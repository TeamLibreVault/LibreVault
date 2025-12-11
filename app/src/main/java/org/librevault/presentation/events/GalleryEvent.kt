package org.librevault.presentation.events

import org.librevault.domain.model.vault.FolderName
import java.io.File

sealed class GalleryEvent {
    data class LoadFolder(val folderName: FolderName) : GalleryEvent()
    data class LoadThumbnails(val ids: List<String> = emptyList()) : GalleryEvent()
    object SelectFiles : GalleryEvent()
    object UnselectFiles : GalleryEvent()
    data class EncryptFiles(val files: List<File>) : GalleryEvent()
    data class LoadMediaInfos(val ids: List<String> = emptyList()) : GalleryEvent()
    data class PreviewMedia(val id: String) : GalleryEvent()
    object RefreshGallery : GalleryEvent()
    object ClearGallery : GalleryEvent()
    data class SetDeleteSelection(val id: String) : GalleryEvent()
    data object ConfirmDeleteSelection : GalleryEvent()
    data object DeleteSelectedFiles : GalleryEvent()
    data object ClearDeleteSelection : GalleryEvent()
    data object CancelDeleteSelection : GalleryEvent()
}