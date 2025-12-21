package org.librevault.presentation.aliases

import org.librevault.common.state.UiState
import org.librevault.domain.model.vault.TempFile
import org.librevault.domain.model.vault.VaultItemContent
import org.librevault.domain.model.vault.VaultMediaInfo

typealias MediaInfo = VaultMediaInfo
typealias MediaInfoState = UiState<MediaInfo>

typealias MediaContent = VaultItemContent
typealias MediaContentState = UiState<TempFile>