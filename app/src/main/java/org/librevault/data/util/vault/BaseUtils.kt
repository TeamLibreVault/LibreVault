package org.librevault.data.util.vault

import org.librevault.common.vault_consts.VaultDirs
import org.librevault.data.encryption.BaseKeyCrypto
import org.librevault.data.encryption.SecureFileCipher

fun getBaseKey(): ByteArray {
    val baseKeyFile = resolveBaseKeyFile()
    val baseKey: ByteArray = if (baseKeyFile.exists()) {
        BaseKeyCrypto.decrypt(baseKeyFile)
    } else {
        val key = SecureFileCipher.generateBaseKey() // should return ByteArray
        BaseKeyCrypto.encrypt(key, baseKeyFile)
        key
    }
    return baseKey
}

fun resolveBaseKeyFile() = VaultDirs.ROOT.resolve("base")