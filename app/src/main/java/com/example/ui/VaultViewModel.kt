package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.VaultItem
import com.example.data.VaultRepository
import com.example.security.PasswordGenerator
import com.example.security.PasswordHealthAnalyzer
import com.example.security.PasswordHealthReport
import com.example.security.VaultSecurityManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SortOption(val displayName: String) {
    RECENTLY_UPDATED("Recently Updated"),
    RECENTLY_ADDED("Recently Added"),
    TITLE_AZ("Title (A-Z)"),
    TITLE_ZA("Title (Z-A)"),
    STRENGTH_WEAKEST("Weakest First"),
    STRENGTH_STRONGEST("Strongest First"),
    FAVORITES_FIRST("Favorites First")
}

enum class VaultFilter(val label: String) {
    ALL("All"),
    FAVORITES("Pinned"),
    NEEDS_ATTENTION("Needs Attention"),
    WEAK("Weak"),
    SHORT("Short (<10)"),
    REUSED("Reused"),
    OLD("Old (>90d)")
}

data class VaultUiState(
    val items: List<VaultItem> = emptyList(),
    val filteredItems: List<VaultItem> = emptyList(),
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.RECENTLY_UPDATED,
    val activeFilter: VaultFilter = VaultFilter.ALL,
    val healthReport: PasswordHealthReport = PasswordHealthAnalyzer.analyze(emptyList()),
    val isMasterLockEnabled: Boolean = false,
    val isVaultUnlocked: Boolean = false,
    val appTheme: String = "system", // "system", "dark", "light"
    val statusMessage: String? = null,
    val editingItem: VaultItem? = null,
    val isAddEditSheetOpen: Boolean = false,
    val showClearAllDialog: Boolean = false,
    val showExportDialog: Boolean = false,
    val showImportDialog: Boolean = false,
    val showMasterLockSetupDialog: Boolean = false,
    val showMasterLockChangeDialog: Boolean = false,
    val generatedPassword: String = "",
    val generatorOptions: PasswordGenerator.GeneratorOptions = PasswordGenerator.GeneratorOptions()
)

class VaultViewModel(application: Application) : AndroidViewModel(application) {

    private val securityManager = VaultSecurityManager(application)
    private val database = AppDatabase.getInstance(application)
    private val repository = VaultRepository(database.vaultDao(), securityManager)
    private val clipboardManager =
        application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    private val _uiState = MutableStateFlow(
        VaultUiState(
            isMasterLockEnabled = securityManager.isMasterLockEnabled(),
            isVaultUnlocked = !securityManager.isMasterLockEnabled(),
            appTheme = securityManager.getAppTheme(),
            generatedPassword = PasswordGenerator.generate(PasswordGenerator.GeneratorOptions())
        )
    )
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    private var clipboardClearJob: Job? = null

    init {
        // Collect vault items from repository reactively
        viewModelScope.launch {
            repository.allVaultItems.collect { items ->
                val health = PasswordHealthAnalyzer.analyze(items)
                _uiState.update { current ->
                    val filtered = applyFilterAndSort(
                        items = items,
                        query = current.searchQuery,
                        filter = current.activeFilter,
                        sort = current.sortOption
                    )
                    current.copy(
                        items = items,
                        filteredItems = filtered,
                        healthReport = health
                    )
                }
            }
        }
    }

    // --- Master Lock Authentication ---

    fun unlockVault(pin: String): Boolean {
        val success = securityManager.verifyMasterLock(pin)
        if (success) {
            _uiState.update { it.copy(isVaultUnlocked = true) }
        }
        return success
    }

    fun lockVault() {
        if (securityManager.isMasterLockEnabled()) {
            _uiState.update { it.copy(isVaultUnlocked = false) }
        }
    }

    fun enableMasterLock(pin: String): Boolean {
        val success = securityManager.setupMasterLock(pin)
        if (success) {
            _uiState.update {
                it.copy(
                    isMasterLockEnabled = true,
                    isVaultUnlocked = true,
                    showMasterLockSetupDialog = false,
                    statusMessage = "Master PIN protection enabled"
                )
            }
        }
        return success
    }

    fun changeMasterLock(oldPin: String, newPin: String): Boolean {
        val success = securityManager.changeMasterLock(oldPin, newPin)
        if (success) {
            _uiState.update {
                it.copy(
                    showMasterLockChangeDialog = false,
                    statusMessage = "Master PIN successfully changed"
                )
            }
        }
        return success
    }

