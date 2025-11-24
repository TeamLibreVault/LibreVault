package org.librevault.presentation.events.gallery

import java.io.File

sealed class GalleryEvent {
    object LoadThumbnails : GalleryEvent()
    object SelectFiles : GalleryEvent()
    object UnselectFiles : GalleryEvent()
    data class EncryptFiles(val files: List<File>) : GalleryEvent()
    data class DecryptInfo(val id: String) : GalleryEvent()
    object RefreshGallery : GalleryEvent()
    object ClearGallery : GalleryEvent()
}