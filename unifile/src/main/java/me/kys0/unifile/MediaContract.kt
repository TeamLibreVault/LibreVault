package me.kys0.unifile

import android.content.Context
import android.net.Uri
import android.provider.MediaStore

internal object MediaContract {

    fun getName(context: Context?, self: Uri?): String =
        context?.let {
            Contracts.queryForString(
                it,
                self,
                MediaStore.MediaColumns.DISPLAY_NAME,
                ""
            )
        }
            .toString()

    fun getType(context: Context?, self: Uri?): String =
        Contracts.queryForString(context!!, self, MediaStore.MediaColumns.MIME_TYPE, "")

    fun getFilePath(context: Context?, self: Uri?): String =
        Contracts.queryForString(context!!, self, MediaStore.MediaColumns.DATA, "")

    fun lastModified(context: Context?, self: Uri?): Long =
        Contracts.queryForLong(context!!, self, MediaStore.MediaColumns.DATE_MODIFIED, -1L)

    fun length(context: Context?, self: Uri?): Long =
        Contracts.queryForLong(context!!, self, MediaStore.MediaColumns.SIZE, -1L)

}
