package org.librevault.domain.use_case_bundle

import org.librevault.domain.use_case.vault.DecryptMediaById
import org.librevault.domain.use_case.vault.GetMediaInfoById

data class PreviewUseCases(
    val getMediaInfoById: GetMediaInfoById,
    val decryptMediaById: DecryptMediaById,
)
