package org.librevault.domain.use_case.vault

import kotlinx.coroutines.flow.last
import org.librevault.domain.repository.vault.VaultRepository

class GetAllThumbnailsById(
    private val vaultRepository: VaultRepository,
) {

    suspend operator fun invoke(ids: List<String>) =
        vaultRepository.getThumbnailsByIds(ids).last()
}