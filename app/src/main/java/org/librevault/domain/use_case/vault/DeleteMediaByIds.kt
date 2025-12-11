package org.librevault.domain.use_case.vault

import kotlinx.coroutines.launch
import org.librevault.domain.repository.vault.VaultRepository
import org.librevault.domain.use_case.utils.getUseCaseScope
import org.librevault.presentation.aliases.DeleteSelection

class DeleteMediaByIds(
    private val vaultRepository: VaultRepository
) {

    private val scope = getUseCaseScope()

    operator fun invoke(ids: Set<DeleteSelection>, onDeleted: () -> Unit) {
        scope.launch {
            vaultRepository.deleteMediaByIds(ids.toList()).onSuccess { onDeleted() }
        }
    }

}