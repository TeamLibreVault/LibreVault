package org.librevault

import java.io.File

enum class FileType {
    IMAGE, VIDEO;

    operator fun invoke() = name

    companion object {
        private val imageExtensions = listOf("jpg", "jpeg", "png", "webp")
        private val videoExtensions = listOf("mp4", "avi", "mkv", "mov", "wmv")

        fun parse(value: File): FileType? = when {
            imageExtensions.contains(value.extension.lowercase()) -> IMAGE
            videoExtensions.contains(value.extension.lowercase()) -> VIDEO
            else -> null
        }

        fun parse(value: String): FileType? = when (value) {
            "IMAGE" -> IMAGE
            "VIDEO" -> VIDEO
            else -> null
        }
    }
}