    fun disableMasterLock(pin: String): Boolean {
        val success = securityManager.disableMasterLock(pin)
        if (success) {
            _uiState.update {
                it.copy(
                    isMasterLockEnabled = false,
                    isVaultUnlocked = true,
                    statusMessage = "Master lock disabled. Vault is open on this device."
                )
            }
        }
        return success
    }

    // --- Search, Filter & Sort ---

    fun setSearchQuery(query: String) {
        _uiState.update { current ->
            val filtered = applyFilterAndSort(current.items, query, current.activeFilter, current.sortOption)
            current.copy(searchQuery = query, filteredItems = filtered)
        }
    }

    fun setActiveFilter(filter: VaultFilter) {
        _uiState.update { current ->
            val filtered = applyFilterAndSort(current.items, current.searchQuery, filter, current.sortOption)
            current.copy(activeFilter = filter, filteredItems = filtered)
        }
    }

    fun setSortOption(sort: SortOption) {
        _uiState.update { current ->
            val filtered = applyFilterAndSort(current.items, current.searchQuery, current.activeFilter, sort)
            current.copy(sortOption = sort, filteredItems = filtered)
        }
    }

    private fun applyFilterAndSort(
        items: List<VaultItem>,
        query: String,
        filter: VaultFilter,
        sort: SortOption
    ): List<VaultItem> {
        val q = query.trim().lowercase()

        // 1. Search filter
        val searchMatched = if (q.isEmpty()) {
            items
        } else {
            items.filter {
                it.title.lowercase().contains(q) ||
                    it.username.lowercase().contains(q) ||
                    it.website.lowercase().contains(q) ||
                    it.notes.lowercase().contains(q)
            }
        }

        // 2. Health & category filter
        // Calculate reused passwords to accurately filter
        val passwordCounts = mutableMapOf<String, Int>()
        for (item in items) {
            passwordCounts[item.password] = passwordCounts.getOrDefault(item.password, 0) + 1
        }

        val categoryFiltered = when (filter) {
            VaultFilter.ALL -> searchMatched
            VaultFilter.FAVORITES -> searchMatched.filter { it.isFavorite }
            VaultFilter.NEEDS_ATTENTION -> searchMatched.filter {
                it.isWeak || it.isShort || it.isOld || (passwordCounts[it.password] ?: 0) > 1
            }
            VaultFilter.WEAK -> searchMatched.filter { it.isWeak }
            VaultFilter.SHORT -> searchMatched.filter { it.isShort }
            VaultFilter.REUSED -> searchMatched.filter { (passwordCounts[it.password] ?: 0) > 1 }
            VaultFilter.OLD -> searchMatched.filter { it.isOld }
        }

        // 3. Sort
        return when (sort) {
            SortOption.RECENTLY_UPDATED -> categoryFiltered.sortedByDescending { it.updatedAt }
            SortOption.RECENTLY_ADDED -> categoryFiltered.sortedByDescending { it.createdAt }
            SortOption.TITLE_AZ -> categoryFiltered.sortedBy { it.title.lowercase() }
            SortOption.TITLE_ZA -> categoryFiltered.sortedByDescending { it.title.lowercase() }
            SortOption.STRENGTH_WEAKEST -> categoryFiltered.sortedBy { it.strength.score }
            SortOption.STRENGTH_STRONGEST -> categoryFiltered.sortedByDescending { it.strength.score }
            SortOption.FAVORITES_FIRST -> categoryFiltered.sortedWith(
                compareByDescending<VaultItem> { it.isFavorite }.thenByDescending { it.updatedAt }
            )
        }
    }

    // --- Password Generator ---

    fun updateGeneratorOptions(options: PasswordGenerator.GeneratorOptions) {
        val newPassword = PasswordGenerator.generate(options)
        _uiState.update { it.copy(generatorOptions = options, generatedPassword = newPassword) }
    }

    fun regeneratePassword() {
        val newPassword = PasswordGenerator.generate(_uiState.value.generatorOptions)
        _uiState.update { it.copy(generatedPassword = newPassword) }
    }

    // --- Vault CRUD Operations ---

    fun openAddDialog(presetPassword: String? = null) {
        val initialItem = VaultItem(
            title = "",
            username = "",
            password = presetPassword ?: _uiState.value.generatedPassword,
            website = "",
            notes = "",
            isFavorite = false
        )
        _uiState.update { it.copy(editingItem = initialItem, isAddEditSheetOpen = true) }
    }

