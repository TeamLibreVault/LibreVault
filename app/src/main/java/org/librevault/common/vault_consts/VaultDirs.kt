package org.librevault.common.vault_consts

import android.os.Environment

object VaultDirs {
    val ROOT = Environment.getExternalStorageDirectory().resolve(".vault")
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