package com.example.data

import com.example.security.VaultSecurityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class VaultRepository(
    private val vaultDao: VaultDao,
    private val securityManager: VaultSecurityManager
) {

    val allVaultItems: Flow<List<VaultItem>> = vaultDao.getAllVaultItems().map { entities ->
        entities.map { entity ->
            VaultItem(
                id = entity.id,
                title = entity.title,
                username = entity.username,
                password = securityManager.decryptAtRest(entity.encryptedPassword),
                website = entity.website,
                notes = securityManager.decryptAtRest(entity.encryptedNotes),
                isFavorite = entity.isFavorite,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }
    }

    suspend fun saveItem(item: VaultItem): Long = withContext(Dispatchers.IO) {
        val entity = VaultEntity(
            id = item.id,
            title = item.title.trim(),
            username = item.username.trim(),
            encryptedPassword = securityManager.encryptAtRest(item.password),
            website = item.website.trim(),
            encryptedNotes = securityManager.encryptAtRest(item.notes.trim()),
            isFavorite = item.isFavorite,
            createdAt = if (item.createdAt == 0L) System.currentTimeMillis() else item.createdAt,
            updatedAt = System.currentTimeMillis()
        )
        if (item.id == 0L) {
            vaultDao.insert(entity)
        } else {
            vaultDao.update(entity)
            item.id
        }
    }

    suspend fun toggleFavorite(item: VaultItem) = withContext(Dispatchers.IO) {
        val updated = item.copy(isFavorite = !item.isFavorite, updatedAt = System.currentTimeMillis())
        saveItem(updated)
    }

    suspend fun deleteItem(item: VaultItem) = withContext(Dispatchers.IO) {
        vaultDao.deleteById(item.id)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        vaultDao.deleteAll()
    }

    suspend fun exportEncryptedBackup(items: List<VaultItem>, exportPassword: String): String = withContext(Dispatchers.Default) {
        val jsonArray = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("title", item.title)
                put("username", item.username)
                put("password", item.password)
                put("website", item.website)
                put("notes", item.notes)
                put("isFavorite", item.isFavorite)
                put("createdAt", item.createdAt)
                put("updatedAt", item.updatedAt)
            }
            jsonArray.put(obj)
        }
        val rawJson = jsonArray.toString()
        securityManager.encryptBackupPayload(rawJson, exportPassword)
    }

    suspend fun importEncryptedBackup(encryptedPayload: String, exportPassword: String): Int = withContext(Dispatchers.IO) {
        val decryptedJson = securityManager.decryptBackupPayload(encryptedPayload, exportPassword)
        val jsonArray = JSONArray(decryptedJson)
        val entitiesToInsert = mutableListOf<VaultEntity>()

        val now = System.currentTimeMillis()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val title = obj.optString("title", "Imported Item")
            val username = obj.optString("username", "")
            val rawPassword = obj.optString("password", "")
            val website = obj.optString("website", "")
            val notes = obj.optString("notes", "")
            val isFavorite = obj.optBoolean("isFavorite", false)
            val createdAt = obj.optLong("createdAt", now)
            val updatedAt = obj.optLong("updatedAt", now)

            entitiesToInsert.add(
                VaultEntity(
                    id = 0,
                    title = title,
                    username = username,
                    encryptedPassword = securityManager.encryptAtRest(rawPassword),
                    website = website,
                    encryptedNotes = securityManager.encryptAtRest(notes),
                    isFavorite = isFavorite,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )
            )
        }

        if (entitiesToInsert.isNotEmpty()) {
            vaultDao.insertAll(entitiesToInsert)
        }
        entitiesToInsert.size
    }
}
