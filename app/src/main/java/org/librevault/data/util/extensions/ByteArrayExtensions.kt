package org.librevault.data.util.extensions

import org.librevault.domain.model.vault.VaultItemContent

fun ByteArray.toVaultItemContent(id: String) = VaultItemContent(
    id = id,
    data = this
)