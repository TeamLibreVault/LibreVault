package me.kys0.unifile

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.text.TextUtils
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

internal class SingleDocumentFile(
    parent: UniFile?,
    context: Context,
    override val uri: Uri,
) :
    UniFile(parent) {

    private val mContext: Context

    init {
        mContext = context.applicationContext
    }

    override fun createFile(displayName: String?): UniFile? = null

    override fun createDirectory(displayName: String?): UniFile? = null

    override val name: String
        get() = DocumentsContractApi19.getName(mContext, uri)

    override val type: String?
        get() {
            val type = DocumentsContractApi19.getType(mContext, uri)
            return if (TextUtils.isEmpty(type).not()) type else Utils.getTypeForName(name)
        }

    override val filePath: String?
        get() = DocumentsContractApi19.getFilePath(mContext, uri)

    override val isDirectory: Boolean
        get() = DocumentsContractApi19.isDirectory(mContext, uri)

    override val isFile: Boolean
        get() = DocumentsContractApi19.isFile(mContext, uri)

    override val lastModified: Long
        get() = DocumentsContractApi19.lastModified(mContext, uri)

    override val length: Long
        get() = DocumentsContractApi19.length(mContext, uri)

    override val canRead: Boolean
        get() = DocumentsContractApi19.canRead(mContext, uri)

    override val canWrite: Boolean
        get() = DocumentsContractApi19.canWrite(mContext, uri)

    override val delete: Boolean
        get() = DocumentsContractApi19.delete(mContext, uri)

    override val exists: Boolean
        get() = DocumentsContractApi19.exists(mContext, uri)

    override fun listFiles(): Array<UniFile?>? = null

    override fun listFiles(filter: FilenameFilter?): Array<UniFile?>? = null

    override fun findFile(displayName: String?): UniFile? = null

    override fun renameTo(displayName: String?): Boolean = false

    @get:Throws(IOException::class)
    override val openOutputStream: OutputStream
        get() {
            val os: OutputStream = try {
                mContext.contentResolver.openOutputStream(uri)!!
            } catch (e: Exception) {
                throw IOException("Can't open OutputStream")
            }
            return os
        }

    @Throws(IOException::class)
    override fun openOutputStream(append: Boolean): OutputStream {
        val os: OutputStream = try {
            mContext.contentResolver.openOutputStream(uri, if (append) "wa" else "w")!!
        } catch (e: Exception) {
            throw IOException("Can't open OutputStream")
        }
        return os
    }

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
        // Check file
        if (isFile.not()) throw IOException("Can't make sure it is file")
        val pfd: ParcelFileDescriptor = try {
            mode?.let { mContext.contentResolver.openFileDescriptor(uri, it) }
        } catch (e: Exception) {
            throw IOException("Can't open ParcelFileDescriptor")
        } ?: throw IOException("Can't open ParcelFileDescriptor")
        return mode?.let { TrickRandomAccessFile.create(pfd, it) }
            ?.let { RawRandomAccessFile(it) }!!
    }
}
