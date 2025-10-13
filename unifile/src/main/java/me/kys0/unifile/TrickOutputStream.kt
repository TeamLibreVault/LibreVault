package me.kys0.unifile

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import org.jetbrains.annotations.Contract
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

// The OutputStream from Context.getContentResolver().openOutputStream()
// and FileProvider uri may throw Exception when write. The Exception looks like:
// java.io.IOException: write failed: EBADF (Bad file descriptor)
// But TrickOutputStream can avoid it on my Nexus 5 cm13.
// TODO need more test
internal class TrickOutputStream private constructor(
    private val mPfd: ParcelFileDescriptor,
    fd: FileDescriptor,
) : FileOutputStream(fd) {

    @Throws(IOException::class)
    override fun close() {
        mPfd.close()
        super.close()
    }

    companion object {
        @Contract("_, _, _ -> new")
        @Throws(IOException::class)
        fun create(context: Context, uri: Uri?, mode: String?): OutputStream {
            val pfd: ParcelFileDescriptor = try {
                uri?.let {
                    mode?.let { it1 ->
                        context.contentResolver.openFileDescriptor(
                            it,
                            it1
                        )
                    }
                }
            } catch (e: Exception) {
                throw IOException("Can't get ParcelFileDescriptor")
            } ?: throw IOException("Can't get ParcelFileDescriptor")

            val fd = pfd.fileDescriptor ?: throw IOException("Can't get FileDescriptor")
            return TrickOutputStream(pfd, fd)
        }
    }
}
