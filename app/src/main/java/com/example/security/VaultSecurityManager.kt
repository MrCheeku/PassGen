package com.example.security

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class VaultSecurityManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val secureRandom = SecureRandom()

    companion object {
        private const val PREFS_NAME = "passgen_vault_security"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "PassGenLocalEncryptionKey"
        private const val FALLBACK_SECRET_KEY = "passgen_fallback_local_key"
        private const val CIPHERTEXT_PREFIX = "v1:"

        private const val PREF_LOCK_ENABLED = "pref_lock_enabled"
        private const val PREF_PIN_HASH = "pref_pin_hash"
        private const val PREF_PIN_SALT = "pref_pin_salt"
        private const val PREF_THEME = "pref_app_theme" // "system", "dark", "light"

        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 128
        private const val PBKDF2_ITERATIONS = 10000
        private const val PBKDF2_KEY_LENGTH = 256
    }

    init {
        ensureAtRestKey()
    }

    private fun ensureAtRestKey() {
        try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    KEYSTORE_PROVIDER
                )
                val spec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGenerator.init(spec)
                keyGenerator.generateKey()
            }
        } catch (error: Exception) {
            // Preserve old installations' legacy key, but never create a new
            // plaintext-stored fallback key on a real Android device.
            if (!prefs.contains(FALLBACK_SECRET_KEY)) {
                if (isRobolectricRuntime()) {
                    createFallbackSecretKeyForTests()
                } else {
                    throw IllegalStateException(
                        "Secure Android Keystore is unavailable; vault encryption is disabled.",
                        error
                    )
                }
            }
        }
    }

    private fun isRobolectricRuntime(): Boolean =
        Build.FINGERPRINT.startsWith("robolectric", ignoreCase = true)

    private fun createFallbackSecretKeyForTests() {
        val keyBytes = ByteArray(32)
        secureRandom.nextBytes(keyBytes)
        val stored = prefs.edit()
            .putString(FALLBACK_SECRET_KEY, Base64.encodeToString(keyBytes, Base64.NO_WRAP))
            .commit()
        check(stored) { "Unable to initialize the test vault encryption key." }
    }

    private fun getSecretKey(): SecretKey {
        // New writes use Android Keystore whenever it is available.
        try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)
            if (keyStore.containsAlias(KEY_ALIAS)) {
                return keyStore.getKey(KEY_ALIAS, null) as? SecretKey
                    ?: throw IllegalStateException("Vault encryption key is unavailable.")
            }
        } catch (error: Exception) {
            if (!prefs.contains(FALLBACK_SECRET_KEY)) {
                throw IllegalStateException(
                    "Secure Android Keystore is unavailable; vault data was not changed.",
                    error
                )
            }
        }

        if (prefs.contains(FALLBACK_SECRET_KEY)) return getFallbackSecretKey()
        throw IllegalStateException("Vault encryption key is missing.")
    }

    private fun getDecryptionKeys(): List<SecretKey> {
        val candidates = mutableListOf<SecretKey>()
        try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)
            if (keyStore.containsAlias(KEY_ALIAS)) {
                (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let(candidates::add)
            }
        } catch (_: Exception) {
            // A legacy fallback key may still decrypt existing records.
        }

        if (prefs.contains(FALLBACK_SECRET_KEY)) {
            try {
                candidates.add(getFallbackSecretKey())
            } catch (error: Exception) {
                if (candidates.isEmpty()) throw error
            }
        }

        check(candidates.isNotEmpty()) { "No usable vault encryption key is available." }
        return candidates
    }

    private fun getFallbackSecretKey(): SecretKey {
        val keyB64 = prefs.getString(FALLBACK_SECRET_KEY, null)
            ?: throw IllegalStateException("The legacy vault encryption key is missing.")
        val raw = Base64.decode(keyB64, Base64.NO_WRAP)
        require(raw.size == 32) { "The legacy vault encryption key is invalid." }
        return SecretKeySpec(raw, "AES")
    }

    /**
     * Encrypts sensitive strings with AES-256-GCM.
     * New writes use Android Keystore; the old fallback is used only when an
     * installation already has that key and Keystore is unavailable.
     */
    fun encryptAtRest(plainText: String): String {
        if (plainText.isEmpty()) return ""
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ByteArray(GCM_IV_LENGTH)
            secureRandom.nextBytes(iv)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), GCMParameterSpec(GCM_TAG_LENGTH, iv))

            val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
            return CIPHERTEXT_PREFIX + Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (error: Exception) {
            throw IllegalStateException("Vault encryption failed; the value was not stored.", error)
        }
    }

    /**
     * Reads versioned AES-GCM ciphertext and legacy unversioned values.
     * Versioned ciphertext always fails closed when authentication or decryption fails.
     */
    fun decryptAtRest(encryptedText: String): String {
        if (encryptedText.isEmpty()) return ""

        if (encryptedText.startsWith(CIPHERTEXT_PREFIX)) {
            val payload = encryptedText.removePrefix(CIPHERTEXT_PREFIX)
            return try {
                decryptCiphertextPayload(payload)
            } catch (error: Exception) {
                throw IllegalStateException(
                    "Unable to decrypt a versioned vault value. The stored data was not changed.",
                    error
                )
            }
        }

        // Older releases sometimes stored plaintext after an encryption failure.
        // Retain that compatibility path while attempting both known legacy key sources.
        return try {
            val decoded = Base64.decode(encryptedText, Base64.NO_WRAP)
            if (decoded.size < GCM_IV_LENGTH + (GCM_TAG_LENGTH / 8)) {
                encryptedText
            } else {
                try {
                    decryptCiphertextBytes(decoded)
                } catch (_: Exception) {
                    encryptedText
                }
            }
        } catch (_: Exception) {
            encryptedText
        }
    }

    private fun decryptCiphertextPayload(payload: String): String {
        val combined = Base64.decode(payload, Base64.NO_WRAP)
        require(combined.size >= GCM_IV_LENGTH + (GCM_TAG_LENGTH / 8)) {
            "Encrypted vault value is truncated."
        }
        return decryptCiphertextBytes(combined)
    }

    private fun decryptCiphertextBytes(combined: ByteArray): String {
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val cipherText = combined.copyOfRange(GCM_IV_LENGTH, combined.size)
        var lastFailure: Exception? = null

        // Some installations may contain records created under either Keystore
        // or a pre-existing fallback key. Try each available key without changing data.
        for (key in getDecryptionKeys()) {
            try {
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
                return String(cipher.doFinal(cipherText), Charsets.UTF_8)
            } catch (error: Exception) {
                lastFailure = error
            }
        }

        throw IllegalStateException(
            "Unable to decrypt vault data with the available keys.",
            lastFailure
        )
    }

    // --- Master Lock Management ---

    fun isMasterLockEnabled(): Boolean {
        return prefs.getBoolean(PREF_LOCK_ENABLED, false) && prefs.contains(PREF_PIN_HASH)
    }

    fun setupMasterLock(pin: String): Boolean {
        if (pin.length < 4) return false
        val salt = ByteArray(16)
        secureRandom.nextBytes(salt)
        val hash = hashPin(pin, salt)

        prefs.edit()
            .putBoolean(PREF_LOCK_ENABLED, true)
            .putString(PREF_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(PREF_PIN_HASH, Base64.encodeToString(hash, Base64.NO_WRAP))
            .apply()
        return true
    }

    fun verifyMasterLock(pin: String): Boolean {
        if (!isMasterLockEnabled()) return true
        val saltB64 = prefs.getString(PREF_PIN_SALT, null) ?: return false
        val hashB64 = prefs.getString(PREF_PIN_HASH, null) ?: return false

        val salt = Base64.decode(saltB64, Base64.NO_WRAP)
        val expectedHash = Base64.decode(hashB64, Base64.NO_WRAP)
        val computedHash = hashPin(pin, salt)

        return MessageDigest.isEqual(expectedHash, computedHash)
    }

    fun changeMasterLock(oldPin: String, newPin: String): Boolean {
        if (!verifyMasterLock(oldPin)) return false
        return setupMasterLock(newPin)
    }

    fun disableMasterLock(pin: String): Boolean {
        if (!verifyMasterLock(pin)) return false
        prefs.edit()
            .putBoolean(PREF_LOCK_ENABLED, false)
            .remove(PREF_PIN_HASH)
            .remove(PREF_PIN_SALT)
            .apply()
        return true
    }

    private fun hashPin(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }

    // --- Theme Settings ---

    fun getAppTheme(): String {
        return prefs.getString(PREF_THEME, "system") ?: "system"
    }

    fun setAppTheme(theme: String) {
        prefs.edit().putString(PREF_THEME, theme).apply()
    }

    // --- Encrypted Backup Export & Import ---

    fun encryptBackupPayload(jsonContent: String, exportPassword: String): String {
        require(exportPassword.isNotEmpty()) { "Backup password must not be empty" }
        val salt = ByteArray(16)
        secureRandom.nextBytes(salt)

        val spec = PBEKeySpec(exportPassword.toCharArray(), salt, PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

        val iv = ByteArray(GCM_IV_LENGTH)
        secureRandom.nextBytes(iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        val cipherBytes = cipher.doFinal(jsonContent.toByteArray(Charsets.UTF_8))

        val saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val cipherB64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP)

        // Envelope structure
        return """
            {
              "app": "PassGen",
              "format": "encrypted_vault_v1",
              "salt": "$saltB64",
              "iv": "$ivB64",
              "ciphertext": "$cipherB64"
            }
        """.trimIndent()
    }

    fun decryptBackupPayload(payloadJson: String, exportPassword: String): String {
        require(exportPassword.isNotEmpty()) { "Password cannot be empty" }

        val saltMatch = Regex("\"salt\"\\s*:\\s*\"([^\"]+)\"").find(payloadJson)?.groupValues?.get(1)
            ?: error("Invalid backup format: missing salt")
        val ivMatch = Regex("\"iv\"\\s*:\\s*\"([^\"]+)\"").find(payloadJson)?.groupValues?.get(1)
            ?: error("Invalid backup format: missing iv")
        val cipherMatch = Regex("\"ciphertext\"\\s*:\\s*\"([^\"]+)\"").find(payloadJson)?.groupValues?.get(1)
            ?: error("Invalid backup format: missing ciphertext")

        val salt = Base64.decode(saltMatch, Base64.NO_WRAP)
        val iv = Base64.decode(ivMatch, Base64.NO_WRAP)
        val cipherBytes = Base64.decode(cipherMatch, Base64.NO_WRAP)

        val spec = PBEKeySpec(exportPassword.toCharArray(), salt, PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        val decryptedBytes = cipher.doFinal(cipherBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}
