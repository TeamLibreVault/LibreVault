package org.librevault.domain.model.gallery

@JvmInline
value class MediaId(val value:String) {
    operator fun invoke() = value
}