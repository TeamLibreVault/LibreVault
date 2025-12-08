package org.librevault.utils

fun emptyString(): String = ""

inline fun String.appendIf(condition: Boolean, block: () -> String): String = if (condition) this + block() else this