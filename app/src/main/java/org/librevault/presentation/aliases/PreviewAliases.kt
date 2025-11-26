package org.librevault.presentation.aliases

import org.librevault.common.state.UiState
import org.librevault.domain.model.vault.VaultItemContent
import org.librevault.domain.model.vault.VaultItemInfo

typealias MediaInfo = VaultItemInfo
typealias MediaInfoState = UiState<MediaInfo>

typealias MediaContent = VaultItemContent
typealias MediaContentState = UiState<MediaContent>