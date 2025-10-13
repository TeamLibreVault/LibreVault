package me.kys0.unifile

import android.text.TextUtils
import android.webkit.MimeTypeMap
import java.util.Locale

internal object Utils {

    @JvmStatic
    fun getTypeForName(name: String): String? {
        if (TextUtils.isEmpty(name)) return null
        val lastDot = name.lastIndexOf('.')
        if (lastDot >= 0) {
            val extension = name.substring(lastDot + 1).lowercase(Locale.getDefault())
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            if (TextUtils.isEmpty(mime).not()) return mime
        }
        return "application/octet-stream"
    }

    /**
     * A normal Unix pathname does not contain consecutive slashes and does not end
     * with a slash. The empty string and "/" are special cases that are also
     * considered normal.
     */
    fun normalize(pathname: String): String {
        val n = pathname.length
        val normalized = pathname.toCharArray()
        var index = 0
        var prevChar = 0.toChar()
        for (i in 0 until n) {
            val current = normalized[i]
            // Remove duplicate slashes.
            if ((current == '/' && prevChar == '/').not()) normalized[index++] = current
            prevChar = current
        }

        // Omit the trailing slash, except when pathname == "/".
        if (prevChar == '/' && n > 1) index--
        return if (index != n) String(normalized, 0, index) else pathname
    }

    // Invariant: Both |parent| and |child| are normalized paths.
    @JvmStatic
    fun resolve(parent: String, child: String): String {
        if (child.isEmpty() || child == "/") return parent
        if (child[0] == '/') return if (parent == "/") child else parent + child
        return if (parent == "/") parent + child else "$parent/$child"
    }
}
