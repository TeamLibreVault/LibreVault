package org.librevault.presentation.aliases

import org.librevault.common.state.UiState
import org.librevault.domain.model.vault.VaultItemContent
import org.librevault.domain.model.vault.VaultItemInfo

typealias ThumbnailsListState = UiState<List<VaultItemContent>>
typealias ThumbnailsList = List<VaultItemContent>
typealias ThumbnailInfo = VaultItemInfo

typealias InfoState = UiState<ThumbnailInfo>

typealias EncryptedInfo = VaultItemInfo
typealias EncryptListState = UiState<List<EncryptedInfo>>