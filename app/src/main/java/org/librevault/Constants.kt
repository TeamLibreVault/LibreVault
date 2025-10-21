package org.librevault

import android.os.Environment

object Constants {

    object Vault {
        val ROOT = Environment.getExternalStorageDirectory().resolve(".vault")
        val THUMBS = ROOT.resolve("thm")
        val DATA = ROOT.resolve("dt")
        val INFO = ROOT.resolve("in")

        object InfoKeys {
            const val ORIGINAL_PATH = "op"
            const val PARENT_FOLDER = "pf"
            const val ORIGINAL_FILE_NAME = "fn"
            const val VAULT_FILE_NAME = "ofn"
            const val FILE_EXTENSION = "fe"
            const val FILE_TYPE = "ft"
        }
    }

}
