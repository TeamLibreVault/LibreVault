package org.librevault.data.repository.vault.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File

private const val TAG = "MediaThumbnailer"

class MediaThumbnailer(
    private val format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    private val initialQuality: Int = 60,
) {

    fun compress(file: File): ByteArray? = try {
        val bitmap = when {
            isVideo(file) -> extractVideoThumbnail(file)
            isImage(file) -> BitmapFactory.decodeFile(file.absolutePath)
            else -> {
                Log.e(TAG, "Unsupported file type: ${file.extension}")
                return null
            }
        } ?: return null

        val resized = bitmap.cropSquare(file.absolutePath)

        val baos = ByteArrayOutputStream()
        var quality = initialQuality
        val targetSize = 100 * 1024

        do {
            baos.reset()
            resized.compress(format, quality, baos)
            quality -= 10
        } while (baos.size() > targetSize && quality > 5)

        baos.toByteArray()
    } catch (e: Exception) {
        Log.e(TAG, "Error compressing file: ${e.message}")
        null
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

    fun Bitmap.cropSquare(filePath: String): Bitmap {
        // Read EXIF orientation
        val exif = ExifInterface(filePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        // Apply rotation only if necessary
        val matrix = Matrix()
        if (orientation != ExifInterface.ORIENTATION_NORMAL) {
            val angle = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            if (angle != 0f) matrix.postRotate(angle)
        }

        // If no rotation needed, skip creating an intermediate bitmap
        val fixed = if (!matrix.isIdentity) {
            Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
        } else {
            this
        }

        // Crop centered square
        val w = fixed.width
        val h = fixed.height
        val size = minOf(w, h)
        val x = (w - size) / 2
        val y = (h - size) / 2

        return Bitmap.createBitmap(fixed, x, y, size, size)
    }
}