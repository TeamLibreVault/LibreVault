package org.librevault

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Provides a single, hardware-backed AES key managed by Android Keystore.
 * The key never leaves secure storage and is cached for performance.
 */
object AndroidKeyProvider {

    private const val KEY_ALIAS = "VaultAESKey"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    }

    @Volatile
    private var cachedKey: SecretKey? = null

    fun getOrCreateSecretKey(): SecretKey {
        cachedKey?.let { return it }

        synchronized(this) {
            cachedKey?.let { return it }

            // If key already exists, return it
            val existingKey = keyStore.getKey(KEY_ALIAS, null)
            if (existingKey is SecretKey) {
                cachedKey = existingKey
                return existingKey
            }

            // Otherwise generate new AES key
            val keyGen = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )

            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setKeySize(256)
                .setUserAuthenticationRequired(false) // flip true for biometrics/PIN
                .build()

            keyGen.init(spec)
            return keyGen.generateKey().also { cachedKey = it }
        }
    }
}