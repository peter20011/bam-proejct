package com.example.projekt

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties

import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.SecretKeySpec

object CryptoManager {
    private const val KEY_ALIAS = "SecureAppKey"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val IV_SIZE = 12
    private const val TAG_SIZE = 128

    fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) {
            return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)
            .setUserAuthenticationValidityDurationSeconds(-1)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    fun deleteKey() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.deleteEntry(KEY_ALIAS)
        }
    }

    fun exportEncrypted(data: String, password: String): ByteArray {
        val salt = ByteArray(16).apply { SecureRandom().nextBytes(this) }
        val key = deriveKeyFromPassword(password, salt)
        val cipher = Cipher.getInstance(AES_MODE)
        val xored = NativeXor.encryptXor(data.toByteArray(Charsets.UTF_8), 42)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(xored)
        return salt + iv + encrypted
    }

    fun importEncrypted(data: ByteArray, password: String): String {
        val salt = data.copyOfRange(0, 16)
        val iv = data.copyOfRange(16, 28)
        val cipherText = data.copyOfRange(28, data.size)
        val key = deriveKeyFromPassword(password, salt)
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_SIZE, iv))
        val decrypted = cipher.doFinal(cipherText)
        val unXored = NativeXor.encryptXor(decrypted,42)
        return String(unXored, StandardCharsets.UTF_8)
    }

    fun getEncryptCipher(): Cipher {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        return cipher
    }

    fun getDecryptCipher(iv: ByteArray): Cipher {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(TAG_SIZE, iv))
        return cipher
    }

    fun encryptWithCipher(plainText: String, cipher: Cipher): ByteArray {
        val xorInput = NativeXor.encryptXor(plainText.toByteArray(Charsets.UTF_8), 42)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(xorInput)
        return iv + encrypted
    }

    fun decryptWithCipher(data: ByteArray, cipher: Cipher): String {
        val decrypted = cipher.doFinal(data.copyOfRange(IV_SIZE, data.size))
        val unXored = NativeXor.encryptXor(decrypted, 42)
        return String(unXored, Charsets.UTF_8)
    }

    private fun deriveKeyFromPassword(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, 10000, 256)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }
}