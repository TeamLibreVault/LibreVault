package org.librevault.data.use_case.gallery

import org.librevault.data.use_case.AddItems
import org.librevault.data.use_case.GetAllThumbnails
import org.librevault.data.use_case.GetInfoById

data class GalleryUseCases(
    val addItems: AddItems,
    val getAllThumbnails: GetAllThumbnails,
    val getInfoById: GetInfoById,
)
