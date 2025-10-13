package me.kys0.unifile

import android.annotation.SuppressLint
import android.content.res.AssetFileDescriptor
import android.os.ParcelFileDescriptor
import org.jetbrains.annotations.Contract
import timber.log.Timber
import java.io.FileDescriptor
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 *  @noinspection ALL
 */
@SuppressLint("SoonBlockedPrivateApi", "ThrowableNotAtBeginning")
internal class TrickRandomAccessFile private constructor(mode: String) :
    RandomAccessFile("/dev/random", mode) {

    private var mPfd: ParcelFileDescriptor? = null
    private var mAfd: AssetFileDescriptor? = null

    @Throws(IOException::class)
    override fun close() {
        if (mPfd != null) {
            mPfd?.close()
            mPfd = null
        }
        if (mAfd != null) {
            mAfd?.close()
            mAfd = null
        }
        super.close()
    }

    companion object {
        private val TAG = TrickRandomAccessFile::class.java.simpleName
        private var FIELD_FD: Field? = null
        private var METHOD_CLOSE: Method? = null

        init {
            var field: Field?
            try {
                field = RandomAccessFile::class.java.getDeclaredField("fd")
                field.isAccessible = true
            } catch (e: NoSuchFieldException) {
                Timber.tag(TAG).e("Can't get field RandomAccessFile.fd : %s", e)
                field = null
            }
            FIELD_FD = field
            val method: Method? = try {
                val clazz = Class.forName("libcore.io.IoUtils")
                clazz.getMethod("close", FileDescriptor::class.java)
            } catch (e: ClassNotFoundException) {
                Timber.tag(TAG).e("Can't get class libcore.io.IoUtils: %s", e)
                null
            } catch (e: NoSuchMethodException) {
                Timber.tag(TAG)
                    .e("Can't get method libcore.io.IoUtils.close(FileDescriptor): %s", e)
                null
            }
            METHOD_CLOSE = method
        }

        @Throws(IOException::class)
        private fun checkReflection() {
            // Check reflection stuff
            if ((FIELD_FD == null) or (METHOD_CLOSE == null))
                throw IOException("Can't get reflection stuff")
        }

        @JvmStatic
        @Contract("null, _ -> fail")
        @Throws(IOException::class)
        fun create(pfd: ParcelFileDescriptor?, mode: String): RandomAccessFile {
            if (pfd == null) throw IOException("ParcelFileDescriptor is null")
            checkReflection()
            return try {
                val fd = pfd.fileDescriptor ?: throw IOException("Can't get FileDescriptor")
                val file = create(fd, mode)
                file.mPfd = pfd
                file
            } catch (e: IOException) {
                // Close ParcelFileDescriptor if failed
                pfd.close()
                throw e
            }
        }

        @JvmStatic
        @Throws(IOException::class)
        fun create(afd: AssetFileDescriptor?, mode: String): RandomAccessFile {
            if (afd == null) throw IOException("AssetFileDescriptor is null")
            checkReflection()
            return try {
                val fd = afd.fileDescriptor ?: throw IOException("Can't get FileDescriptor")
                val file = create(fd, mode)
                file.mAfd = afd
                file
            } catch (e: IOException) {
                // Close AssetFileDescriptor if failed
                afd.close()
                throw e
            }
        }

        @Throws(IOException::class)
        private fun create(fd: FileDescriptor, mode: String): TrickRandomAccessFile {
            // Create TrickRandomAccessFile object
            val file: TrickRandomAccessFile = try {
                TrickRandomAccessFile(mode)
            } catch (e: FileNotFoundException) {
                throw IOException("Can't create TrickRandomAccessFile")
            }

            // Close old FileDescriptor
            try {
                val obj = FIELD_FD!![file]
                if (obj is FileDescriptor) METHOD_CLOSE!!.invoke(null, obj)
            } catch (e: IllegalAccessException) {
                Timber.tag(TAG)
                    .e("Failed to invoke libcore.io.IoUtils.close(FileDescriptor): %s", e)
                file.close()
                throw IOException(e.message)
            } catch (e: InvocationTargetException) {
                Timber.tag(TAG)
                    .e("Failed to invoke libcore.io.IoUtils.close(FileDescriptor): %s", e)
                file.close()
                throw IOException(e.message)
            }

            // Set new FileDescriptor
            try {
                FIELD_FD!![file] = fd
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
                file.close()
                throw IOException(e.message)
            }
            return file
        }
    }
}
