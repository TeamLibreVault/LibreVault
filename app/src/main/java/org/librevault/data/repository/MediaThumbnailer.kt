package org.librevault.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File

private const val TAG = "MediaThumbnailer"

class MediaThumbnailer(
    private val targetWidth: Int = 400,
    private val targetHeight: Int = 400,
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

            val resized = Bitmap.createScaledBitmap(
                bitmap,
                targetWidth,
                targetHeight,
                true
            )

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
}