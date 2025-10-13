package me.kys0.unifile

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import org.jetbrains.annotations.Contract
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * In Android files can be accessed via [File] and [Uri].
 * The UniFile is designed to emulate File interface for both File and Uri.
 */
@Suppress("unused", "KDocUnresolvedReference")
abstract class UniFile internal constructor(private val mParent: UniFile?) {
    /**
     * Create a new file as a direct child of this directory.
     *
     * @param displayName name of new file
     * @return file representing newly created document, or null if failed
     * @see android.provider.DocumentsContract.createDocument
     */
    abstract fun createFile(displayName: String?): UniFile?

    /**
     * Create a new directory as a direct child of this directory.
     *
     * @param displayName name of new directory
     * @return file representing newly created directory, or null if failed
     * @see android.provider.DocumentsContract.createDocument
     */
    abstract fun createDirectory(displayName: String?): UniFile?

    /**
     * Return a Uri for the underlying document represented by this file. This
     * can be used with other platform APIs to manipulate or share the
     * underlying content. You can use [.isTreeDocumentUri] to
     * test if the returned Uri is backed by a
     * [android.provider.DocumentsProvider].
     *
     * @return uri of the file
     * @see Intent.setData
     * @see Intent.setClipData
     * @see ContentResolver.openInputStream
     * @see ContentResolver.openOutputStream
     * @see ContentResolver.openFileDescriptor
     */
    abstract val uri: Uri

    /**
     * Return the display name of this file.
     *
     * @return name of the file, or null if failed
     * @see android.provider.DocumentsContract.Document.COLUMN_DISPLAY_NAME
     */
    abstract val name: String?

    /**
     * Return the MIME type of this file.
     *
     * @return MIME type of the file, or null if failed
     * @see android.provider.DocumentsContract.Document.COLUMN_MIME_TYPE
     */
    abstract val type: String?

    /**
     * Return the file path of this file.
     * Like `/xxx/yyy/zzz`.
     *
     * @return file path of the file, or null if can't or failed
     */
    abstract val filePath: String?
    val parentFile: UniFile?
        /**
         * Return the parent file of this file. Only defined inside of the
         * user-selected tree; you can never escape above the top of the tree.
         *
         *
         * The underlying [android.provider.DocumentsProvider] only defines a
         * forward mapping from parent to child, so the reverse mapping of child to
         * parent offered here is purely a convenience method, and it may be
         * incorrect if the underlying tree structure changes.
         *
         * @return parent of the file, or null if it is the top of the file tree
         */
        get() = mParent

    /**
     * Indicates if this file represents a *directory*.
     *
     * @return `true` if this file is a directory, `false`
     * otherwise.
     * @see android.provider.DocumentsContract.Document.MIME_TYPE_DIR
     */
    abstract val isDirectory: Boolean

    /**
     * Indicates if this file represents a *file*.
     *
     * @return `true` if this file is a file, `false` otherwise.
     * @see android.provider.DocumentsContract.Document.COLUMN_MIME_TYPE
     */
    abstract val isFile: Boolean

    /**
     * Returns the time when this file was last modified, measured in
     * milliseconds since January 1st, 1970, midnight. Returns -1 if the file
     * does not exist, or if the modified time is unknown.
     *
     * @return the time when this file was last modified, `-1L` if can't get it
     * @see android.provider.DocumentsContract.Document.COLUMN_LAST_MODIFIED
     */
    abstract val lastModified: Long

    /**
     * Returns the length of this file in bytes. Returns -1 if the file does not
     * exist, or if the length is unknown. The result for a directory is not
     * defined.
     *
     * @return the number of bytes in this file, `-1L` if can't get it
     * @see android.provider.DocumentsContract.Document.COLUMN_SIZE
     */
    abstract val length: Long

    /**
     * Indicates whether the current context is allowed to read from this file.
     *
     * @return `true` if this file can be read, `false` otherwise.
     */
    abstract val canRead: Boolean

    /**
     * Indicates whether the current context is allowed to write to this file.
     *
     * @return `true` if this file can be written, `false`
     * otherwise.
     * @see android.provider.DocumentsContract.Document.COLUMN_FLAGS
     *
     * @see android.provider.DocumentsContract.Document.FLAG_SUPPORTS_DELETE
     *
     * @see android.provider.DocumentsContract.Document.FLAG_SUPPORTS_WRITE
     *
     * @see android.provider.DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE
     */
    abstract val canWrite: Boolean

