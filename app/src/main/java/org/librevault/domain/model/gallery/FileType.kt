package org.librevault.domain.model.gallery

import java.io.File

enum class FileType {
    IMAGE, VIDEO, ERROR;

    operator fun invoke() = name

    companion object {
        private val imageExtensions = listOf("jpg", "jpeg", "png", "webp", "webm")
        private val videoExtensions = listOf("mp4", "avi", "mkv", "mov", "wmv")

        fun parse(value: File?): FileType = when {
            imageExtensions.contains(value?.extension?.lowercase()) -> IMAGE
            videoExtensions.contains(value?.extension?.lowercase()) -> VIDEO
            else -> ERROR
        }

        fun parse(value: String): FileType = when (value) {
            "IMAGE" -> IMAGE
            "VIDEO" -> VIDEO
            else -> ERROR
        }
    }
}