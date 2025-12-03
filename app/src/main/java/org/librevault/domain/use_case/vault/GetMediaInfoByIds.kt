package org.librevault.domain.use_case.vault

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import org.librevault.domain.model.vault.VaultItemInfo
import org.librevault.domain.repository.vault.VaultRepository
import org.librevault.domain.use_case.utils.getUseCaseScope

class GetMediaInfoByIds(
    private val vaultRepository: VaultRepository,
) {
    private val scope = getUseCaseScope()

    operator fun invoke(
        ids: List<String>,
        onSuccess: (List<VaultItemInfo>) -> Unit,
        onError: (Throwable) -> Unit,
        onCompletion: () -> Unit = {},
    ) {
        vaultRepository.getMediaInfoByIds(ids)
            .onEach { value ->
                withContext(Dispatchers.Main) { onSuccess(value) }
            }
            .catch { cause ->
                withContext(Dispatchers.Main) { onError(cause) }
            }
            .onCompletion {
                withContext(Dispatchers.Main) { onCompletion() }
            }
            .launchIn(scope)
    }
}