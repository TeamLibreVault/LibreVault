package me.kys0.unifile

import android.net.Uri
import android.text.TextUtils
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile

/** @noinspection IOStreamConstructor
 */
internal class RawFile(parent: UniFile?, private var mFile: File) : UniFile(parent) {

    override fun createFile(displayName: String?): UniFile? {
        if (TextUtils.isEmpty(displayName)) return null
        val target = displayName?.let { File(mFile, it) }
        if (target != null) return if (target.exists()) {
            if (target.isFile) RawFile(this, target) else null
        } else {
            var os: OutputStream? = null
            try {
                os = FileOutputStream(target)
                RawFile(this, target)
            } catch (e: IOException) {
                Timber.tag(TAG).w("Failed to createFile $displayName: $e")
                null
            } finally {
                IOUtils.closeQuietly(os)
            }
        }
        return null
    }

    override fun createDirectory(displayName: String?): UniFile? {
        if (TextUtils.isEmpty(displayName)) return null
        val target = displayName?.let { File(mFile, it) }
        if (target != null)
            return if (target.isDirectory || target.mkdirs()) RawFile(this, target) else null
        return null
    }

    override val uri: Uri
        get() = Uri.fromFile(mFile)

    override val name: String
        get() = mFile.name

    override val type: String?
        get() = if (mFile.isDirectory) null else Utils.getTypeForName(mFile.name)

    override val filePath: String?
        get() = mFile.path

    override val isDirectory: Boolean
        get() = mFile.isDirectory

    override val isFile: Boolean
        get() = mFile.isFile

    override val lastModified: Long
        get() = mFile.lastModified()

    override val length: Long
        get() = mFile.length()

    override val canRead: Boolean
        get() = mFile.canRead()

    override val canWrite: Boolean
        get() = mFile.canWrite()

    override val delete: Boolean
        get() {
            deleteContents(mFile)
            return mFile.delete()
        }

    override val exists: Boolean
        get() = mFile.exists()

    override fun listFiles(): Array<UniFile?>? {
        val files = mFile.listFiles() ?: return null
        val length = files.size
        val results = arrayOfNulls<UniFile>(length)
        for (i in 0 until length) results[i] = RawFile(this, files[i])
        return results
    }

    override fun listFiles(filter: FilenameFilter?): Array<UniFile?>? {
        if (filter == null) return listFiles()
        val files = mFile.listFiles() ?: return null
        val results = ArrayList<UniFile?>()
        for (file in files) if (filter.accept(this, file.name)) results.add(RawFile(this, file))
        return results.toTypedArray<UniFile?>()
    }

    override fun findFile(displayName: String?): UniFile? {
        if (TextUtils.isEmpty(displayName)) return null
        val child = displayName?.let { File(mFile, it) }
        if (child != null) return if (child.exists()) RawFile(this, child) else null
        return null
    }

    override fun renameTo(displayName: String?): Boolean {
        if (TextUtils.isEmpty(displayName)) return false
        val target = displayName?.let { File(mFile.parentFile, it) }
        return if (target?.let { mFile.renameTo(it) } == true) {
            mFile = target
            true
        } else false
    }

    @get:Throws(IOException::class)
    override val openOutputStream: OutputStream
        get() = FileOutputStream(mFile)

    @Throws(IOException::class)
    override fun openOutputStream(append: Boolean): OutputStream = FileOutputStream(mFile, append)

    @get:Throws(IOException::class)
    override val openInputStream: InputStream
        get() = FileInputStream(mFile)

    @Throws(FileNotFoundException::class)
    override fun createRandomAccessFile(mode: String?): UniRandomAccessFile =
        RawRandomAccessFile(RandomAccessFile(mFile, mode))


    companion object {
        private val TAG = RawFile::class.java.simpleName

        private fun deleteContents(dir: File): Boolean {
            val files = dir.listFiles()
            var success = true
            if (files != null) for (file in files) {
                if (file.isDirectory) success = success and deleteContents(file)
                if (file.delete().not()) {
                    Timber.tag(TAG).w("Failed to delete %s", file)
                    success = false
                }
            }
            return success
        }
    }
}
