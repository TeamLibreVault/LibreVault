package org.librevault.presentation.aliases

import org.librevault.common.state.UiState
import org.librevault.domain.model.vault.VaultItemContent
import org.librevault.domain.model.vault.VaultItemInfo

typealias ThumbnailsListState = UiState<List<VaultItemContent>>
typealias ThumbnailsList = List<VaultItemContent>
typealias ThumbnailInfo = VaultItemInfo

typealias ThumbnailInfoListState = UiState<List<ThumbnailInfo>>
typealias ThumbnailInfoList = List<ThumbnailInfo>

typealias EncryptedInfo = VaultItemInfo
typealias EncryptListState = UiState<List<EncryptedInfo>>