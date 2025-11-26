package org.librevault.domain.use_case.vault

import kotlinx.coroutines.launch
import org.librevault.domain.model.vault.VaultItemInfo
import org.librevault.domain.repository.vault.VaultRepository
import org.librevault.domain.use_case.utils.getUseCaseScope

class GetMediaInfoById(
    private val vaultRepository: VaultRepository,
) {
    private val scope = getUseCaseScope()

    operator fun invoke(
        id: String,
        onFailure: (Throwable) -> Unit,
        onSuccess: (VaultItemInfo) -> Unit,
    ) {
        scope.launch {
            vaultRepository.getMediaInfoById(id)
                .onSuccess { onSuccess(it) }
                .onFailure { onFailure(it) }
        }
    }
}