package org.librevault.utils

import android.content.Context
import org.librevault.domain.model.vault.TempFile

fun Context.createTempFile(
    prefix: String,
    suffix: String = ".tmp"
): TempFile = TempFile.createTempFile(prefix, suffix, this.cacheDir).apply { deleteOnExit() }
