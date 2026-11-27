package com.example.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VaultDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: VaultDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.vaultDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insert and getById persist credential fields`() = runBlocking {
        val id = dao.insert(
            VaultEntity(
                title = "Email",
                username = "cheeku",
                encryptedPassword = "v1:encrypted",
                website = "https://mail.test",
                encryptedNotes = "v1:notes",
                isFavorite = true,
                createdAt = 100L,
                updatedAt = 200L
            )
        )

        val saved = dao.getById(id)
        assertNotNull(saved)
        assertEquals(id, saved!!.id)
        assertEquals("Email", saved.title)
        assertEquals("cheeku", saved.username)
        assertEquals("v1:encrypted", saved.encryptedPassword)
        assertEquals("https://mail.test", saved.website)
        assertEquals("v1:notes", saved.encryptedNotes)
        assertTrue(saved.isFavorite)
        assertEquals(100L, saved.createdAt)
        assertEquals(200L, saved.updatedAt)
    }

    @Test
    fun `insertAll and flow query return records ordered by most recently updated`() = runBlocking {
        dao.insertAll(
            listOf(
                VaultEntity(title = "Old", username = "old", encryptedPassword = "old", updatedAt = 10L),
                VaultEntity(title = "New", username = "new", encryptedPassword = "new", updatedAt = 30L),
                VaultEntity(title = "Middle", username = "middle", encryptedPassword = "middle", updatedAt = 20L)
            )
        )

        val records = dao.getAllVaultItems().first()
        assertEquals(listOf("New", "Middle", "Old"), records.map { it.title })
    }

    @Test
    fun `update and delete remove the expected records`() = runBlocking {
        val id = dao.insert(
            VaultEntity(title = "Before", username = "user", encryptedPassword = "cipher", updatedAt = 1L)
        )
        val original = dao.getById(id)!!

        dao.update(original.copy(title = "After", updatedAt = 2L))
        assertEquals("After", dao.getById(id)!!.title)

        val updated = dao.getById(id)!!
        dao.delete(updated)
        assertNull(dao.getById(id))
    }

    @Test
    fun `deleteById and deleteAll clear selected and remaining records`() = runBlocking {
        val first = dao.insert(VaultEntity(title = "First", username = "one", encryptedPassword = "a"))
        dao.insert(VaultEntity(title = "Second", username = "two", encryptedPassword = "b"))

        dao.deleteById(first)
        assertEquals(listOf("Second"), dao.getAllVaultItems().first().map { it.title })

        dao.deleteAll()
        assertTrue(dao.getAllVaultItems().first().isEmpty())
    }
}
