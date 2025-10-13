package me.kys0.unifile

import android.content.ContentResolver
import android.content.res.AssetManager
import android.net.Uri
import android.text.TextUtils
import me.kys0.unifile.IOUtils.closeQuietly
import me.kys0.unifile.TrickRandomAccessFile.Companion.create
import me.kys0.unifile.Utils.getTypeForName
import me.kys0.unifile.Utils.resolve
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

internal class AssetFile(
    parent: UniFile?,
    private val mAssetManager: AssetManager,
    private val mPath: String,
) : UniFile(parent) {

    override fun createFile(displayName: String?): UniFile? {
        val file = findFile(displayName)
        return if (file != null && file.isFile) file else null
    }

    override fun createDirectory(displayName: String?): UniFile? {
        val file = findFile(displayName)
        return if (file != null && file.isDirectory) file else null
    }

    override val uri: Uri
        get() = Uri.Builder()
            .scheme(ContentResolver.SCHEME_FILE)
            .authority("")
            .path("android_asset/$mPath")
            .build()

    override val name: String
        get() {
            val index = mPath.lastIndexOf('/')
            return if (index >= 0 && index < mPath.length - 1) mPath.substring(index + 1) else mPath
        }

    override val type: String?
        get() = (if (isDirectory) null else getTypeForName(name))

    override val filePath: String?
        get() = null // Not supported

    override val isDirectory: Boolean
        get() = try {
            val files = mAssetManager.list(mPath)
            files.isNullOrEmpty().not()
        } catch (e: IOException) {
            false
        }

    override val isFile: Boolean
        get() {
            val inputStream: InputStream = try {
                openInputStream
            } catch (e: IOException) {
                return false
            }
            closeQuietly(inputStream)
            return true
        }

    override val lastModified: Long
        get() = -1 // Not supported

    override val length: Long
        get() = -1 // Not supported

    override val canRead: Boolean
        get() = isFile

    override val canWrite: Boolean
        get() = false

    override val delete: Boolean
        get() = false // Not supported

    override val exists: Boolean
        get() = isDirectory || isFile

    override fun listFiles(): Array<UniFile?>? {
        return try {
            val files = mAssetManager.list(mPath)
            if (files.isNullOrEmpty()) return null
            val length = files.size
            val results = arrayOfNulls<UniFile>(length)
            for (i in 0 until length)
                results[i] =
                    AssetFile(
                        parent = this,
                        mAssetManager = mAssetManager,
                        mPath = resolve(mPath, files[i])
                    )
            results
        } catch (e: IOException) {
            null
        }
    }

    override fun listFiles(filter: FilenameFilter?): Array<UniFile?>? {
        return if (filter == null) listFiles() else try {
            val files = mAssetManager.list(mPath)
            if (files.isNullOrEmpty()) return null
            val results = ArrayList<UniFile?>()
            for (name in files) if (filter.accept(this, name))
                results.add(AssetFile(this, mAssetManager, resolve(mPath, name!!)))
            results.toTypedArray<UniFile?>()
        } catch (e: IOException) {
            null
        }
    }

    override fun findFile(displayName: String?): UniFile? {
        return if (TextUtils.isEmpty(displayName)) null else try {
            val files = mAssetManager.list(mPath) ?: return null
            for (f in files) if (displayName == f) return displayName?.let { resolve(mPath, it) }
                ?.let { AssetFile(this, mAssetManager, it) }
            null
        } catch (e: IOException) {
            null
        }
    }

    override fun renameTo(displayName: String?): Boolean = false // Not supported

    @get:Throws(IOException::class)
    override val openOutputStream: OutputStream
        get() = throw IOException("Not support OutputStream for asset file.")

    @Throws(IOException::class)
    override fun openOutputStream(append: Boolean): OutputStream =
        throw IOException("Not support OutputStream for asset file.")

    @get:Throws(IOException::class)
    override val openInputStream: InputStream
        get() = mAssetManager.open(mPath)

    @Throws(IOException::class)
    override fun createRandomAccessFile(mode: String?): UniRandomAccessFile {
        if ("r" != mode) throw IOException("Unsupported mode: $mode")
        val afd = mAssetManager.openFd(mPath)
        return RawRandomAccessFile(create(afd, mode))
    }
}
