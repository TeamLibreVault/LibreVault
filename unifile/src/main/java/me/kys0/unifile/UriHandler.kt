package me.kys0.unifile

import android.content.Context
import android.net.Uri

/**
 * A UriHandler is to get UniFile from custom uri for extensions
 */
interface UriHandler {
    /**
     * Create a [UniFile] representing the uri
     */
    fun fromUri(context: Context?, uri: Uri?): UniFile?
}
