package org.librevault.utils

import java.io.ByteArrayOutputStream
import java.util.Properties

fun buildProperties(comment: String = emptyString(), block: Properties.() -> Unit): String {
    val properties = Properties()
    val out = ByteArrayOutputStream()

    block(properties)
    properties.store(out, comment)

    return out.toString(Charsets.UTF_8)
}

fun String.toProperties(): Properties {
    val properties = Properties()
    properties.load(this.byteInputStream())
    return properties
}