    /**
     * Deletes this file.
     *
     *
     * Note that this method does *not* throw `IOException` on
     * failure. Callers must check the return value.
     *
     * @return `true` if this file was deleted, `false` otherwise.
     * @see android.provider.DocumentsContract.deleteDocument
     */
    abstract val delete: Boolean

    /**
     * Returns a boolean indicating whether this file can be found.
     *
     * @return `true` if this file exists, `false` otherwise.
     */
    abstract val exists: Boolean

    /**
     * Returns an array of files contained in the directory represented by this
     * file.
     *
     * @return an array of files or `null`.
     * @see android.provider.DocumentsContract.buildChildDocumentsUriUsingTree
     */
    abstract fun listFiles(): Array<UniFile?>?

    /**
     * Gets a list of the files in the directory represented by this file. This
     * list is then filtered through a FilenameFilter and the names of files
     * with matching names are returned as an array of strings.
     *
     * @param filter the filter to match names against, may be `null`.
     * @return an array of files or `null`.
     */
    abstract fun listFiles(filter: FilenameFilter?): Array<UniFile?>?

    /**
     * Test there is a file with the display name in the directory.
     *
     * @return the file if found it, or `null`.
     */
    abstract fun findFile(displayName: String?): UniFile?

    /**
     * Renames this file to `displayName`.
     *
     *
     * Note that this method does *not* throw `IOException` on
     * failure. Callers must check the return value.
     *
     *
     * Some providers may need to create a new file to reflect the rename,
     * potentially with a different MIME type, so [.getUri] and
     * [.getType] may change to reflect the rename.
     *
     *
     * When renaming a directory, children previously enumerated through
     * [.listFiles] may no longer be valid.
     *
     * @param displayName the new display name.
     * @return true on success.
     * @see android.provider.DocumentsContract.renameDocument
     */
    abstract fun renameTo(displayName: String?): Boolean

    /**
     * Open a stream on to the content associated with the file, clean it if it exists
     *
     * @return the [OutputStream]
     * @throws IOException If an error occurs during processing.
     */
    @get:Throws(IOException::class)
    abstract val openOutputStream: OutputStream

    /**
     * Open a stream on to the content associated with the file
     *
     * @param append `true` for do not clean it if it exists
     * @return the [OutputStream]
     * @throws IOException If an error occurs during processing.
     */
    @Throws(IOException::class)
    abstract fun openOutputStream(append: Boolean): OutputStream

    /**
     * Open a stream on to the content associated with the file
     *
     * @return the [InputStream]
     * @throws IOException If an error occurs during processing.
     */
    @get:Throws(IOException::class)
    abstract val openInputStream: InputStream

    /**
     * Get a random access stuff of the UniFile
     *
     * @param mode "r" or "rw"
     * @return the random access stuff
     * @throws IOException If an error occurs during processing.
     */
    @Throws(IOException::class)
    abstract fun createRandomAccessFile(mode: String?): UniRandomAccessFile

    companion object {
        private var sUriHandlerArray: MutableList<UriHandler>? = null

        /**
         * Add a UriHandler to get UniFile from uri
         */
        fun addUriHandler(handler: UriHandler) {
            if (sUriHandlerArray == null) sUriHandlerArray = ArrayList()
            sUriHandlerArray?.add(handler)
        }

        /**
         * Remove the UriHandler added before
         */
        fun removeUriHandler(handler: UriHandler) = sUriHandlerArray?.remove(handler)


        /**
         * Create a [UniFile] representing the given [File].
         *
         * @param file the file to wrap
         * @return the [UniFile] representing the given [File].
         */
        @JvmStatic
        fun fromFile(file: File?): UniFile? = if (file != null) RawFile(null, file) else null

        /**
         * Create a [UniFile] representing the given asset File.
         */
        fun fromAsset(assetManager: AssetManager, filename: String): UniFile {
            val uri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_FILE)
                .authority("")
                .path("android_asset/$filename")
                .build()
            return fromAssetUri(assetManager, uri)
        }

