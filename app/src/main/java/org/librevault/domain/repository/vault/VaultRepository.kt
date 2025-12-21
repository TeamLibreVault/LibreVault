package org.librevault.domain.repository.vault

import kotlinx.coroutines.flow.Flow
import org.librevault.domain.model.vault.TempFile
import org.librevault.domain.model.vault.VaultMediaInfo
import java.io.File

interface VaultRepository {

    suspend fun addItems(files: List<File>): Result<List<VaultMediaInfo>>

    suspend fun deleteItemById(id: String): Throwable?

    fun getAllMediaInfo(): Flow<List<VaultMediaInfo>>

    suspend fun getMediaInfoById(id: String): Result<VaultMediaInfo>

    fun getMediaInfoByIds(ids: List<String>): Flow<List<VaultMediaInfo>>

    fun getAllThumbnails(): Flow<Result<List<TempFile>>>

    fun getThumbnailsByIds(ids: List<String>): Flow<Result<List<TempFile>>>

    suspend fun getMediaContentById(id: String): Result<TempFile>

    fun deleteMediaByIds(ids: List<String>): Result<Unit>
}