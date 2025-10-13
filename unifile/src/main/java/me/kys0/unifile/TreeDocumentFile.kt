package me.kys0.unifile

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.text.TextUtils
import android.webkit.MimeTypeMap
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

internal class TreeDocumentFile(parent: UniFile?, context: Context, override var uri: Uri) :
    UniFile(parent) {
    private val mContext: Context

    init {
        mContext = context.applicationContext
    }

    override fun createFile(displayName: String?): UniFile? {
        if (TextUtils.isEmpty(displayName)) return null
        val child = findFile(displayName)
        return if (child != null) {
            if (child.isFile) child else {
                Timber.tag(TAG)
                    .w("Try to create file $displayName, but it is not file")
                null
            }
        } else {
            // FIXME There's nothing about display name and extension mentioned in document.
            // But it works for com.android.externalstorage.documents.
            // The safest way is use application/octet-stream all the time,
            // But media store will not be updated.
            val index = displayName!!.lastIndexOf('.')
            if (index > 0) {
                val name = displayName.substring(0, index)
                val extension = displayName.substring(index + 1)
                val mimeType =
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                if (!TextUtils.isEmpty(mimeType)) {
                    val result = DocumentsContractApi21.createFile(
                        context = mContext,
                        self = uri,
                        mimeType = mimeType,
                        displayName = name
                    )
                    return if (result != null) TreeDocumentFile(
                        parent = this,
                        context = mContext,
                        uri = result
                    ) else null
                }
            }

            // Not dot in displayName or dot is the first char or can't get MimeType
            val result = DocumentsContractApi21.createFile(
                context = mContext,
                self = uri,
                mimeType = "application/octet-stream",
                displayName = displayName
            )
            if (result != null) TreeDocumentFile(
                parent = this,
                context = mContext,
                uri = result
            ) else null
        }
    }

    override fun createDirectory(displayName: String?): UniFile? {
        if (TextUtils.isEmpty(displayName)) return null
        val child = findFile(displayName)
        return if (child != null) {
            if (child.isDirectory) child else null
        } else {
            val result = DocumentsContractApi21.createDirectory(mContext, uri, displayName)
            if (result != null) TreeDocumentFile(
                parent = this,
                context = mContext,
                uri = result
            ) else null
        }
    }

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
        get() = (if (isDirectory) -1L else DocumentsContractApi19.length(mContext, uri))

    override val canRead: Boolean
        get() = DocumentsContractApi19.canRead(mContext, uri)

    override val canWrite: Boolean
        get() = DocumentsContractApi19.canWrite(mContext, uri)

    override val delete: Boolean
        get() = DocumentsContractApi19.delete(mContext, uri)

    override val exists: Boolean
        get() = DocumentsContractApi19.exists(mContext, uri)

    override fun listFiles(): Array<UniFile?>? {
        if (isDirectory.not()) return null
        val result = DocumentsContractApi21.listFiles(mContext, uri)
        val resultFiles = arrayOfNulls<UniFile>(result.size)
        var i = 0
        val n = result.size
        while (i < n) {
            val uri = result[i]
            resultFiles[i] = TreeDocumentFile(this, mContext, uri)
            i++
        }
        return resultFiles
    }

    override fun listFiles(filter: FilenameFilter?): Array<UniFile?>? {
        if (filter == null) return listFiles()
        if (isDirectory.not()) return null
        val uris = DocumentsContractApi21.listFiles(mContext, uri)
        val results = ArrayList<UniFile?>()
        for (uri in uris) {
            val name = DocumentsContractApi19.getName(mContext, uri)
            if (name.isNotBlank() && filter.accept(this, name))
                results.add(TreeDocumentFile(parent = this, context = mContext, uri = uri))
        }
        return results.toTypedArray<UniFile?>()
    }

    override fun findFile(displayName: String?): UniFile? {
        if (TextUtils.isEmpty(displayName)) return null
        if (isDirectory.not()) return null
        val result = DocumentsContractApi21.listFiles(mContext, uri)
        for (uri in result) {
            val name = DocumentsContractApi19.getName(mContext, uri)
            if (displayName == name) return TreeDocumentFile(
                parent = this,
                context = mContext,
                uri = uri
            )
        }
        return null
    }

    override fun renameTo(displayName: String?): Boolean {
        val result = DocumentsContractApi21.renameTo(mContext, uri, displayName)
        return if (result != null) {
            uri = result
            true
        } else false
    }

    @get:Throws(IOException::class)
    override val openOutputStream: OutputStream
        get() {
            if (isDirectory) throw IOException("Can't open OutputStream from a directory")
            val os: OutputStream = try {
                mContext.contentResolver.openOutputStream(uri)!!
            } catch (e: Exception) {
                throw IOException("Can't open OutputStream")
            }
            return os
        }

    @Throws(IOException::class)
    override fun openOutputStream(append: Boolean): OutputStream {
        if (isDirectory) throw IOException("Can't open OutputStream from a directory")
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
            if (isDirectory) throw IOException("Can't open InputStream from a directory")
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
            mContext.contentResolver.openFileDescriptor(uri, mode!!)
        } catch (e: Exception) {
            throw IOException("Can't open ParcelFileDescriptor")
        } ?: throw IOException("Can't open ParcelFileDescriptor")
        return RawRandomAccessFile(TrickRandomAccessFile.create(pfd, mode))
    }

    companion object {
        private val TAG = TreeDocumentFile::class.java.simpleName
    }
}
