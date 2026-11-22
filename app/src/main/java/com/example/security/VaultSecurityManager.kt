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
        // Preserve a fallback key created by older installations so existing vault
        // entries remain readable. New fallback keys are permitted only in Robolectric.
        if (prefs.contains(FALLBACK_SECRET_KEY)) return

        if (isRobolectricRuntime()) {
            createFallbackSecretKeyForTests()
            return
        }

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
            throw IllegalStateException(
                "Secure Android Keystore is unavailable; vault encryption is disabled.",
                error
            )
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
        // Existing fallback keys remain supported for compatibility, but new
        // production keys must always be held by Android Keystore.
        if (prefs.contains(FALLBACK_SECRET_KEY)) return getFallbackSecretKey()

        return try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
            keyStore.load(null)
            check(keyStore.containsAlias(KEY_ALIAS)) { "Vault encryption key is missing." }
            keyStore.getKey(KEY_ALIAS, null) as? SecretKey
                ?: throw IllegalStateException("Vault encryption key is unavailable.")
        } catch (error: Exception) {
            throw IllegalStateException(
                "Secure Android Keystore is unavailable; vault data was not changed.",
                error
            )
        }
    }

    private fun getFallbackSecretKey(): SecretKey {
        val keyB64 = prefs.getString(FALLBACK_SECRET_KEY, null)
            ?: throw IllegalStateException("The legacy vault encryption key is missing.")
        val raw = Base64.decode(keyB64, Base64.NO_WRAP)
        require(raw.size == 32) { "The legacy vault encryption key is invalid." }
        return SecretKeySpec(raw, "AES")
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
