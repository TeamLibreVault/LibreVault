package org.librevault

import android.os.Environment

object Constants {
    val VAULT_FILE = Environment.getExternalStorageDirectory().resolve(".vault")

    object Vault {
        val ROOT = Environment.getExternalStorageDirectory().resolve(".vault")
        val THUMBS = ROOT.resolve("thm")
        val DATA = ROOT.resolve("dt")
    }

}
