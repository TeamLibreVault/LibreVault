package org.librevault.domain.use_case.vault

import kotlinx.coroutines.flow.last
import org.librevault.domain.repository.vault.VaultRepository

class GetAllThumbnails(
    private val vaultRepository: VaultRepository,
) {

    suspend operator fun invoke() =vaultRepository.getAllThumbnails().last()
}