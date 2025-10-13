package org.librevault

import android.os.Environment
import java.io.File

object VaultFolder {

    val path = File(Environment.getExternalStorageDirectory(), ".vault")

    fun init() {
        if (path.exists().not())
            path.mkdirs()
    }

}