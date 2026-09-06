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
    }

    @Test
    fun `test vault security manager encryption at rest`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val security = VaultSecurityManager(context)

        val originalSecret = "SuperSecureVaultSecret#2026!"
        val encrypted = security.encryptAtRest(originalSecret)

        assertNotEquals(originalSecret, encrypted)
        val decrypted = security.decryptAtRest(encrypted)
        assertEquals(originalSecret, decrypted)
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
}
