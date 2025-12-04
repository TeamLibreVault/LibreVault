package org.librevault.domain.use_case.vault

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.librevault.domain.model.vault.VaultMediaInfo
import org.librevault.domain.repository.vault.VaultRepository
import java.io.File

class AddItems(
    private val vaultRepository: VaultRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) {
    operator fun invoke(
        files: List<File>,
        onFailure: (Throwable) -> Unit,
        onSuccess: (List<VaultMediaInfo>) -> Unit,
    ) {
        scope.launch {
            vaultRepository.addItems(files)
                .onFailure { onFailure(it) }
                .onSuccess { onSuccess(it) }
        }
    }
}