        /**
         * Create a [UniFile] representing the given resource id.
         */
        fun fromResource(context: Context, id: Int): UniFile? {
            val r = context.resources
            val p = context.packageName
            val name: String = try {
                r.getResourceEntryName(id)
            } catch (e: Resources.NotFoundException) {
                return null
            }
            return ResourceFile(r, p, id, name)
        }

        private const val ASSET_PATH_PREFIX_LENGTH = "/android_asset/".length

        // Create AssetFile from asset file uri
        private fun fromAssetUri(assetManager: AssetManager, assetUri: Uri): UniFile {
            val originPath = assetUri.path?.substring(
                ASSET_PATH_PREFIX_LENGTH
            )
            val path = originPath?.let { Utils.normalize(it) }
            return path?.let { AssetFile(null, assetManager, it) }!!
        }

        // Create SingleDocumentFile from single document file uri
        @SuppressLint("ObsoleteSdkInt")
        private fun fromSingleDocumentUri(context: Context, singleUri: Uri): UniFile? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) SingleDocumentFile(
                parent = null,
                context = context,
                uri = singleUri
            ) else null

        // Create TreeDocumentFile from tree document file uri
        @SuppressLint("ObsoleteSdkInt")
        private fun fromTreeDocumentUri(context: Context, treeUri: Uri): UniFile? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) TreeDocumentFile(
                parent = null, context = context,
                uri = DocumentsContractApi21.prepareTreeUri(treeUri)
            ) else null
        }

        // Create MediaFile from tree media file uri
        @Contract("_, _ -> new")
        private fun fromMediaUri(context: Context, mediaUri: Uri): UniFile =
            MediaFile(context, mediaUri)

        /**
         * Create a [UniFile] representing the given [Uri].
         */
        fun fromUri(context: Context?, uri: Uri?): UniFile? {
            if (context == null || uri == null) return null

            // Custom handler
            if (sUriHandlerArray != null) {
                var i = 0
                val size = sUriHandlerArray!!.size
                while (i < size) {
                    val file = sUriHandlerArray?.get(i)?.fromUri(context, uri)
                    if (file != null) return file
                    i++
                }
            }
            return if (isFileUri(uri)) {
                if (isAssetUri(uri)) fromAssetUri(context.assets, uri) else fromFile(
                    file = uri.path?.let(
                        ::File
                    )
                )
            } else if (isDocumentUri(context, uri)) {
                if (isTreeDocumentUri(context, uri)) fromTreeDocumentUri(
                    context,
                    uri
                ) else fromSingleDocumentUri(context, uri)
            } else if (isMediaUri(uri)) MediaFile(context, uri) else {
                val result = ResourcesContract.openResource(context, uri)
                if (result != null) result.r?.let {
                    result.p?.let { it1 ->
                        result.name?.let { it2 ->
                            ResourceFile(
                                mR = it,
                                mP = it1,
                                mId = result.id,
                                mName = it2
                            )
                        }
                    }
                } else null
            }
        }

        /**
         * Test if given Uri is FileUri
         */
        @JvmStatic
        fun isFileUri(uri: Uri?): Boolean = uri != null && ContentResolver.SCHEME_FILE == uri.scheme

        /**
         * Test if given Uri is backed by a
         * [android.provider.DocumentsProvider].
         */
        @SuppressLint("ObsoleteSdkInt")
        fun isDocumentUri(context: Context?, uri: Uri?): Boolean =
            uri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                    context?.let { DocumentsContractApi19.isDocumentUri(it, uri) } == true


        /**
         * Test if given Uri is TreeDocumentUri
         */
        @SuppressLint("ObsoleteSdkInt")
        fun isTreeDocumentUri(context: Context?, uri: Uri?): Boolean =
            uri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                    DocumentsContractApi21.isTreeDocumentUri(context, uri)


        /**
         * Test if given Uri is AssetUri.
         * Like `file:///android_asset/pathsegment1/pathsegment2`
         */
        @JvmStatic
        fun isAssetUri(uri: Uri?): Boolean {
            if (uri == null) return false
            val paths = uri.pathSegments
            return ContentResolver.SCHEME_FILE == uri.scheme && paths.size >= 2 && "android_asset" == paths[0]
        }

        /**
         * Test if given Uri is MediaUri
         */
        @JvmStatic
        fun isMediaUri(uri: Uri?): Boolean =
            uri != null && ContentResolver.SCHEME_CONTENT == uri.scheme
    }
}
