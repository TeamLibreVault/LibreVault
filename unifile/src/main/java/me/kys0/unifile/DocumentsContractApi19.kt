package me.kys0.unifile

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import timber.log.Timber

@SuppressLint("ObsoleteSdkInt")
@TargetApi(Build.VERSION_CODES.KITKAT)
internal object DocumentsContractApi19 {

    private val TAG = DocumentsContractApi19::class.java.simpleName

    private const val AUTHORITY_DOCUMENT_EXTERNAL_STORAGE = "com.android.externalstorage.documents"
    private const val AUTHORITY_DOCUMENT_DOWNLOAD = "com.android.providers.downloads.documents"
    private const val AUTHORITY_DOCUMENT_MEDIA = "com.android.providers.media.documents"
    private const val PROVIDER_INTERFACE = "android.content.action.DOCUMENTS_PROVIDER"
    private const val PATH_DOCUMENT = "document"
    private const val PATH_TREE = "tree"

    fun isContentUri(uri: Uri?): Boolean =
        uri != null && ContentResolver.SCHEME_CONTENT == uri.scheme


    fun isDocumentsProvider(context: Context, authority: String?): Boolean {
        val intent = Intent(PROVIDER_INTERFACE)
        val infos = context.packageManager
            .queryIntentContentProviders(intent, 0)
        for (info in infos) if (authority == info.providerInfo.authority) return true
        return false
    }

    // It is different from DocumentsContract.isDocumentUri().
    // It accepts uri like content://com.android.externalstorage.documents/tree/primary%3AHaHa as well.
    fun isDocumentUri(context: Context, self: Uri): Boolean {
        if (isContentUri(self) && isDocumentsProvider(context, self.authority)) {
            val paths = self.pathSegments
            if (paths.size == 2) return PATH_DOCUMENT == paths[0] || PATH_TREE == paths[0]
            else if (paths.size == 4) return PATH_TREE == paths[0] && PATH_DOCUMENT == paths[2]
        }
        return false
    }

    fun getName(context: Context?, self: Uri?): String = Contracts.queryForString(
        context!!,
        self,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        ""
    )

    private fun getRawType(context: Context, self: Uri): String = Contracts.queryForString(
        context,
        self,
        DocumentsContract.Document.COLUMN_MIME_TYPE,
        ""
    )

    fun getType(context: Context, self: Uri): String? {
        val rawType = getRawType(context, self)
        return if (DocumentsContract.Document.MIME_TYPE_DIR == rawType) null else rawType
    }

    fun getFilePath(context: Context, self: Uri?): String? {
        return if (self == null) null else try {
            val authority = self.authority
            if (AUTHORITY_DOCUMENT_EXTERNAL_STORAGE == authority) {
                // Get type and path
                val docId = DocumentsContract.getDocumentId(self)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                val path = split[1]

                if ("primary".equals(type, ignoreCase = true))
                    Environment.getExternalStorageDirectory().toString() + "/" + path else {
                    // Get the storage path
                    val cacheDirs = context.externalCacheDirs
                    var storageDir: String? = null
                    for (cacheDir in cacheDirs) {
                        val cachePath = cacheDir.path
                        val index = cachePath.indexOf(type)
                        if (index >= 0) {
                            storageDir = cachePath.substring(0, index + type.length)
                        }
                    }
                    if (storageDir != null) "$storageDir/$path" else null
                }
            } else if (AUTHORITY_DOCUMENT_DOWNLOAD == authority) {
                val id = DocumentsContract.getDocumentId(self)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), id.toLong()
                )
                Contracts.queryForString(context, contentUri, MediaStore.MediaColumns.DATA, "")
            } else if (AUTHORITY_DOCUMENT_MEDIA == authority) {
                // Get type and id
                val docId = DocumentsContract.getDocumentId(self)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                val id = split[1]
                val baseUri: Uri = when (type) {
                    "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

                    else -> {
                        Timber.tag(TAG)
                            .d("%s%s", "Unknown type in $AUTHORITY_DOCUMENT_MEDIA: ", type)
                        return null
                    }
                }
                val contentUri = ContentUris.withAppendedId(baseUri, id.toLong())

                // Requires android.permission.READ_EXTERNAL_STORAGE or return null
                Contracts.queryForString(context, contentUri, MediaStore.MediaColumns.DATA, "")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun isDirectory(context: Context, self: Uri): Boolean =
        DocumentsContract.Document.MIME_TYPE_DIR == getRawType(context, self)


    fun isFile(context: Context, self: Uri): Boolean {
        val type = getRawType(context, self)
        return !(DocumentsContract.Document.MIME_TYPE_DIR == type || TextUtils.isEmpty(type))
    }

    fun lastModified(context: Context?, self: Uri?): Long = Contracts.queryForLong(
        context!!,
        self,
        DocumentsContract.Document.COLUMN_LAST_MODIFIED,
        -1L
    )


    fun length(context: Context?, self: Uri?): Long =
        Contracts.queryForLong(context!!, self, DocumentsContract.Document.COLUMN_SIZE, -1L)


    fun canRead(context: Context, self: Uri): Boolean =
        if (context.checkCallingOrSelfUriPermission( // Ignore if grant doesn't allow read
                self,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            false
        } else !TextUtils.isEmpty(
            getRawType(
                context,
                self
            )
        ) // Ignore documents without MIME


    fun canWrite(context: Context, self: Uri): Boolean {
        // Ignore if grant doesn't allow write
        if (context.checkCallingOrSelfUriPermission(self, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            != PackageManager.PERMISSION_GRANTED
        ) return false

        val type = getRawType(context, self)
        val flags = Contracts.queryForInt(context, self, DocumentsContract.Document.COLUMN_FLAGS, 0)

        // Ignore documents without MIME
        if (TextUtils.isEmpty(type)) return false

        // Deletable documents considered writable
        if (flags and DocumentsContract.Document.FLAG_SUPPORTS_DELETE != 0) return true

        // Writable normal files considered writable
        return if (DocumentsContract.Document.MIME_TYPE_DIR == type && flags and DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE != 0) // Directories that allow create considered writable
            true else (!TextUtils.isEmpty(type)
                && flags and DocumentsContract.Document.FLAG_SUPPORTS_WRITE != 0)
    }

    fun delete(context: Context, self: Uri?): Boolean = try {
        DocumentsContract.deleteDocument(context.contentResolver, self!!)
    } catch (e: Exception) {
        // Maybe user ejects tf card
        Timber.tag(TAG).e(e, "Failed to renameTo")
        false
    }


    @SuppressLint("BinaryOperationInTimber")
    fun exists(context: Context, self: Uri?): Boolean {
        val resolver = context.contentResolver
        var c: Cursor? = null
        return try {
            c = resolver.query(
                self!!, arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID
                ), null, null, null
            )
            null != c && c.count > 0
        } catch (e: Exception) {
            Timber.tag(TAG).w("Failed query: $e")
            false
        } finally {
            Contracts.closeQuietly(c)
        }
    }
}
