package org.librevault.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.librevault.data.encryption.SecureFileCipher
import org.librevault.data.util.extensions.getVaultItemInfo
import org.librevault.data.util.extensions.toVaultItemContent
import org.librevault.data.util.extensions.toVaultItemInfo
import org.librevault.data.util.vault.getBaseKey
import org.librevault.domain.model.vault.VaultItemContent
import org.librevault.domain.model.vault.VaultItemInfo
import org.librevault.domain.model.vault.aliases.resolveVaultData
import org.librevault.domain.model.vault.aliases.resolveVaultFiles
import org.librevault.domain.model.vault.aliases.resolveVaultFolders
import org.librevault.domain.model.vault.aliases.resolveVaultInfo
import org.librevault.domain.model.vault.aliases.resolveVaultThumb
import org.librevault.domain.repository.VaultRepository
import org.librevault.utils.toProperties
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val TAG = "VaultRepositoryImpl"

class VaultRepositoryImpl(
    private val mediaThumbnailer: MediaThumbnailer
) : VaultRepository {
    override suspend fun addItems(files: List<File>): Result<List<VaultItemInfo>> = runCatching {
        val infos = mutableListOf<VaultItemInfo>()

        for (file in files) {
            val vaultInfo = file.getVaultItemInfo()
            val (infoOutput, thumbOutput, originalOutput) = resolveVaultFolders(vaultInfo.id)
            val thumbBytes = mediaThumbnailer.compress(file) ?: byteArrayOf()
            val baseKey = getBaseKey()

            // Encrypt info and thumbnail synchronously
            SecureFileCipher.encryptBytes(
                inputBytes = vaultInfo.toString().encodeToByteArray(),
                outputFile = infoOutput,
                key = baseKey
            )
            SecureFileCipher.encryptBytes(
                inputBytes = thumbBytes,
                outputFile = thumbOutput,
                key = baseKey
            )

            // Encrypt original file asynchronously and await completion
            suspendCancellableCoroutine { cont ->
                SecureFileCipher.encryptFile(
                    inputFile = file,
                    outputFile = originalOutput,
                    key = baseKey,
                    onComplete = {
                        cont.resume(Unit) { _, _, _ -> }
                    },
                    onError = { error ->
                        cont.resumeWithException(error)
                    }
                )
            }

            infos += vaultInfo
            baseKey.fill(0)
        }

        infos
    }

    override suspend fun deleteItemById(id: String): Throwable? = runCatching {
        resolveVaultInfo(id).delete()
        resolveVaultThumb(id).delete()
        resolveVaultData(id).delete()
    }.exceptionOrNull()

    override suspend fun getInfoById(id: String): Result<VaultItemInfo> =
        suspendCoroutine { continuation ->
            runCatching {
                val baseKey = getBaseKey()
                val info = SecureFileCipher.decryptToBytes(
                    inputFile = resolveVaultInfo(id),
                    key = baseKey
                ).decodeToString().toProperties().toVaultItemInfo()
                baseKey.fill(0)
                continuation.resume(Result.success(info))
            }.onFailure { continuation.resume(Result.failure(it)) }
        }

    override fun getAllInfos(): Flow<List<VaultItemInfo>> = flow {
        val vaultInfos = mutableListOf<VaultItemInfo>()

        val vaultInfoFiles = resolveVaultFiles().second
        val baseKey = getBaseKey()

        vaultInfoFiles.forEach { thumbFile ->
            val id = thumbFile.nameWithoutExtension

            vaultInfos += SecureFileCipher.decryptToBytes(
                inputFile = resolveVaultInfo(id),
                key = baseKey
            ).decodeToString().toProperties().toVaultItemInfo()
        }
        baseKey.fill(0)
        emit(vaultInfos)
    }

    override fun getAllThumbnails(): Flow<List<VaultItemContent>> = flow {
        val vaultThumbs = mutableListOf<VaultItemContent>()

        val vaultThumbFiles = resolveVaultFiles().second
        Log.d(TAG, "getAllThumbnails: Thumbnails: ${vaultThumbFiles.size}")
        val baseKey = getBaseKey()

        vaultThumbFiles.forEach { thumbFile ->
            val id = thumbFile.nameWithoutExtension

            vaultThumbs += SecureFileCipher.decryptToBytes(
                inputFile = resolveVaultThumb(id),
                key = baseKey
            ).toVaultItemContent(id)
        }
        baseKey.fill(0)
        emit(vaultThumbs)
    }

    override suspend fun loadContentById(id: String): VaultItemContent {
        val baseKey = getBaseKey()
        val decryptedContent = SecureFileCipher.decryptToBytes(
            inputFile = resolveVaultInfo(id),
            key = baseKey
        ).toVaultItemContent(id)
        baseKey.fill(0)
        return decryptedContent
    }
}