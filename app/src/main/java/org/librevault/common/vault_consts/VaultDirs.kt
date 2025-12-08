package org.librevault.common.vault_consts

import android.os.Environment
import org.librevault.BuildConfig
import org.librevault.utils.appendIf

object VaultDirs {
    val ROOT = Environment.getExternalStorageDirectory().resolve(".vault".appendIf(BuildConfig.DEBUG) { "d" })
    val THUMBS = ROOT.resolve("thm")
    val DATA = ROOT.resolve("dt")
    val INFO = ROOT.resolve("in")

    fun initVaultDirs() {
        apply {
            if (ROOT.exists().not()) ROOT.mkdirs()
            if (THUMBS.exists().not()) THUMBS.mkdirs()
            if (DATA.exists().not()) DATA.mkdirs()
            if (INFO.exists().not()) INFO.mkdirs()
        }
    }
}