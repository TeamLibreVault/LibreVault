package org.librevault.data.util

object RandomNameGenerator {
    private const val CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

    fun generate(length: Int = 12): String {
        val randomPart = (1..length)
            .map { CHARS.random() }
            .joinToString("")

        val prefixSuffix = "=="

        return "$prefixSuffix$randomPart$prefixSuffix"
    }
}