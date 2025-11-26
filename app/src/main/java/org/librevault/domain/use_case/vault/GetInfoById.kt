package org.librevault.domain.use_case.vault

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.librevault.domain.model.vault.VaultItemInfo
import org.librevault.domain.repository.vault.VaultRepository

class GetInfoById(
    private val vaultRepository: VaultRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) {
    operator fun invoke(id: String, onFailure: (Throwable) -> Unit, onSuccess: (VaultItemInfo) -> Unit) {
        scope.launch {
            vaultRepository.getInfoById(id)
                .onSuccess { onSuccess(it) }
                .onFailure { onFailure(it) }
        }
    }
}