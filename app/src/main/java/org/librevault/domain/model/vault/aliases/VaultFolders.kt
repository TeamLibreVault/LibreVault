package org.librevault.domain.model.vault.aliases

import org.librevault.common.vault_consts.VaultDirs
import java.io.File

typealias VaultFolders = Triple<File, File, File>

fun resolveVaultFolders(id: String): VaultFolders = Triple(
    first = VaultDirs.INFO.resolve(id),
    second = VaultDirs.THUMBS.resolve(id),
    third = VaultDirs.DATA.resolve(id)
)

fun resolveVaultThumb(id: String): File = VaultDirs.THUMBS.resolve(id)
fun resolveVaultInfo(id: String): File = VaultDirs.INFO.resolve(id)
fun resolveVaultData(id: String): File = VaultDirs.DATA.resolve(id)

