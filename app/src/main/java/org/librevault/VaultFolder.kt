package org.librevault

@JvmInline
value class VaultFolder(private val name: String) {
    operator fun invoke() = name
}

fun String.toVaultFolder() = VaultFolder(this)
fun FileType.toVaultFolder() = VaultFolder(name)