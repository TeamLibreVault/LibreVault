package org.librevault.data.use_case

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import org.librevault.domain.model.vault.VaultItemContent
import org.librevault.domain.repository.VaultRepository

class GetAllThumbnails(
    private val vaultRepository: VaultRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) {

    operator fun invoke(
        onThumbsDecrypted: (List<VaultItemContent>) -> Unit = {},
        onError: (Throwable) -> Unit,
        onCompletion: () -> Unit = {},
    ) {
        vaultRepository.getAllThumbnails()
            .onEach { value ->
                withContext(Dispatchers.Main) { onThumbsDecrypted(value) }
            }
            .catch { cause ->
                withContext(Dispatchers.Main) { onError(cause) }
            }
            .onCompletion {
                withContext(Dispatchers.Main) { onCompletion() }
            }
            .launchIn(coroutineScope)
    }

}