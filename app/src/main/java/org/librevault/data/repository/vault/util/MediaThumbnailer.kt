package org.librevault.data.repository.vault.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File

private const val TAG = "MediaThumbnailer"

class MediaThumbnailer(
    private val format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    private val initialQuality: Int = 50,
) {

    fun compress(file: File): ByteArray? {
        return try {
            val bitmap = if (isVideo(file)) {
                extractVideoThumbnail(file)
            } else if (isImage(file)) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                Log.e(TAG, "Unsupported file type: ${file.extension}")
                null
            }

            if (bitmap == null)
                return null

            val resized = bitmap.cropSquare()

            @Suppress("SpellCheckingInspection")
            val baos = ByteArrayOutputStream()
            resized.compress(format, initialQuality, baos)
            baos.toByteArray()
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing file: ${e.message}")
            null
        }
    }

    private fun extractVideoThumbnail(file: File): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            retriever.frameAtTime
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting video thumbnail: ${e.message}")
            null
        } finally {
            retriever.release()
        }
    }

    private fun isImage(file: File): Boolean {
        val lower = file.name.lowercase()
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                lower.endsWith(".png") || lower.endsWith(".webp") ||
                lower.endsWith(".bmp") || lower.endsWith(".gif")
    }

    private fun isVideo(file: File): Boolean {
        val lower = file.name.lowercase()
        return lower.endsWith(".mp4") || lower.endsWith(".avi") ||
                lower.endsWith(".mkv") || lower.endsWith(".webm") ||
                lower.endsWith(".3gp") || lower.endsWith(".mov")
    }

    private fun Bitmap.cropSquare(): Bitmap {
        val bitmap = this

        val width = bitmap.width
        val height = bitmap.height

        val size = minOf(width, height)

        val x = (width - size) / 2
        val y = (height - size) / 2

        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }
}