package org.librevault.domain.model.vault

import org.librevault.domain.model.gallery.MediaId
import java.io.File

typealias TempFile = File

val TempFile.mediaId: MediaId
    get() = MediaId(this.nameWithoutExtension.substring(0, 16))