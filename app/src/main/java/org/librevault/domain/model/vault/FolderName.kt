package org.librevault.domain.model.vault

@JvmInline
value class FolderName(private val name: String) {
    operator fun invoke() = name

    companion object {
        val IMAGES = FolderName("Images")
        val VIDEOS = FolderName("Videos")
    }
}

fun String.toFolderName() = FolderName(this)