package com.example.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.VaultSecurityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VaultRepositoryTest {

    private lateinit var dao: InMemoryVaultDao
    private lateinit var securityManager: VaultSecurityManager
    private lateinit var repository: VaultRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        dao = InMemoryVaultDao()
        securityManager = VaultSecurityManager(context)
        repository = VaultRepository(dao, securityManager)
    }

    @Test
    fun `save trims metadata and encrypts password and notes`() = runBlocking {
        val password = "VerySecretPassword!2026"
        val notes = " private recovery note "

        val id = repository.saveItem(
            VaultItem(
                title = "  Example Account  ",
                username = "  cheeku  ",
                password = password,
                website = "  https://example.test  ",
                notes = notes
            )
        )

        val stored = dao.getById(id)!!
        assertEquals("Example Account", stored.title)
        assertEquals("cheeku", stored.username)
        assertEquals("https://example.test", stored.website)
        assertNotEquals(password, stored.encryptedPassword)
        assertNotEquals(notes.trim(), stored.encryptedNotes)
        assertTrue(stored.encryptedPassword.startsWith("v1:"))
        assertTrue(stored.encryptedNotes.startsWith("v1:"))

        val restored = repository.allVaultItems.first().single()
        assertEquals(password, restored.password)
        assertEquals(notes.trim(), restored.notes)
        assertEquals(id, restored.id)
    }

    @Test
    fun `update keeps the item id and persists the new encrypted values`() = runBlocking {
        val id = repository.saveItem(
            VaultItem(
                title = "Original",
                username = "owner",
                password = "OriginalSecret!123",
                notes = "first note"
            )
        )
        val original = repository.allVaultItems.first().single()

        repository.saveItem(
            original.copy(
                title = "Updated",
                password = "UpdatedSecret!456",
                notes = "new note"
            )
        )

        val stored = dao.getById(id)!!
        val restored = repository.allVaultItems.first().single()
        assertEquals(id, stored.id)
        assertEquals(id, restored.id)
        assertEquals("Updated", restored.title)
        assertEquals("UpdatedSecret!456", restored.password)
        assertEquals("new note", restored.notes)
        assertEquals(original.createdAt, restored.createdAt)
    }

    @Test
    fun `toggle favorite persists the opposite state`() = runBlocking {
        repository.saveItem(
            VaultItem(
                title = "Pinned item",
                username = "owner",
                password = "FavoriteSecret!123",
                isFavorite = false
            )
        )
        val item = repository.allVaultItems.first().single()

        repository.toggleFavorite(item)

        assertTrue(repository.allVaultItems.first().single().isFavorite)
    }

    @Test
    fun `delete and clear remove only the expected credentials`() = runBlocking {
        val firstId = repository.saveItem(
            VaultItem(title = "First", username = "one", password = "FirstSecret!123")
        )
        repository.saveItem(
            VaultItem(title = "Second", username = "two", password = "SecondSecret!123")
        )

        val firstItem = repository.allVaultItems.first().first { it.id == firstId }
        repository.deleteItem(firstItem)
        assertEquals(listOf("Second"), repository.allVaultItems.first().map { it.title })

        repository.clearAll()
        assertTrue(repository.allVaultItems.first().isEmpty())
    }

    @Test
    fun `encrypted backup round trips credential data without exposing secrets`() = runBlocking {
        val secret = "BackupOnlySecret!2026"
        repository.saveItem(
            VaultItem(
                title = "Backup account",
                username = "backup-user",
                password = secret,
                website = "https://backup.test",
                notes = "backup note",
                isFavorite = true,
                createdAt = 1234L,
                updatedAt = 5678L
            )
        )
        val originalItems = repository.allVaultItems.first()
        val encryptedBackup = repository.exportEncryptedBackup(originalItems, "ExportPassphrase!2026")

        assertFalse(encryptedBackup.contains(secret))
        assertTrue(encryptedBackup.contains("\"ciphertext\""))

        val targetDao = InMemoryVaultDao()
        val targetRepository = VaultRepository(targetDao, securityManager)
        assertEquals(1, targetRepository.importEncryptedBackup(encryptedBackup, "ExportPassphrase!2026"))

        val restored = targetRepository.allVaultItems.first().single()
        assertEquals("Backup account", restored.title)
        assertEquals("backup-user", restored.username)
        assertEquals(secret, restored.password)
        assertEquals("https://backup.test", restored.website)
        assertEquals("backup note", restored.notes)
        assertTrue(restored.isFavorite)
        assertEquals(1234L, restored.createdAt)
        assertEquals(5678L, restored.updatedAt)
    }

    @Test
    fun `empty backup imports zero rows`() = runBlocking {
        val encryptedBackup = repository.exportEncryptedBackup(emptyList(), "ExportPassphrase!2026")

        assertEquals(0, repository.importEncryptedBackup(encryptedBackup, "ExportPassphrase!2026"))
        assertTrue(repository.allVaultItems.first().isEmpty())
    }

    @Test
    fun `wrong backup password is rejected without inserting credentials`() {
        val failure = org.junit.Assert.assertThrows(Exception::class.java) {
            runBlocking {
                val encryptedBackup = repository.exportEncryptedBackup(
                    listOf(VaultItem(title = "A", username = "B", password = "Secret!123")),
                    "CorrectPassphrase!2026"
                )
                repository.importEncryptedBackup(encryptedBackup, "WrongPassphrase!2026")
            }
        }
        assertTrue(failure.message != null)
        assertTrue(dao.getAllVaultItems().first().isEmpty())
    }

    private class InMemoryVaultDao : VaultDao {
        private val rows = MutableStateFlow<List<VaultEntity>>(emptyList())
        private var nextId = 1L

        override fun getAllVaultItems(): Flow<List<VaultEntity>> = rows

        override suspend fun getById(id: Long): VaultEntity? =
            rows.value.firstOrNull { it.id == id }

        override suspend fun insert(item: VaultEntity): Long {
            val id = if (item.id == 0L) nextId++ else item.id
            rows.value = rows.value.filterNot { it.id == id } + item.copy(id = id)
            return id
        }

        override suspend fun insertAll(items: List<VaultEntity>) {
            val added = items.map { item -> item.copy(id = nextId++) }
            rows.value = rows.value + added
        }

        override suspend fun update(item: VaultEntity) {
            rows.value = rows.value.map { current -> if (current.id == item.id) item else current }
        }

        override suspend fun delete(item: VaultEntity) {
            deleteById(item.id)
        }

        override suspend fun deleteById(id: Long) {
            rows.value = rows.value.filterNot { it.id == id }
        }

        override suspend fun deleteAll() {
            rows.value = emptyList()
        }
    }
}
