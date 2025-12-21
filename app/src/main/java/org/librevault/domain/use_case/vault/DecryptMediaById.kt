package org.librevault.domain.use_case.vault

import kotlinx.coroutines.launch
import org.librevault.domain.model.vault.TempFile
import org.librevault.domain.repository.vault.VaultRepository
import org.librevault.domain.use_case.utils.getUseCaseScope

class DecryptMediaById(
    private val vaultRepository: VaultRepository,
) {
    private val scope = getUseCaseScope()

    operator fun invoke(
        id: String,
        onFailure: (Throwable) -> Unit,
        onSuccess: (TempFile) -> Unit,
    ) {
        scope.launch {
            vaultRepository.getMediaContentById(id)
                .onSuccess { onSuccess(it) }
                .onFailure { onFailure(it) }
        }
    }
}