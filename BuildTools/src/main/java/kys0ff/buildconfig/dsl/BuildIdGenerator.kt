package kys0ff.buildconfig.dsl

import java.util.zip.CRC32

fun String.runCommand(): String? = try {
    ProcessBuilder(*split(" ").toTypedArray())
        .redirectErrorStream(true)
        .start()
        .inputStream
        .bufferedReader()
        .readText()
        .trim()
        .takeIf { it.isNotEmpty() }
} catch (e: Exception) {
    e.printStackTrace()
    null
}

fun String?.formattedHex(): String {
    val crc = CRC32()
    crc.update(this?.toByteArray())
    return crc.value.toString(16).padStart(6, '0').takeLast(6)
}

fun getCommitHash():String? {
    val commitHash = "git rev-parse --short=12 HEAD".runCommand()
    return commitHash
}