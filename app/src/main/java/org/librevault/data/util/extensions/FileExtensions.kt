package org.librevault.data.util.extensions

import org.librevault.data.util.RandomNameGenerator
import org.librevault.domain.model.gallery.FileType
import org.librevault.domain.model.vault.FolderName
import org.librevault.domain.model.vault.VaultMediaInfo
import java.io.File

fun File.getVaultMediaInfo(): VaultMediaInfo {
    val file = this
    val id = RandomNameGenerator.generate()
    val fileType = FileType.parse(file)
    val filePath = file.absolutePath
    val fileParent = file.parent ?: "/"
    val fileName = file.nameWithoutExtension
    val fileSize = file.length()
    val extension = file.extension

    return VaultMediaInfo(
        id = id,
        filePath = filePath,
        fileName = fileName,
        fileExtension = extension,
        fileSize = fileSize,
        parentFolder = fileParent,
        dateAdded = System.currentTimeMillis(),
        fileType = fileType,
        folders = buildList {
            val type = FileType.parse(this@getVaultMediaInfo)
            if (type == FileType.IMAGE) add(FolderName.IMAGES) else if (type == FileType.VIDEO) add(
                FolderName.VIDEOS
            )
        }
    )
}