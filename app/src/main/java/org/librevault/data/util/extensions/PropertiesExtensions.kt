package org.librevault.data.util.extensions

import org.librevault.common.vault_consts.VaultInfoKeys
import org.librevault.domain.model.gallery.FileType
import org.librevault.domain.model.vault.VaultItemInfo
import java.util.Properties

fun Properties.toVaultItemInfo(): VaultItemInfo = VaultItemInfo(
    id = getProperty(VaultInfoKeys.VAULT_FILE_NAME),
    filePath = getProperty(VaultInfoKeys.ORIGINAL_PATH),
    fileName = getProperty(VaultInfoKeys.ORIGINAL_FILE_NAME),
    fileExtension = getProperty(VaultInfoKeys.FILE_EXTENSION),
    parentFolder = getProperty(VaultInfoKeys.PARENT_FOLDER),
    dateAdded = getProperty(VaultInfoKeys.DATE_ADDED).toLong(),
    fileType = FileType.parse(getProperty(VaultInfoKeys.FILE_TYPE))
)