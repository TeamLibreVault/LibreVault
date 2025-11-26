package org.librevault.data.util.extensions

import org.librevault.data.util.RandomNameGenerator
import org.librevault.domain.model.gallery.FileType
import org.librevault.domain.model.vault.VaultItemInfo
import java.io.File

fun File.getVaultItemInfo(): VaultItemInfo {
    val file = this
    val id = RandomNameGenerator.generate()
    val fileType = FileType.parse(file)
    val filePath = file.absolutePath
    val fileParent = file.parent ?: "/"
    val fileName = file.nameWithoutExtension
    val fileSize = file.length()
    val extension = file.extension

    return VaultItemInfo(
        id = id,
        filePath = filePath,
        fileName = fileName,
        fileExtension = extension,
        fileSize = fileSize,
        parentFolder = fileParent,
        dateAdded = System.currentTimeMillis(),
        fileType = fileType
    )
}