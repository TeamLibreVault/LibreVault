package org.librevault.domain.model.vault.aliases

import org.librevault.common.vault_consts.VaultDirs
import java.io.File

typealias VaultFolders = Triple<File, File, File>

fun resolveVaultFolders(id: String): VaultFolders = Triple(
    first = VaultDirs.INFO.resolve(id),
    second = VaultDirs.THUMBS.resolve(id),
    third = VaultDirs.DATA.resolve(id)
)

fun resolveVaultInfo(id: String): File = resolveVaultFolders(id).first
fun resolveVaultThumb(id: String): File = resolveVaultFolders(id).second
fun resolveVaultData(id: String): File = resolveVaultFolders(id).third

