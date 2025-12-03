package org.librevault.domain.use_case_bundle

import org.librevault.domain.use_case.vault.AddItems
import org.librevault.domain.use_case.vault.GetAllMediaInfo
import org.librevault.domain.use_case.vault.GetAllThumbnails
import org.librevault.domain.use_case.vault.GetAllThumbnailsById
import org.librevault.domain.use_case.vault.GetMediaInfoByIds

data class GalleryUseCases(
    val addItems: AddItems,
    val getAllThumbnailsById: GetAllThumbnailsById,
    val getAllThumbnails: GetAllThumbnails,
    val getAllMediaInfo: GetAllMediaInfo,
    val getMediaInfoByIds: GetMediaInfoByIds
)