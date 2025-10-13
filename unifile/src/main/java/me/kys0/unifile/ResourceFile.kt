package me.kys0.unifile

import android.content.ContentResolver
import android.content.res.AssetFileDescriptor
import android.content.res.Resources
import android.net.Uri
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

internal class ResourceFile(
    private val mR: Resources,
    private val mP: String,
    private val mId: Int,
    private val mName: String,
) : UniFile(null) {

    override fun createFile(displayName: String?): UniFile? = null


    override fun createDirectory(displayName: String?): UniFile? = null


    override val uri: Uri
        get() = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(mP)
            .path(mId.toString())
            .build()

    override val name: String
        get() = mName

    override val type: String
        get() = // Can't get type, just return application/octet-stream
            "application/octet-stream"

    override val filePath: String?
        get() = null

    override val isDirectory: Boolean
        get() = false

    override val isFile: Boolean
        get() = true

    override val lastModified: Long
        get() = -1

    override val length: Long
        get() = -1

    override val canRead: Boolean
        get() = true

    override val canWrite: Boolean
        get() = false

    override val delete: Boolean
        get() = false

    override val exists: Boolean
        get() = true

    override fun listFiles(): Array<UniFile?>? = null

    override fun listFiles(filter: FilenameFilter?): Array<UniFile?>? = null


    override fun findFile(displayName: String?): UniFile? = null


    override fun renameTo(displayName: String?): Boolean = false


    @get:Throws(IOException::class)
    override val openOutputStream: OutputStream
        get() = throw IOException("Can't open OutputStream from resource file.")


    @Throws(IOException::class)
    override fun openOutputStream(append: Boolean): OutputStream =
        throw IOException("Can't open OutputStream from resource file.")


    @get:Throws(IOException::class)
    override val openInputStream: InputStream
        get() = try {
            mR.openRawResource(mId)
        } catch (e: Resources.NotFoundException) {
            throw IOException("Can't open InputStream")
        }

    @Throws(IOException::class)
    override fun createRandomAccessFile(mode: String?): UniRandomAccessFile {
        if ("r" != mode) {
            throw IOException("Unsupported mode: $mode")
        }
        val afd: AssetFileDescriptor = try {
            mR.openRawResourceFd(mId)
        } catch (e: Resources.NotFoundException) {
            throw IOException("Can't open AssetFileDescriptor")
        } ?: throw IOException("Can't open AssetFileDescriptor")
        return RawRandomAccessFile(TrickRandomAccessFile.create(afd, mode))
    }
}
