package org.librevault.presentation.aliases

import org.librevault.common.state.SelectState
import org.librevault.common.state.UiState
import org.librevault.domain.model.vault.VaultItemContent
import org.librevault.domain.model.vault.VaultMediaInfo

typealias ThumbnailsListState = UiState<List<VaultItemContent>>
typealias ThumbnailsList = List<VaultItemContent>
typealias ThumbnailInfo = VaultMediaInfo

typealias ThumbnailInfoListState = UiState<List<ThumbnailInfo>>
typealias ThumbnailInfoList = List<ThumbnailInfo>

typealias EncryptedInfo = VaultMediaInfo
typealias EncryptListState = UiState<List<EncryptedInfo>>

typealias DeleteSelectionState = SelectState<DeleteSelection>
typealias MutableDeleteSelectionList = MutableList<String>
typealias DeleteSelection = String