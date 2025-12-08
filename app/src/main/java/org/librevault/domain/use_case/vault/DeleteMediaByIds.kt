package org.librevault.domain.use_case.vault

import kotlinx.coroutines.launch
import org.librevault.domain.repository.vault.VaultRepository
import org.librevault.domain.use_case.utils.getUseCaseScope

class DeleteMediaByIds(
    private val vaultRepository: VaultRepository
) {

    private val scope = getUseCaseScope()

    operator fun invoke(ids: List<String>, onDeleted: () -> Unit) {
        scope.launch {
            vaultRepository.deleteMediaByIds(ids).onSuccess { onDeleted() }
        }
    }

}