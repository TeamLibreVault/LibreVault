package me.kys0.unifile

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import timber.log.Timber

@SuppressLint("ObsoleteSdkInt")
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
internal object DocumentsContractApi21 {

    private val TAG = DocumentsContractApi21::class.java.simpleName

    private const val PATH_DOCUMENT = "document"
    private const val PATH_TREE = "tree"

    fun isTreeDocumentUri(context: Context?, self: Uri): Boolean {
        if (DocumentsContractApi19.isContentUri(self) &&
            DocumentsContractApi19.isDocumentsProvider(context!!, self.authority)
        ) {
            val paths = self.pathSegments
            if (paths.size == 2) return PATH_TREE == paths[0] else if (paths.size == 4) return PATH_TREE == paths[0] && PATH_DOCUMENT == paths[2]
        }
        return false
    }

    fun createFile(
        context: Context, self: Uri?, mimeType: String?,
        displayName: String?,
    ): Uri? {
        return try {
            DocumentsContract.createDocument(
                context.contentResolver, self!!, mimeType!!,
                displayName!!
            )
        } catch (e: Exception) {
            // Maybe user ejects tf card
            Timber.tag(TAG).e(e, "Failed to createFile")
            null
        }
    }

    fun createDirectory(context: Context, self: Uri?, displayName: String?): Uri? =
        createFile(context, self, DocumentsContract.Document.MIME_TYPE_DIR, displayName)


    fun prepareTreeUri(treeUri: Uri?): Uri {
        var documentId: String?
        try {
            documentId = DocumentsContract.getDocumentId(treeUri)
            requireNotNull(documentId)
        } catch (e: Exception) {
            // IllegalArgumentException will be raised
            // if DocumentsContract.getDocumentId() failed.
            // But it isn't mentioned the document,
            // catch all kinds of Exception for safety.
            documentId = DocumentsContract.getTreeDocumentId(treeUri)
        }
        return DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
    }

    fun listFiles(context: Context, self: Uri?): Array<Uri> {
        val resolver = context.contentResolver
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            self,
            DocumentsContract.getDocumentId(self)
        )
        val results = ArrayList<Uri>()
        var c: Cursor? = null
        try {
            c = resolver.query(
                childrenUri, arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID
                ), null, null, null
            )
            if (null != c) while (c.moveToNext()) {
                val documentId = c.getString(0)
                val documentUri = DocumentsContract.buildDocumentUriUsingTree(
                    self,
                    documentId
                )
                results.add(documentUri)
            }
        } catch (e: Exception) {
            // Log.w(TAG, "Failed query: " + e);
        } finally {
            closeQuietly(c)
        }
        return results.toTypedArray<Uri>()
    }

    fun renameTo(context: Context, self: Uri?, displayName: String?): Uri? {
        return try {
            DocumentsContract.renameDocument(context.contentResolver, self!!, displayName!!)
        } catch (e: Exception) {
            // Maybe user ejects tf card
            Timber.tag(TAG).e(e, "Failed to renameTo")
            null
        }
    }

    private fun closeQuietly(closeable: AutoCloseable?) {
        if (closeable != null) try {
            closeable.close()
        } catch (rethrown: RuntimeException) {
            throw rethrown
        } catch (_: Exception) {
        }
    }
}
