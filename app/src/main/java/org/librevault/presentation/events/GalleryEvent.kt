package org.librevault.presentation.events

import java.io.File

sealed class GalleryEvent {
    data class LoadFolder(val folderName: String) : GalleryEvent()
    data class LoadThumbnails(val ids: List<String> = emptyList()) : GalleryEvent()
    object SelectFiles : GalleryEvent()
    object UnselectFiles : GalleryEvent()
    data class EncryptFiles(val files: List<File>) : GalleryEvent()
    data class LoadMediaInfos(val ids: List<String> = emptyList()) : GalleryEvent()
    data class PreviewMedia(val id: String): GalleryEvent()
    object RefreshGallery : GalleryEvent()
    object ClearGallery : GalleryEvent()
}