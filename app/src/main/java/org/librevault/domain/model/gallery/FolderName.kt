package org.librevault.domain.model.gallery

import org.librevault.domain.model.gallery.FileType

@JvmInline
value class FolderName(private val name: String) {
    operator fun invoke() = name
}

fun String.toFolderName() = FolderName(this)
fun FileType.toFolderName() = FolderName(name)