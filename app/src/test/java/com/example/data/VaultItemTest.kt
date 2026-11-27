package com.example.data

import com.example.security.StrengthLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VaultItemTest {

    @Test
    fun `derived flags identify weak short and missing website values`() {
        val item = VaultItem(
            title = "Email",
            username = "user",
            password = "tiny",
            website = "   "
        )

        assertTrue(item.isWeak)
        assertTrue(item.isShort)
        assertTrue(item.isMissingWebsite)
        assertEquals(StrengthLevel.VERY_WEAK, item.strength.level)
    }

    @Test
    fun `strong password and nonblank website are not flagged`() {
        val item = VaultItem(
            title = "Bank",
            username = "user",
            password = "mP9!vX2@qL7#zR5&",
            website = "https://bank.test"
        )

        assertFalse(item.isWeak)
        assertFalse(item.isShort)
        assertFalse(item.isMissingWebsite)
    }

    @Test
    fun `old credential threshold is ninety days`() {
        val now = System.currentTimeMillis()
        val ninetyOneDaysAgo = now - (91L * 24 * 60 * 60 * 1000)

        val oldItem = VaultItem(
            title = "Old",
            username = "user",
            password = "mP9!vX2@qL7#zR5&",
            updatedAt = ninetyOneDaysAgo
        )
        val recentItem = oldItem.copy(updatedAt = now)

        assertTrue(oldItem.isOld)
        assertFalse(recentItem.isOld)
    }
}
