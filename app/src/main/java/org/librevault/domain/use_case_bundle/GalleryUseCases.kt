package org.librevault.domain.use_case_bundle

import org.librevault.domain.use_case.vault.AddItems
import org.librevault.domain.use_case.vault.GetAllThumbnails
import org.librevault.domain.use_case.vault.GetAllThumbnailsById
import org.librevault.domain.use_case.vault.GetMediaInfoById

data class GalleryUseCases(
    val addItems: AddItems,
    val getAllThumbnailsById: GetAllThumbnailsById,
    val getAllThumbnails: GetAllThumbnails,
    val getMediaInfoById: GetMediaInfoById,
)