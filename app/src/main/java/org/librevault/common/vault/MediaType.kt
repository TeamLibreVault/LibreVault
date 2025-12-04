package org.librevault.common.vault

sealed class MediaType(private val name: String) {
    object Image: MediaType("image")
    object Video: MediaType("video")
    data class Other(val type: String): MediaType(type)

    operator fun invoke() = name
}