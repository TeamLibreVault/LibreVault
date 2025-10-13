package me.kys0.unifile

import java.io.Closeable
import java.io.IOException

internal object IOUtils {
    /**
     * Close the closeable stuff. Don't worry about anything.
     *
     * @param closeable the closeable stuff
     */
    @JvmStatic
    fun closeQuietly(closeable: Closeable?) {
        if (closeable != null) try {
            closeable.close()
        } catch (_: IOException) {
        }
    }
}