    fun openEditDialog(item: VaultItem) {
        _uiState.update { it.copy(editingItem = item, isAddEditSheetOpen = true) }
    }

    fun closeAddEditDialog() {
        _uiState.update { it.copy(isAddEditSheetOpen = false, editingItem = null) }
    }

    fun saveCredential(
        id: Long,
        title: String,
        username: String,
        password: String,
        website: String,
        notes: String,
        isFavorite: Boolean
    ) {
        if (title.isBlank()) {
            _uiState.update { it.copy(statusMessage = "Title cannot be empty") }
            return
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(statusMessage = "Password cannot be empty") }
            return
        }

        viewModelScope.launch {
            val item = VaultItem(
                id = id,
                title = title,
                username = username,
                password = password,
                website = website,
                notes = notes,
                isFavorite = isFavorite,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveItem(item)
            _uiState.update {
                it.copy(
                    isAddEditSheetOpen = false,
                    editingItem = null,
                    statusMessage = if (id == 0L) "Credential saved to vault" else "Credential updated"
                )
            }
        }
    }

    fun toggleFavorite(item: VaultItem) {
        viewModelScope.launch {
            repository.toggleFavorite(item)
        }
    }

    fun deleteItem(item: VaultItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
            _uiState.update { it.copy(statusMessage = "\"${item.title}\" deleted") }
        }
    }

    fun clearAllVaultData() {
        viewModelScope.launch {
            repository.clearAll()
            _uiState.update {
                it.copy(
                    showClearAllDialog = false,
                    statusMessage = "All vault data permanently cleared"
                )
            }
        }
    }

    // --- Clipboard Security ---

    fun copyToClipboard(text: String, label: String, isPassword: Boolean = false) {
        if (text.isEmpty()) return

        val clip = ClipData.newPlainText(label, text)
        if (isPassword && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            clip.description.extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
        }
        clipboardManager.setPrimaryClip(clip)

        val message = if (isPassword) {
            "Password copied! Clipboard will auto-clear in 30s."
        } else {
            "$label copied to clipboard"
        }
        _uiState.update { it.copy(statusMessage = message) }

        if (isPassword) {
            clipboardClearJob?.cancel()
            clipboardClearJob = viewModelScope.launch {
                delay(30000)
                try {
                    val currentClip = clipboardManager.primaryClip
                    if (currentClip != null && currentClip.itemCount > 0) {
                        val currentText = currentClip.getItemAt(0).text?.toString()
                        if (currentText == text) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                clipboardManager.clearPrimaryClip()
                            } else {
                                clipboardManager.setPrimaryClip(ClipData.newPlainText("", ""))
                            }
                            _uiState.update { it.copy(statusMessage = "Clipboard cleared for security") }
                        }
                    }
                } catch (_: Exception) {
                    // Ignore background clipboard access restrictions
                }
            }
        }
    }

    // --- Dialogs & Settings ---

    fun setShowClearAllDialog(show: Boolean) {
        _uiState.update { it.copy(showClearAllDialog = show) }
    }

    fun setShowExportDialog(show: Boolean) {
        _uiState.update { it.copy(showExportDialog = show) }
    }

    fun setShowImportDialog(show: Boolean) {
        _uiState.update { it.copy(showImportDialog = show) }
    }

    fun setShowMasterLockSetupDialog(show: Boolean) {
        _uiState.update { it.copy(showMasterLockSetupDialog = show) }
    }

    fun setShowMasterLockChangeDialog(show: Boolean) {
        _uiState.update { it.copy(showMasterLockChangeDialog = show) }
    }

    fun setAppTheme(theme: String) {
        securityManager.setAppTheme(theme)
        _uiState.update { it.copy(appTheme = theme) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    // --- Encrypted Backup Export & Import ---

    suspend fun exportVault(passphrase: String): Result<String> {
        return try {
            val encryptedBackup = repository.exportEncryptedBackup(_uiState.value.items, passphrase)
            Result.success(encryptedBackup)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importVault(payload: String, passphrase: String): Result<Int> {
        return try {
            val count = repository.importEncryptedBackup(payload, passphrase)
            _uiState.update { it.copy(showImportDialog = false, statusMessage = "Imported $count credentials successfully") }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
