package org.librevault

import android.os.Environment

object Constants {
    val VAULT_FILE = Environment.getExternalStorageDirectory().resolve(".vault")

}
