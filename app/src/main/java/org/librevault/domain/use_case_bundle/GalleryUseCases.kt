package org.librevault.domain.use_case_bundle

import org.librevault.domain.use_case.vault.AddItems
import org.librevault.domain.use_case.vault.GetAllThumbnails
import org.librevault.domain.use_case.vault.GetInfoById

data class GalleryUseCases(
    val addItems: AddItems,
    val getAllThumbnails: GetAllThumbnails,
    val getInfoById: GetInfoById,
)