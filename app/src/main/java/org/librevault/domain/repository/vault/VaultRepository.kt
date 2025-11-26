package org.librevault.domain.repository.vault

import kotlinx.coroutines.flow.Flow
import org.librevault.domain.model.vault.VaultItemContent
import org.librevault.domain.model.vault.VaultItemInfo
import java.io.File

interface VaultRepository {

    suspend fun addItems(files: List<File>): Result<List<VaultItemInfo>>

    suspend fun deleteItemById(id: String): Throwable?

    suspend fun getMediaInfoById(id: String): Result<VaultItemInfo>

    fun getAllMediaInfo(): Flow<List<VaultItemInfo>>

    fun getAllThumbnails(): Flow<List<VaultItemContent>>

    suspend fun getMediaContentById(id: String): Result<VaultItemContent>
}