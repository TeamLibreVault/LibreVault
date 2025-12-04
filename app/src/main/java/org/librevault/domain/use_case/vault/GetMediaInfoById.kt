package org.librevault.domain.use_case.vault

import kotlinx.coroutines.launch
import org.librevault.domain.model.vault.VaultMediaInfo
import org.librevault.domain.repository.vault.VaultRepository
import org.librevault.domain.use_case.utils.getUseCaseScope

class GetMediaInfoById(
    private val vaultRepository: VaultRepository,
) {
    private val scope = getUseCaseScope()

    operator fun invoke(
        id: String,
        onFailure: (Throwable) -> Unit,
        onSuccess: (VaultMediaInfo) -> Unit,
    ) {
        scope.launch {
            vaultRepository.getMediaInfoById(id)
                .onSuccess { onSuccess(it) }
                .onFailure { onFailure(it) }
        }
    }
}