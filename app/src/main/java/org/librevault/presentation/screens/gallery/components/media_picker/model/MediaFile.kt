package org.librevault.presentation.screens.gallery.components.media_picker.model

import android.net.Uri
import java.io.File

data class MediaFile(
    val uri: Uri,
    val file: File?,
)