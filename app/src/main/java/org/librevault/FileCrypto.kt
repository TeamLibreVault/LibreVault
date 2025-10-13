package org.librevault

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec

private const val TRANSFORMATION = "AES/CBC/PKCS7Padding"
private const val IV_SIZE = 16
private const val BUFFER_SIZE = 256 * 1024 // Increased to 256KB for better throughput
private const val PROGRESS_UPDATE_THRESHOLD = 1024 * 1024 // Update progress every 1MB to reduce callback overhead

object FileCrypto {

    fun encryptFile(
        inputFile: File,
        outputFile: File,
        onProgress: ((Float) -> Unit)? = null,
        onComplete: (Boolean, Exception?) -> Unit
    ) {
        try {
            val key = AndroidKeyProvider.getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv

            val totalBytes = inputFile.length().toFloat()
            var processedBytes = 0L
            var lastProgressUpdate = 0L

            BufferedInputStream(FileInputStream(inputFile), BUFFER_SIZE).use { fis ->
                BufferedOutputStream(FileOutputStream(outputFile), BUFFER_SIZE).use { fos ->
                    fos.write(iv) // prepend IV
                    CipherOutputStream(fos, cipher).use { cos ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        var bytesRead: Int
                        while (fis.read(buffer).also { bytesRead = it } != -1) {
                            cos.write(buffer, 0, bytesRead)
                            processedBytes += bytesRead

                            // Throttle progress updates
                            if (onProgress != null &&
                                (processedBytes - lastProgressUpdate >= PROGRESS_UPDATE_THRESHOLD ||
                                        processedBytes == totalBytes.toLong())) {
                                onProgress.invoke(processedBytes / totalBytes)
                                lastProgressUpdate = processedBytes
                            }
                        }
                    }
                }
            }
            onComplete(true, null)
        } catch (e: Exception) {
            onComplete(false, e)
        }
    }

    fun decryptFile(
        inputFile: File,
        outputFile: File,
        onProgress: ((Float) -> Unit)? = null,
        onComplete: (Boolean, Exception?) -> Unit
    ) {
        try {
            val key = AndroidKeyProvider.getOrCreateSecretKey()

            // Pre-calculate total bytes for progress
            val totalBytes = inputFile.length().toFloat()
            var processedBytes = 0L
            var lastProgressUpdate = 0L

            BufferedInputStream(FileInputStream(inputFile), BUFFER_SIZE).use { fis ->
                val iv = ByteArray(IV_SIZE)
                if (fis.read(iv) != IV_SIZE) throw IllegalStateException("Invalid IV in file")

                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))

                CipherInputStream(fis, cipher).use { cis ->
                    BufferedOutputStream(FileOutputStream(outputFile), BUFFER_SIZE).use { fos ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        var bytesRead: Int
                        while (cis.read(buffer).also { bytesRead = it } != -1) {
                            fos.write(buffer, 0, bytesRead)
                            processedBytes += bytesRead

                            // Throttle progress updates to reduce overhead
                            if (onProgress != null &&
                                (processedBytes - lastProgressUpdate >= PROGRESS_UPDATE_THRESHOLD ||
                                        processedBytes == totalBytes.toLong())) {
                                onProgress.invoke(processedBytes / totalBytes)
                                lastProgressUpdate = processedBytes
                            }
                        }
                    }
                }
            }
            onComplete(true, null)
        } catch (e: Exception) {
            onComplete(false, e)
        }
    }

    // High-performance version for memory-mapped decryption (for advanced use cases)
    fun decryptFileOptimized(
        inputFile: File,
        outputFile: File,
        onProgress: ((Float) -> Unit)? = null,
        onComplete: (Boolean, Exception?) -> Unit
    ) {
        try {
            val key = AndroidKeyProvider.getOrCreateSecretKey()
            val totalBytes = inputFile.length()
            var processedBytes = 0L
            var lastProgressUpdate = 0L

            RandomAccessFile(inputFile, "r").use { raf ->
                // Read IV from beginning of file
                val iv = ByteArray(IV_SIZE)
                raf.read(iv)
                processedBytes += IV_SIZE

                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))

                BufferedOutputStream(FileOutputStream(outputFile), BUFFER_SIZE).use { fos ->
                    val inputBuffer = ByteArray(BUFFER_SIZE)
                    val outputBuffer = ByteArray(BUFFER_SIZE + 16) // Extra space for cipher output

                    var bytesRead: Int
                    while (raf.filePointer < totalBytes) {
                        val remaining = (totalBytes - raf.filePointer).toInt()
                        val chunkSize = if (remaining > BUFFER_SIZE) BUFFER_SIZE else remaining

                        bytesRead = raf.read(inputBuffer, 0, chunkSize)
                        if (bytesRead == -1) break

                        // Process chunk by chunk to avoid large memory allocations
                        val decryptedBytes = if (bytesRead == BUFFER_SIZE) {
                            cipher.update(inputBuffer, 0, bytesRead, outputBuffer)
                        } else {
                            // Last chunk - use doFinal
                            val finalOutput = cipher.doFinal(inputBuffer, 0, bytesRead)
                            fos.write(finalOutput)
                            finalOutput.size
                        }

                        if (decryptedBytes > 0 && bytesRead == BUFFER_SIZE) {
                            fos.write(outputBuffer, 0, decryptedBytes)
                        }

                        processedBytes += bytesRead

                        // Throttled progress updates
                        if (onProgress != null &&
                            (processedBytes - lastProgressUpdate >= PROGRESS_UPDATE_THRESHOLD ||
                                    processedBytes == totalBytes)) {
                            onProgress.invoke(processedBytes.toFloat() / totalBytes)
                            lastProgressUpdate = processedBytes
                        }
                    }
                }
            }
            onComplete(true, null)
        } catch (e: Exception) {
            onComplete(false, e)
        }
    }

    fun decryptFileToBytes(
        inputFile: File,
        onProgress: (Float) -> Unit
    ): ByteArray {
        val key = AndroidKeyProvider.getOrCreateSecretKey()
        val fileSize = inputFile.length()
        var processedBytes = 0L
        var lastProgressUpdate = 0L

        BufferedInputStream(FileInputStream(inputFile), BUFFER_SIZE).use { fis ->
            val iv = ByteArray(IV_SIZE)
            if (fis.read(iv) != IV_SIZE) throw IllegalStateException("Invalid IV in file")
            processedBytes += IV_SIZE

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))

            // Calculate output size more accurately
            val cipherTextSize = fileSize - IV_SIZE
            val output = ByteArray(cipher.getOutputSize(cipherTextSize.toInt()))

            var offset = 0
            val buffer = ByteArray(BUFFER_SIZE)
            CipherInputStream(fis, cipher).use { cis ->
                var bytesRead: Int
                while (cis.read(buffer).also { bytesRead = it } != -1) {
                    System.arraycopy(buffer, 0, output, offset, bytesRead)
                    offset += bytesRead
                    processedBytes += bytesRead

                    // Throttled progress updates
                    if (processedBytes - lastProgressUpdate >= PROGRESS_UPDATE_THRESHOLD ||
                        processedBytes == fileSize) {
                        val progress = offset.toFloat() / output.size
                        onProgress(progress.coerceIn(0f, 1f))
                        lastProgressUpdate = processedBytes
                    }
                }
            }

            onProgress(1f)
            return if (offset == output.size) output else output.copyOf(offset)
        }
    }
}