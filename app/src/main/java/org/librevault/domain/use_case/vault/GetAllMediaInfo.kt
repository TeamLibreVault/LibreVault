package org.librevault.domain.use_case.vault

import kotlinx.coroutines.flow.first
import org.librevault.domain.repository.vault.VaultRepository

class GetAllMediaInfo(
    private val vaultRepository: VaultRepository
) {

    suspend operator fun invoke() = vaultRepository.getAllMediaInfo().first()
}