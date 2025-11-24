package org.librevault.domain.model.vault

import org.librevault.domain.model.vault.aliases.VaultFiles

typealias VaultItemId = Triple<List<String>, List<String>, List<String>>

fun VaultFiles.resolveVaultIds(): VaultItemId = Triple(
    first = first.map { it.nameWithoutExtension },
    second = second.map { it.nameWithoutExtension },
    third = third.map { it.nameWithoutExtension }
)