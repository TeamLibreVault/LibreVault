package org.librevault.domain.repository

import kotlinx.coroutines.flow.Flow
import org.librevault.domain.model.vault.VaultItemContent
import org.librevault.domain.model.vault.VaultItemInfo
import java.io.File

interface VaultRepository {

    suspend fun addItems(files: List<File>): Result<List<VaultItemInfo>>

    suspend fun deleteItemById(id: String): Throwable?

    suspend fun getInfoById(id: String): Result<VaultItemInfo>

    fun getAllInfos(): Flow<List<VaultItemInfo>>

    fun getAllThumbnails(): Flow<List<VaultItemContent>>

    suspend fun loadContentById(id: String): VaultItemContent
}

