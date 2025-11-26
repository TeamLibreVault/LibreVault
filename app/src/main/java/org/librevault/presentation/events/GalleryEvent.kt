package org.librevault.presentation.events

import java.io.File

sealed class GalleryEvent {
    object LoadThumbnails : GalleryEvent()
    object SelectFiles : GalleryEvent()
    object UnselectFiles : GalleryEvent()
    data class EncryptFiles(val files: List<File>) : GalleryEvent()
    data class DecryptInfo(val id: String) : GalleryEvent()
    data class PreviewMedia(val id: String): GalleryEvent()
    object RefreshGallery : GalleryEvent()
    object ClearGallery : GalleryEvent()
}