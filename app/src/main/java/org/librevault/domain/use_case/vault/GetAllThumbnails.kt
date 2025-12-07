package org.librevault.domain.use_case.vault

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import org.librevault.domain.model.vault.VaultItemContent
import org.librevault.domain.repository.vault.VaultRepository
import org.librevault.domain.use_case.utils.getUseCaseScope

class GetAllThumbnails(
    private val vaultRepository: VaultRepository,
) {

    private val coroutineScope = getUseCaseScope()

    operator fun invoke(
        onThumbsDecrypted: (List<VaultItemContent>) -> Unit = {},
        onError: (Throwable) -> Unit,
        onCompletion: () -> Unit = {},
    ) {
        vaultRepository.getAllThumbnails()
            .onEach { value ->
                withContext(Dispatchers.Main) {
                    value.onSuccess { onThumbsDecrypted(it) }.onFailure { onError(it) }
                }
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