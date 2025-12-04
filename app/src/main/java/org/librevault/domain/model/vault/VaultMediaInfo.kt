package org.librevault.domain.model.vault

import com.google.gson.Gson
import org.librevault.domain.model.gallery.FileType
import org.librevault.utils.emptyString
import java.util.Locale

data class VaultMediaInfo(
    val id: String,
    val filePath: String,
    val fileName: String,
    val fileExtension: String,
    val fileSize: Long,
    val parentFolder: String,
    val dateAdded: Long,
    val fileType: FileType,
    val folders: List<FolderName>,
) {
    override fun toString(): String = Gson().toJson(this)

    val formattedFileSize: String
        get() {
            val size = fileSize.toDouble()
            val locale = Locale.getDefault()
            return when {
                size < 1024 -> String.format(locale, "%.2f B", size)
                size < 1024 * 1024 -> String.format(locale, "%.2f KB", size / 1024)
                size < 1024 * 1024 * 1024 -> String.format(locale, "%.2f MB", size / (1024 * 1024))
                else -> String.format(locale, "%.2f GB", size / (1024 * 1024 * 1024))
            }
        }

    companion object {
        private const val NOT_FOUND = "Not found!"

        fun placeholder(): VaultMediaInfo = VaultMediaInfo(
            id = emptyString(),
            filePath = emptyString(),
            fileName = emptyString(),
            fileExtension = emptyString(),
            fileSize = 0L,
            parentFolder = emptyString(),
            dateAdded = System.currentTimeMillis(),
            fileType = FileType.IMAGE,
            folders = emptyList()
        )

        fun error(): VaultMediaInfo = VaultMediaInfo(
            id = NOT_FOUND,
            filePath = NOT_FOUND,
            fileName = NOT_FOUND,
            fileExtension = NOT_FOUND,
            fileSize = -1L,
            parentFolder = NOT_FOUND,
            dateAdded = -1L,
            fileType = FileType.ERROR,
            folders = emptyList()
        )
    }
}

fun String.fromJsonToVaultMediaInfo(): VaultMediaInfo = Gson().fromJson(this, VaultMediaInfo::class.java)