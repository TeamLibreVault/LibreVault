package org.librevault.data.encryption

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object SecureFileCipher {

    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val IV_SIZE = 12
    private const val SALT_SIZE = 16
    private const val BUFFER_SIZE = 8192
    private const val PBKDF2_ITERATIONS = 65536
    private const val PBKDF2_KEY_LENGTH = 256

    /**
     * Returns raw random bytes.
     */
    fun generateBaseKey(length: Int = 32): ByteArray {
        val bytes = ByteArray(length)
        SecureRandom().nextBytes(bytes)
        return bytes
    }

    /**
     * Derive an AES key from a password represented as a ByteArray.
     * We convert the byte[] to a char[] in a 1:1 mapping so PBEKeySpec can consume it,
     * then clear sensitive memory where possible.
     */
    private fun deriveKeyFromPassword(passwordBytes: ByteArray, salt: ByteArray): SecretKeySpec {
        // Convert bytes -> chars in a reversible, lossless one-to-one way
        val pwdChars = CharArray(passwordBytes.size) { i -> (passwordBytes[i].toInt() and 0xFF).toChar() }
        try {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec = PBEKeySpec(pwdChars, salt, PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH)
            try {
                val tmp = factory.generateSecret(spec)
                // tmp.encoded is the derived key bytes (copy). Wrap into SecretKeySpec
                return SecretKeySpec(tmp.encoded, "AES")
            } finally {
                // best-effort: clear the PBE spec password
                spec.clearPassword()
            }
        } finally {
            // wipe our temporary char array
            pwdChars.fill('\u0000')
        }
    }

    fun encryptBytes(
        inputBytes: ByteArray,
        outputFile: File,
        key: ByteArray,
        onProgress: (Float) -> Unit = {},
        onComplete: () -> Unit = {}
    ) {
        val salt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
        val derivedKey = deriveKeyFromPassword(key, salt)

        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(AES_MODE)

        cipher.init(Cipher.ENCRYPT_MODE, derivedKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        val totalBytes = inputBytes.size.toFloat().coerceAtLeast(1f)
        var processedBytes = 0L
        var lastReportedProgress = 0f

        ByteArrayInputStream(inputBytes).use { bais ->
            BufferedInputStream(bais, BUFFER_SIZE).use { bufferedIn ->
                FileOutputStream(outputFile).use { fos ->
                    BufferedOutputStream(fos, BUFFER_SIZE).use { bufferedOut ->
                        // write salt + iv
                        bufferedOut.write(salt)
                        bufferedOut.write(iv)

                        val buffer = ByteArray(BUFFER_SIZE)
                        var bytesRead: Int
                        while (bufferedIn.read(buffer).also { bytesRead = it } != -1) {
                            val encrypted = cipher.update(buffer, 0, bytesRead)
                            if (encrypted != null) {
                                bufferedOut.write(encrypted)
                            }
                            processedBytes += bytesRead
                            val progress = processedBytes.toFloat() / totalBytes

                            // report once every 1% change to avoid flooding
                            if (progress - lastReportedProgress >= 0.01f || progress == 1f) {
                                lastReportedProgress = progress
                                onProgress(progress)
                            }
                        }

                        // finalize encryption
                        val finalBytes = cipher.doFinal()
                        if (finalBytes != null) bufferedOut.write(finalBytes)
                        bufferedOut.flush()
                    }
                }
            }
        }

        onComplete()
    }

    fun encryptFile(
        inputFile: File,
        outputFile: File,
        key: ByteArray,
        onProgress: (Float) -> Unit = {},
        onComplete: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) = runCatching {
        val salt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
        val derivedKey = deriveKeyFromPassword(key, salt)

        val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, derivedKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        val totalBytes = inputFile.length().toFloat().coerceAtLeast(1f)
        var processedBytes = 0L
        var lastReportedProgress = 0f

        FileInputStream(inputFile).use { fis ->
            BufferedInputStream(fis, BUFFER_SIZE).use { bufferedIn ->
                FileOutputStream(outputFile).use { fos ->
                    BufferedOutputStream(fos, BUFFER_SIZE).use { bufferedOut ->
                        // write salt + iv
                        bufferedOut.write(salt)
                        bufferedOut.write(iv)

                        val buffer = ByteArray(BUFFER_SIZE)
                        var bytesRead: Int
                        while (bufferedIn.read(buffer).also { bytesRead = it } != -1) {
                            val encrypted = cipher.update(buffer, 0, bytesRead)
                            if (encrypted != null) {
                                bufferedOut.write(encrypted)
                            }
                            processedBytes += bytesRead.toLong()
                            val progress = processedBytes.toFloat() / totalBytes

                            // report once every 1% change to avoid flooding
                            if (progress - lastReportedProgress >= 0.01f || progress == 1f) {
                                lastReportedProgress = progress
                                onProgress(progress)
                            }
                        }

                        // finalize
                        val finalBytes = cipher.doFinal()
                        if (finalBytes != null) bufferedOut.write(finalBytes)
                        bufferedOut.flush()
                    }
                }
            }
        }

        onComplete()
    }.onFailure { onError(it) }

    /**
     * Decrypts file written by encryptFile(); expects [salt][iv][ciphertext]
     * key: ByteArray (the base key). Caller should zero the byte[] when done.
     */
    fun decryptFile(
        inputFile: File,
        outputFile: File,
        key: ByteArray,
        onProgress: (Float) -> Unit = {},
        onComplete: () -> Unit = {}
    ) {
        val totalCiphertextBytes = (inputFile.length() - SALT_SIZE - IV_SIZE).toFloat().coerceAtLeast(1f)
        var processedBytes = 0L
        var lastReportedProgress = 0f

        FileInputStream(inputFile).use { fis ->
            BufferedInputStream(fis, BUFFER_SIZE).use { bufferedIn ->
                // read salt and iv
                val salt = ByteArray(SALT_SIZE)
                if (bufferedIn.read(salt) != SALT_SIZE) throw IllegalStateException("Unable to read salt")

                val iv = ByteArray(IV_SIZE)
                if (bufferedIn.read(iv) != IV_SIZE) throw IllegalStateException("Unable to read IV")

                val derivedKey = deriveKeyFromPassword(passwordBytes = key, salt = salt)
                val cipher = Cipher.getInstance(AES_MODE)
                cipher.init(Cipher.DECRYPT_MODE, derivedKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))

                FileOutputStream(outputFile).use { fos ->
                    BufferedOutputStream(fos, BUFFER_SIZE).use { bufferedOut ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        var bytesRead: Int
                        while (bufferedIn.read(buffer).also { bytesRead = it } != -1) {
                            val decrypted = cipher.update(buffer, 0, bytesRead)
                            if (decrypted != null) {
                                bufferedOut.write(decrypted)
                            }
                            processedBytes += bytesRead.toLong()
                            val progress = processedBytes.toFloat() / totalCiphertextBytes
                            if (progress - lastReportedProgress >= 0.01f || progress == 1f) {
                                lastReportedProgress = progress
                                onProgress(progress)
                            }
                        }

                        // finalize - will throw AEADBadTagException if tag doesn't validate
                        val finalBytes = cipher.doFinal()
                        if (finalBytes != null) bufferedOut.write(finalBytes)
                        bufferedOut.flush()
                    }
                }
            }
        }

        onComplete()
    }

    fun decryptToBytes(
        inputFile: File,
        key: ByteArray,
        onProgress: (Float) -> Unit = {}
    ): ByteArray {
        val totalCiphertextBytes =
            (inputFile.length() - SALT_SIZE - IV_SIZE).toFloat().coerceAtLeast(1f)
        var processedBytes = 0L
        var lastReportedProgress = 0f

        FileInputStream(inputFile).use { fis ->
            BufferedInputStream(fis, BUFFER_SIZE).use { bufferedIn ->
                // read salt and IV
                val salt = ByteArray(SALT_SIZE)
                if (bufferedIn.read(salt) != SALT_SIZE)
                    throw IllegalStateException("Unable to read salt")

                val iv = ByteArray(IV_SIZE)
                if (bufferedIn.read(iv) != IV_SIZE)
                    throw IllegalStateException("Unable to read IV")

                val derivedKey = deriveKeyFromPassword(passwordBytes = key, salt = salt)
                val cipher = Cipher.getInstance(AES_MODE)
                cipher.init(Cipher.DECRYPT_MODE, derivedKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))

                // use a single expandable buffer instead of chunk list
                val outputStream = ByteArrayOutputStream(BUFFER_SIZE)

                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int
                while (bufferedIn.read(buffer).also { bytesRead = it } != -1) {
                    val decrypted = cipher.update(buffer, 0, bytesRead)
                    if (decrypted != null) outputStream.write(decrypted)

                    processedBytes += bytesRead
                    val progress = processedBytes.toFloat() / totalCiphertextBytes
                    if (progress - lastReportedProgress >= 0.02f || progress == 1f) {
                        lastReportedProgress = progress
                        onProgress(progress)
                    }
                }

                // finalize
                val finalBytes = cipher.doFinal()
                if (finalBytes != null) outputStream.write(finalBytes)

                val output = outputStream.toByteArray()

                return output
            }
        }
    }

}