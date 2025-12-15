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

data class MediaThumbnail(
    val info: ThumbnailInfo,
    val content: ByteArray
) {

    fun toVaultItemContent() =  VaultItemContent(
        id = info.id,
        data = content
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaThumbnail

        if (info != other.info) return false
        if (!content.contentEquals(other.content)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = info.hashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }
}