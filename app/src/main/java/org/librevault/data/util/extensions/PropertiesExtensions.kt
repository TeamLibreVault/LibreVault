package org.librevault.data.util.extensions

import com.google.gson.Gson
import org.librevault.domain.model.vault.VaultItemInfo

fun String.fromJsonToVaultItemInfo(): VaultItemInfo = Gson().fromJson(this, VaultItemInfo::class.java)