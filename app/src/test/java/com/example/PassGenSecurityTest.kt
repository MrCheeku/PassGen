package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.VaultItem
import com.example.security.PasswordGenerator
import com.example.security.PasswordHealthAnalyzer
import com.example.security.PasswordStrengthCalculator
import com.example.security.StrengthLevel
import com.example.security.VaultSecurityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PassGenSecurityTest {

    @Test
    fun `test password generator generates requested length and charsets`() {
        val options = PasswordGenerator.GeneratorOptions(
            length = 20,
            includeUppercase = true,
            includeLowercase = true,
            includeNumbers = true,
            includeSymbols = true
        )
        val password = PasswordGenerator.generate(options)

        assertEquals(20, password.length)
        assertTrue(password.any { it.isUpperCase() })
        assertTrue(password.any { it.isLowerCase() })
        assertTrue(password.any { it.isDigit() })
        assertTrue(password.any { "!@#$%^&*()-_=+[]{}|;:,.<>?".contains(it) })
    }

    @Test
    fun `generator rejects impossible lengths and guarantees only selected groups`() {
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            PasswordGenerator.generate(PasswordGenerator.GeneratorOptions(length = 5))
        }

        val digitsOnly = PasswordGenerator.generate(
            PasswordGenerator.GeneratorOptions(
                length = 12,
                includeUppercase = false,
                includeLowercase = false,
                includeNumbers = true,
                includeSymbols = false
            )
        )
        assertEquals(12, digitsOnly.length)
        assertTrue(digitsOnly.all { it.isDigit() })

        val fallback = PasswordGenerator.generate(
            PasswordGenerator.GeneratorOptions(
                length = 12,
                includeUppercase = false,
                includeLowercase = false,
                includeNumbers = false,
                includeSymbols = false
            )
        )
        assertEquals(12, fallback.length)
        assertTrue(fallback.all { it.isLowerCase() || it.isDigit() })
    }

    @Test
    fun `strength calculator penalizes repeated sequential and common patterns`() {
        val randomLike = PasswordStrengthCalculator.calculate("mP9!vX2@qL7#zR5&")
        val repeated = PasswordStrengthCalculator.calculate("AAAAaaaa1111!!!!")
        val sequential = PasswordStrengthCalculator.calculate("Abcd1234!XYZ")
        val common = PasswordStrengthCalculator.calculate("Password!23456789")

        assertTrue(randomLike.score > repeated.score)
        assertTrue(randomLike.score > sequential.score)
        assertTrue(common.score <= 1)
        assertTrue(repeated.feedback.isNotEmpty())
        assertTrue(sequential.feedback.isNotEmpty())
    }

    @Test
    fun `test password generator numbers only`() {
        val options = PasswordGenerator.GeneratorOptions(
            length = 8,
            includeUppercase = false,
            includeLowercase = false,
            includeNumbers = true,
            includeSymbols = false
        )
        val pin = PasswordGenerator.generate(options)
        assertEquals(8, pin.length)
        assertTrue(pin.all { it.isDigit() })
    }

    @Test
    fun `test password strength calculator detects weak and strong passwords`() {
        val weak = PasswordStrengthCalculator.calculate("12345")
        assertTrue(weak.level == StrengthLevel.WEAK || weak.level == StrengthLevel.VERY_WEAK)
        assertTrue(weak.score <= 1)

        val veryStrong = PasswordStrengthCalculator.calculate("K9#vX!mP4@wQ8\$zL2&")
        assertTrue(veryStrong.level == StrengthLevel.STRONG || veryStrong.level == StrengthLevel.VERY_STRONG)
        assertTrue(veryStrong.score >= 3)
        assertTrue(veryStrong.entropyBits > 60)
    }

    @Test
    fun `test password health analyzer detects issues`() {
        val items = listOf(
            VaultItem(
                id = 1,
                title = "Email",
                username = "user@test.com",
                password = "123", // short & weak
                website = "mail.com",
                notes = ""
            ),
            VaultItem(
                id = 2,
                title = "Shopping",
                username = "user@test.com",
                password = "123", // reused!
                website = "shop.com",
                notes = ""
            ),
            VaultItem(
                id = 3,
                title = "Bank",
                username = "user_bank",
                password = "P@ssw0rdSecureKey#99!",
                website = "bank.com",
                notes = ""
            )
        )

        val report = PasswordHealthAnalyzer.analyze(items)
        assertEquals(3, report.totalCount)
        assertEquals(1, report.strongCount)
        assertTrue(report.weakCount >= 2)
        assertEquals(2, report.shortCount)
        assertEquals(2, report.reusedCount) // 2 items share "123"
        assertEquals(2, report.needingAttentionCount)
        assertEquals(33, report.overallScore)
    }

    @Test
    fun `empty vault has a neutral health score`() {
        val report = PasswordHealthAnalyzer.analyze(emptyList())
        assertEquals(0, report.totalCount)
        assertEquals(100, report.overallScore)
    }

    @Test
    fun `empty passwords are weak but are not counted as reused`() {
        val items = listOf(
            VaultItem(id = 10, title = "Missing one", username = "one", password = "", website = "one.test"),
            VaultItem(id = 11, title = "Missing two", username = "two", password = "", website = "two.test")
        )

        val report = PasswordHealthAnalyzer.analyze(items)
        assertEquals(2, report.weakCount)
        assertEquals(0, report.reusedCount)
        assertEquals(2, report.needingAttentionCount)
        assertEquals(0, report.overallScore)
    }

    @Test
    fun `test vault security manager encryption at rest`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val security = VaultSecurityManager(context)

        val originalSecret = "SuperSecureVaultSecret#2026!"
        val encrypted = security.encryptAtRest(originalSecret)

        assertNotEquals(originalSecret, encrypted)
        assertTrue(encrypted.startsWith("v1:"))
        val decrypted = security.decryptAtRest(encrypted)
        assertEquals(originalSecret, decrypted)
    }

    @Test
    fun `versioned vault ciphertext fails closed when corrupted`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val security = VaultSecurityManager(context)
        val encrypted = security.encryptAtRest("vault-secret-to-protect")
        val corrupted = encrypted.dropLast(2) + "xx"

        try {
            security.decryptAtRest(corrupted)
            throw AssertionError("Corrupted versioned ciphertext must not be returned as plaintext.")
        } catch (expected: IllegalStateException) {
            assertTrue(expected.message!!.contains("Unable to decrypt"))
        }
    }

    @Test
    fun `legacy fallback key remains usable when Android Keystore is unavailable`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val preferences = context.getSharedPreferences("passgen_vault_security", Context.MODE_PRIVATE)
        val fallbackKey = ByteArray(32) { (it + 1).toByte() }
        preferences.edit()
            .putString("passgen_fallback_local_key", android.util.Base64.encodeToString(fallbackKey, android.util.Base64.NO_WRAP))
            .commit()

        val security = VaultSecurityManager(context)
        val encrypted = security.encryptAtRest("fallback-key-data")
        assertEquals("fallback-key-data", security.decryptAtRest(encrypted))
        assertTrue(preferences.contains("passgen_fallback_local_key"))
    }

    @Test
    fun `invalid legacy fallback key fails closed instead of regenerating a key`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val preferences = context.getSharedPreferences("passgen_vault_security", Context.MODE_PRIVATE)
        val originalKey = preferences.getString("passgen_fallback_local_key", null)
        preferences.edit().putString("passgen_fallback_local_key", "invalid-base64-key").commit()

        try {
            val security = VaultSecurityManager(context)
            assertThrows(IllegalStateException::class.java) {
                security.encryptAtRest("must-not-be-stored-unencrypted")
            }
        } finally {
            val editor = preferences.edit()
            if (originalKey == null) {
                editor.remove("passgen_fallback_local_key")
            } else {
                editor.putString("passgen_fallback_local_key", originalKey)
            }
            editor.commit()
        }
    }

    @Test
    fun `legacy plaintext fallback values remain readable`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val security = VaultSecurityManager(context)
        assertEquals("legacy-plain-value", security.decryptAtRest("legacy-plain-value"))
    }

    @Test
    fun `test encrypted backup export and import`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val security = VaultSecurityManager(context)

        val rawPayload = """[{"title":"Test","username":"user","password":"secretPassword123"}]"""
        val passphrase = "MasterPassphrase!123"

        val encryptedPayload = security.encryptBackupPayload(rawPayload, passphrase)
        assertFalse(encryptedPayload.contains("secretPassword123"))

        val decryptedPayload = security.decryptBackupPayload(encryptedPayload, passphrase)
        assertEquals(rawPayload, decryptedPayload)
    }

    @Test
    fun `test master lock pin setup and verification`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val security = VaultSecurityManager(context)

        security.disableMasterLock("any")
        assertFalse(security.isMasterLockEnabled())

        val setup = security.setupMasterLock("5892")
        assertTrue(setup)
        assertTrue(security.isMasterLockEnabled())

        assertTrue(security.verifyMasterLock("5892"))
        assertFalse(security.verifyMasterLock("0000"))
        assertFalse(security.verifyMasterLock("5891"))

        val changed = security.changeMasterLock("5892", "9999")
        assertTrue(changed)
        assertTrue(security.verifyMasterLock("9999"))
        assertFalse(security.verifyMasterLock("5892"))
    }


    @Test
    fun `password generator honours uppercase and symbol only selections at boundary lengths`() {
        val uppercase = PasswordGenerator.generate(
            PasswordGenerator.GeneratorOptions(
                length = 6,
                includeUppercase = true,
                includeLowercase = false,
                includeNumbers = false,
                includeSymbols = false
            )
        )
        assertEquals(6, uppercase.length)
        assertTrue(uppercase.all { it.isUpperCase() })

        val symbols = PasswordGenerator.generate(
            PasswordGenerator.GeneratorOptions(
                length = 64,
                includeUppercase = false,
                includeLowercase = false,
                includeNumbers = false,
                includeSymbols = true
            )
        )
        assertEquals(64, symbols.length)
        assertTrue(symbols.all { "!@#$%^&*()-_=+[]{}|;:,.<>?".contains(it) })
    }

    @Test
    fun `password generator rejects lengths above the supported maximum`() {
        assertThrows(IllegalArgumentException::class.java) {
            PasswordGenerator.generate(PasswordGenerator.GeneratorOptions(length = 65))
        }
    }

    @Test
    fun `strength calculator handles empty short descending and case insensitive common patterns`() {
        val empty = PasswordStrengthCalculator.calculate("")
        val short = PasswordStrengthCalculator.calculate("a")
        val descending = PasswordStrengthCalculator.calculate("Zyxw9876!@#")
        val commonMixedCase = PasswordStrengthCalculator.calculate("WELCome2026!Aa")

        assertEquals(StrengthLevel.VERY_WEAK, empty.level)
        assertEquals(0.0, empty.entropyBits, 0.0)
        assertEquals(StrengthLevel.VERY_WEAK, short.level)
        assertTrue(descending.feedback.any { it.contains("sequential") })
        assertTrue(commonMixedCase.feedback.any { it.contains("common words") })
        assertTrue(commonMixedCase.score <= 1)
    }

    @Test
    fun `health analyzer reports excellent good attention and urgent summaries`() {
        fun item(id: Long, password: String, updatedAt: Long = System.currentTimeMillis()) =
            VaultItem(
                id = id,
                title = "Credential $id",
                username = "user$id",
                password = password,
                website = "https://service$id.test",
                updatedAt = updatedAt
            )

        val strongPasswords = listOf(
            "mP9!vX2@qL7#zR5&",
            "tR8@kM4!pV6#xQ2&",
            "bN7#sL3@wC9!fH5%"
        )
        val excellent = PasswordHealthAnalyzer.analyze(listOf(item(1, strongPasswords[0])))
        assertTrue(excellent.summaryLabel.contains("Excellent"))
        assertEquals(100, excellent.overallScore)

        val good = PasswordHealthAnalyzer.analyze(
            listOf(
                item(1, "123"),
                item(2, strongPasswords[0]),
                item(3, strongPasswords[1]),
                item(4, strongPasswords[2])
            )
        )
        assertTrue(good.summaryLabel.contains("Good"))
        assertEquals(75, good.overallScore)

        val attention = PasswordHealthAnalyzer.analyze(
            listOf(
                item(1, "123"),
                item(2, "abc"),
                item(3, strongPasswords[0]),
                item(4, strongPasswords[1])
            )
        )
        assertTrue(attention.summaryLabel.contains("need attention"))
        assertEquals(50, attention.overallScore)

        val urgent = PasswordHealthAnalyzer.analyze(
            listOf(
                item(1, "123"),
                item(2, "abc"),
                item(3, "tiny"),
                item(4, strongPasswords[0])
            )
        )
        assertTrue(urgent.summaryLabel.contains("urgent attention"))
        assertEquals(25, urgent.overallScore)
    }

    @Test
    fun `health analyzer counts old credentials and missing websites separately`() {
        val now = System.currentTimeMillis()
        val oldTimestamp = now - (91L * 24 * 60 * 60 * 1000)
        val report = PasswordHealthAnalyzer.analyze(
            listOf(
                VaultItem(
                    id = 1,
                    title = "Old",
                    username = "old-user",
                    password = "mP9!vX2@qL7#zR5&",
                    website = "",
                    updatedAt = oldTimestamp
                ),
                VaultItem(
                    id = 2,
                    title = "New",
                    username = "new-user",
                    password = "tR8@kM4!pV6#xQ2&",
                    website = "https://new.test",
                    updatedAt = now
                )
            )
        )

        assertEquals(1, report.oldCount)
        assertEquals(1, report.missingWebsiteCount)
        assertEquals(1, report.needingAttentionCount)
    }

    @Test
    fun `at rest encryption supports empty and legacy unversioned values`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val security = VaultSecurityManager(context)

        assertEquals("", security.encryptAtRest(""))
        assertEquals("", security.decryptAtRest(""))

        val encrypted = security.encryptAtRest("legacy-ciphertext-value")
        assertTrue(encrypted.startsWith("v1:"))
        assertEquals(
            "legacy-ciphertext-value",
            security.decryptAtRest(encrypted.removePrefix("v1:"))
        )
        assertEquals("legacy-plaintext", security.decryptAtRest("legacy-plaintext"))
    }

    @Test
    fun `master lock rejects short pins and failed authentication changes nothing`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val security = VaultSecurityManager(context)

        assertFalse(security.setupMasterLock("123"))
        assertTrue(security.verifyMasterLock("anything"))

        assertTrue(security.setupMasterLock("2468"))
        assertFalse(security.changeMasterLock("0000", "1357"))
        assertFalse(security.disableMasterLock("0000"))
        assertTrue(security.verifyMasterLock("2468"))

        assertFalse(security.changeMasterLock("2468", "123"))
        assertTrue(security.verifyMasterLock("2468"))
        assertTrue(security.disableMasterLock("2468"))
        assertFalse(security.isMasterLockEnabled())
    }

    @Test
    fun `theme preferences default to system and preserve chosen theme`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val security = VaultSecurityManager(context)

        assertEquals("system", security.getAppTheme())
        security.setAppTheme("dark")
        assertEquals("dark", security.getAppTheme())
        security.setAppTheme("light")
        assertEquals("light", security.getAppTheme())
    }

    @Test
    fun `encrypted backup rejects empty and incorrect passwords`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val security = VaultSecurityManager(context)

        assertThrows(IllegalArgumentException::class.java) {
            security.encryptBackupPayload("[]", "")
        }

        val backup = security.encryptBackupPayload("[]", "CorrectPassphrase!2026")
        assertThrows(Exception::class.java) {
            security.decryptBackupPayload(backup, "WrongPassphrase!2026")
        }
        assertThrows(IllegalArgumentException::class.java) {
            security.decryptBackupPayload(backup, "")
        }
        assertThrows(IllegalStateException::class.java) {
            security.decryptBackupPayload("{}", "CorrectPassphrase!2026")
        }
    }


    @Test
    fun `generator falls back to lowercase and digits when no character groups are selected`() {
        val password = PasswordGenerator.generate(
            PasswordGenerator.GeneratorOptions(
                length = 12,
                includeUppercase = false,
                includeLowercase = false,
                includeNumbers = false,
                includeSymbols = false
            )
        )

        assertEquals(12, password.length)
        assertTrue(password.all { it.isLowerCase() || it.isDigit() })
    }

    @Test
    fun `at rest encryption fails closed if its legacy fallback key is removed`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val preferences = context.getSharedPreferences("passgen_vault_security", Context.MODE_PRIVATE)
        val security = VaultSecurityManager(context)
        val originalKey = preferences.getString("passgen_fallback_local_key", null)

        try {
            preferences.edit().remove("passgen_fallback_local_key").commit()
            assertThrows(IllegalStateException::class.java) {
                security.encryptAtRest("never-store-this-as-plaintext")
            }
        } finally {
            val editor = preferences.edit()
            if (originalKey == null) editor.remove("passgen_fallback_local_key")
            else editor.putString("passgen_fallback_local_key", originalKey)
            editor.commit()
        }
    }

    @Test
    fun `truncated versioned ciphertext is rejected rather than returned as plaintext`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val security = VaultSecurityManager(context)

        assertThrows(IllegalStateException::class.java) {
            security.decryptAtRest("v1:AA==")
        }
    }

    @Test
    fun `encrypted backup rejects missing iv and missing ciphertext fields`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val security = VaultSecurityManager(context)
        val salt = android.util.Base64.encodeToString(ByteArray(16), android.util.Base64.NO_WRAP)
        val iv = android.util.Base64.encodeToString(ByteArray(12), android.util.Base64.NO_WRAP)

        val missingIv = """{"salt":"$salt","ciphertext":"AA=="}"""
        val missingCiphertext = """{"salt":"$salt","iv":"$iv"}"""

        assertThrows(IllegalStateException::class.java) {
            security.decryptBackupPayload(missingIv, "ExportPassphrase!2026")
        }
        assertThrows(IllegalStateException::class.java) {
            security.decryptBackupPayload(missingCiphertext, "ExportPassphrase!2026")
        }
    }

}
