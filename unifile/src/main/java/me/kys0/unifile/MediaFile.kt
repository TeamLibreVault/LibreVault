package me.kys0.unifile

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.text.TextUtils
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

internal class MediaFile(context: Context, override val uri: Uri) : UniFile(null) {

    private val mContext: Context

    init {
        mContext = context.applicationContext
    }

    override fun createFile(displayName: String?): UniFile? = null

    override fun createDirectory(displayName: String?): UniFile? = null

    override val name: String
        get() = MediaContract.getName(mContext, uri)

    override val type: String?
        get() {
            val type = MediaContract.getType(mContext, uri)
            return if (TextUtils.isEmpty(type).not()) type else Utils.getTypeForName(name)
        }

    override val filePath: String
        get() = MediaContract.getFilePath(mContext, uri)

    override val isDirectory: Boolean
        get() = false

    override val isFile: Boolean
        get() {
            val `is`: InputStream = try {
                openInputStream
            } catch (e: IOException) {
                return false
            }
            IOUtils.closeQuietly(`is`)
            return true
        }

    override val lastModified: Long
        get() = MediaContract.lastModified(mContext, uri)

    override val length: Long
        get() = MediaContract.length(mContext, uri)

    override val canRead: Boolean
        get() = isFile

    override val canWrite: Boolean
        get() {
            val os: OutputStream = try {
                openOutputStream(true)
            } catch (e: IOException) {
                return false
            }
            IOUtils.closeQuietly(os)
            return true
        }

    override val delete: Boolean
        get() = false

    override val exists: Boolean
        get() = isFile

    override fun listFiles(): Array<UniFile?>? = null

    override fun listFiles(filter: FilenameFilter?): Array<UniFile?>? = null

    override fun findFile(displayName: String?): UniFile? = null

    override fun renameTo(displayName: String?): Boolean = false

    @get:Throws(IOException::class)
    override val openOutputStream: OutputStream
        get() = TrickOutputStream.create(mContext, uri, "w")

    @Throws(IOException::class)
    override fun openOutputStream(append: Boolean): OutputStream =
        TrickOutputStream.create(mContext, uri, if (append) "wa" else "w")

    @get:Throws(IOException::class)
    override val openInputStream: InputStream
        get() {
            val inputStream: InputStream = try {
                mContext.contentResolver.openInputStream(uri)!!
            } catch (e: Exception) {
                throw IOException("Can't open InputStream")
            }
            return inputStream
        }

    @Throws(IOException::class)
    override fun createRandomAccessFile(mode: String?): UniRandomAccessFile {
        val pfd: ParcelFileDescriptor = try {
            mContext.contentResolver.openFileDescriptor(uri, mode!!)
        } catch (e: Exception) {
            throw IOException("Can't open ParcelFileDescriptor")
        } ?: throw IOException("Can't open ParcelFileDescriptor")
        return RawRandomAccessFile(TrickRandomAccessFile.create(pfd, mode))
    }
}
