package org.librevault.domain.model.vault.aliases

import org.librevault.common.vault_consts.VaultDirs
import java.io.File

typealias VaultFiles = Triple<List<File>, List<File>, List<File>>

fun resolveVaultFiles(): VaultFiles = Triple(
    first = VaultDirs.INFO.listFiles()
        ?.filterNotNull()
        ?.filter { it.extension.isEmpty() }
        ?: emptyList(),
    second = VaultDirs.THUMBS.listFiles()
        ?.filterNotNull()
        ?.filter { it.extension.isEmpty() }
        ?: emptyList(),
    third = VaultDirs.DATA.listFiles()
        ?.filterNotNull()
        ?.filter { it.extension.isEmpty() }
        ?: emptyList()
)