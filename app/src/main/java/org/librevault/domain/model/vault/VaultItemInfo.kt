package org.librevault.domain.model.vault

import org.librevault.domain.model.gallery.FileType
import org.librevault.common.vault_consts.VaultInfoKeys
import org.librevault.utils.buildProperties
import org.librevault.utils.emptyString
import org.librevault.utils.toProperties
import java.util.Properties

data class VaultItemInfo(
    val id: String,
    val filePath: String,
    val fileName: String,
    val fileExtension: String,
    val parentFolder: String,
    val dateAdded: Long = 0,
    val fileType: FileType,
) {
    override fun toString(): String = buildProperties("Info") {
        setProperty(
            VaultInfoKeys.FILE_TYPE,
            fileType()
        )
        setProperty(
            VaultInfoKeys.ORIGINAL_PATH,
            filePath
        )
        setProperty(VaultInfoKeys.PARENT_FOLDER, parentFolder)
        setProperty(
            VaultInfoKeys.ORIGINAL_FILE_NAME,
            fileName
        )
        setProperty(VaultInfoKeys.VAULT_FILE_NAME, id)
        setProperty(
            VaultInfoKeys.FILE_EXTENSION,
            fileExtension
        )
    }

    fun toProperties(): Properties = toString().toProperties()

    companion object {
        private const val NOT_FOUND = "Not found!"

        fun placeholder(): VaultItemInfo = VaultItemInfo(
            id = emptyString(),
            filePath = emptyString(),
            fileName = emptyString(),
            fileExtension = emptyString(),
            parentFolder = emptyString(),
            dateAdded = System.currentTimeMillis(),
            fileType = FileType.IMAGE
        )

        fun error(): VaultItemInfo = VaultItemInfo(
            id = NOT_FOUND,
            filePath = NOT_FOUND,
            fileName = NOT_FOUND,
            fileExtension = NOT_FOUND,
            parentFolder = NOT_FOUND,
            dateAdded = -1,
            fileType = FileType.ERROR
        )
    }
}