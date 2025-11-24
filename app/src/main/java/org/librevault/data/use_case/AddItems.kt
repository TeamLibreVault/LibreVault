package org.librevault.data.use_case

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.librevault.domain.model.vault.VaultItemInfo
import org.librevault.domain.repository.VaultRepository
import java.io.File

class AddItems(
    private val vaultRepository: VaultRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) {
    operator fun invoke(
        files: List<File>,
        onFailure: (Throwable) -> Unit,
        onSuccess: (List<VaultItemInfo>) -> Unit,
    ) {
        scope.launch {
            vaultRepository.addItems(files)
                .onFailure { onFailure(it) }
                .onSuccess { onSuccess(it) }
        }
    }
}