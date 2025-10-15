package org.librevault

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException

object MediaThumbnailer {

    fun generate(
        file: File,
        width: Int? = null,
        height: Int? = null,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        initialQuality: Int = 80,
    ): ByteArray? {
        if (!file.exists()) return null

        val bitmap = when {
            isImage(file) -> generateImageThumbnail(file, width, height)
            isVideo(file) -> generateVideoThumbnail(file, width, height)
            else -> null
        } ?: return null

        return bitmapToByteArray(bitmap, format, initialQuality)
    }

    private fun generateImageThumbnail(file: File, width: Int?, height: Int?): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(FileInputStream(file), null, options)

            val targetWidth = width ?: options.outWidth
            val targetHeight = height ?: options.outHeight

            options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight)
            options.inJustDecodeBounds = false

            val bitmap = BitmapFactory.decodeStream(FileInputStream(file), null, options) ?: return null
            ThumbnailUtils.extractThumbnail(bitmap, targetWidth, targetHeight)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun generateVideoThumbnail(file: File, width: Int?, height: Int?): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val frameBitmap = retriever.getFrameAtTime(0) ?: return null
            retriever.release()

            val targetWidth = width ?: frameBitmap.width
            val targetHeight = height ?: frameBitmap.height

            ThumbnailUtils.extractThumbnail(frameBitmap, targetWidth, targetHeight)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun bitmapToByteArray(
        bitmap: Bitmap,
        format: Bitmap.CompressFormat,
        initialQuality: Int,
    ): ByteArray? {
        return try {
            val targetMaxSize = 200_000
            val minSize = 60_000
            val maxDimension = 600      // Optional upper cap for decompressing
            val outputStream = ByteArrayOutputStream()
            var quality = 70            // start lower for more efficient thumbnails

            // --- Keep original shape but shrink proportionally if too large ---
            val scaled = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val ratio = minOf(
                    maxDimension.toFloat() / bitmap.width,
                    maxDimension.toFloat() / bitmap.height
                )
                val newWidth = (bitmap.width * ratio).toInt()
                val newHeight = (bitmap.height * ratio).toInt()
                bitmap.scale(newWidth, newHeight)
            } else bitmap

            // --- Initial compression ---
            scaled.compress(format, quality, outputStream)
            var byteArray = outputStream.toByteArray()

            // --- Drop quality until target size hit ---
            while (byteArray.size > targetMaxSize && quality > 40) {
                outputStream.reset()
                quality -= 10
                scaled.compress(format, quality, outputStream)
                byteArray = outputStream.toByteArray()
            }

            // --- If still too big, scale down further (keeping shape) ---
            if (byteArray.size > targetMaxSize) {
                var scaledBitmap = scaled
                var scaleFactor = 0.85f
                while (byteArray.size > targetMaxSize &&
                    scaledBitmap.width > 160 &&
                    scaledBitmap.height > 160
                ) {
                    val newWidth = (scaledBitmap.width * scaleFactor).toInt()
                    val newHeight = (scaledBitmap.height * scaleFactor).toInt()
                    scaledBitmap = scaledBitmap.scale(newWidth, newHeight)
                    outputStream.reset()
                    scaledBitmap.compress(format, quality, outputStream)
                    byteArray = outputStream.toByteArray()
                    scaleFactor -= 0.1f
                }
                if (scaledBitmap != scaled) scaledBitmap.recycle()
            }

            // --- If over-compressed, bring back a touch of quality ---
            if (byteArray.size < minSize && quality < initialQuality) {
                outputStream.reset()
                scaled.compress(format, (quality + 10).coerceAtMost(90), outputStream)
                byteArray = outputStream.toByteArray()
            }

            if (scaled != bitmap) scaled.recycle()
            byteArray
        } catch (e: Exception) {
            e.printStackTrace()
            null
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
