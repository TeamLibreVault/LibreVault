package me.kys0.unifile

import android.content.Context
import android.database.Cursor
import android.net.Uri

internal object Contracts {

    fun queryForString(
        context: Context, self: Uri?, column: String,
        defaultValue: String,
    ): String {
        val resolver = context.contentResolver
        var c: Cursor? = null
        return try {
            c = resolver.query(self!!, arrayOf(column), null, null, null)
            if (c != null && c.moveToFirst() && c.isNull(0).not()) c.getString(0) else defaultValue
        } catch (e: Exception) {
            defaultValue
        } finally {
            closeQuietly(c)
        }
    }

    fun queryForInt(
        context: Context, self: Uri?, column: String?,
        defaultValue: Int,
    ): Int = queryForLong(context, self, column, defaultValue.toLong()).toInt()


    fun queryForLong(
        context: Context, self: Uri?, column: String?,
        defaultValue: Long,
    ): Long {
        val resolver = context.contentResolver
        var c: Cursor? = null
        return try {
            c = resolver.query(self!!, arrayOf(column), null, null, null)
            if (c != null && c.moveToFirst() && c.isNull(0).not()) c.getLong(0) else defaultValue
        } catch (e: Exception) {
            defaultValue
        } finally {
            closeQuietly(c)
        }
    }

    fun closeQuietly(closeable: Cursor?) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (rethrown: RuntimeException) {
                throw rethrown
            } catch (ignored: Exception) {
            }
        }
    }
}
