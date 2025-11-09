package org.librevault

import java.io.ByteArrayOutputStream
import java.util.Properties

fun buildProperties(comment: String = "", block: Properties.() -> Unit): String {
    val properties = Properties()
    val out = ByteArrayOutputStream()

    block(properties)
    properties.store(out, comment)

    return out.toString("UTF-8")
}

fun String.toProperties(): Properties {
    val properties = Properties()
    properties.load(this.byteInputStream())
    return properties
}