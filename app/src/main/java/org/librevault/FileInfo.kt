package org.librevault

import java.util.Properties

data class FileInfo(
    val originalPath: String,
    val parentFolder: String,
    val originalFileName:String,
    val vaultFileName:String,
    val fileExtension:String,
    val fileType: FileType?
) {
    companion object {
        fun parseValue(properties: Properties): FileInfo {
            fun get(key:String)=properties.getProperty(key)
            return FileInfo(
                originalPath = get(Constants.Vault.InfoKeys.ORIGINAL_PATH),
                parentFolder = get(Constants.Vault.InfoKeys.PARENT_FOLDER),
                originalFileName = get(Constants.Vault.InfoKeys.ORIGINAL_FILE_NAME),
                vaultFileName = get(Constants.Vault.InfoKeys.VAULT_FILE_NAME),
                fileExtension = get(Constants.Vault.InfoKeys.FILE_EXTENSION),
                fileType = FileType.parse(get(Constants.Vault.InfoKeys.FILE_TYPE))
            )
        }
    }
}

fun Properties.toFileInfo() = FileInfo.parseValue(this)