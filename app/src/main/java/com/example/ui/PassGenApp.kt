package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddEditPasswordDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GeneratorScreen
import com.example.ui.screens.MasterLockScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.VaultListScreen
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.PassGenTheme

enum class PassGenTab(val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    VAULT("Vault", Icons.Filled.Lock, Icons.Outlined.Lock),
    GENERATOR("Generator", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun PassGenApp(
    viewModel: VaultViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var currentTab by remember { mutableStateOf(PassGenTab.DASHBOARD) }
    var isSplashActive by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Display reactive snackbar for copy, save, etc.
    LaunchedEffect(state.statusMessage) {
        val msg = state.statusMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    PassGenTheme(appThemeSetting = state.appTheme) {
        if (isSplashActive) {
            SplashScreen(
                onSplashFinished = { isSplashActive = false }
            )
        } else if (state.isMasterLockEnabled && !state.isVaultUnlocked) {
            MasterLockScreen(
                onUnlockAttempt = { pin -> viewModel.unlockVault(pin) }
            )
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        tonalElevation = 0.dp,
                        modifier = Modifier.drawBehind {
                            drawLine(
                                color = ElegantDarkBorder,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    ) {
                        for (tab in PassGenTab.entries) {
                            val isSelected = currentTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = { Text(text = tab.title) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.outline,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                ),
                                modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                },
                floatingActionButton = {
                    // Show FAB on Dashboard and Vault screens
                    if (currentTab == PassGenTab.DASHBOARD || currentTab == PassGenTab.VAULT) {
                        FloatingActionButton(
                            onClick = { viewModel.openAddDialog() },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.testTag("main_add_fab")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Password")
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "TabTransition"
                    ) { tab ->
                        when (tab) {
                            PassGenTab.DASHBOARD -> DashboardScreen(
                                state = state,
                                onNavigateToVault = { filter ->
                                    viewModel.setActiveFilter(filter)
                                    currentTab = PassGenTab.VAULT
                                },
                                onNavigateToGenerator = {
                                    currentTab = PassGenTab.GENERATOR
                                },
                                onAddPassword = { viewModel.openAddDialog() },
                                onCopyPassword = { pass -> viewModel.copyToClipboard(pass, "Password", isPassword = true) },
                                onCopyUsername = { user -> viewModel.copyToClipboard(user, "Username") },
                                onToggleFavorite = { item -> viewModel.toggleFavorite(item) },
                                onEditItem = { item -> viewModel.openEditDialog(item) },
                                onDeleteItem = { item -> viewModel.deleteItem(item) }
                            )

                            PassGenTab.VAULT -> VaultListScreen(
                                state = state,
                                onSearchChange = { query -> viewModel.setSearchQuery(query) },
                                onFilterChange = { filter -> viewModel.setActiveFilter(filter) },
                                onSortChange = { sort -> viewModel.setSortOption(sort) },
                                onCopyPassword = { pass -> viewModel.copyToClipboard(pass, "Password", isPassword = true) },
                                onCopyUsername = { user -> viewModel.copyToClipboard(user, "Username") },
                                onToggleFavorite = { item -> viewModel.toggleFavorite(item) },
                                onEditItem = { item -> viewModel.openEditDialog(item) },
                                onDeleteItem = { item -> viewModel.deleteItem(item) }
                            )

                            PassGenTab.GENERATOR -> GeneratorScreen(
                                generatedPassword = state.generatedPassword,
                                options = state.generatorOptions,
                                onOptionsChanged = { opts -> viewModel.updateGeneratorOptions(opts) },
                                onRegenerate = { viewModel.regeneratePassword() },
                                onCopy = { pass -> viewModel.copyToClipboard(pass, "Password", isPassword = true) },
                                onSaveToVault = { pass ->
                                    viewModel.openAddDialog(presetPassword = pass)
                                }
                            )

                            PassGenTab.SETTINGS -> SettingsScreen(
                                state = state,
                                onEnableMasterLock = { pin -> viewModel.enableMasterLock(pin) },
                                onChangeMasterLock = { oldPin, newPin -> viewModel.changeMasterLock(oldPin, newPin) },
                                onDisableMasterLock = { pin -> viewModel.disableMasterLock(pin) },
                                onThemeSelected = { theme -> viewModel.setAppTheme(theme) },
                                onExportBackup = { passphrase -> viewModel.exportVault(passphrase) },
                                onImportBackup = { payload, passphrase -> viewModel.importVault(payload, passphrase) },
                                onClearAllData = { viewModel.clearAllVaultData() },
                                onCopyText = { text, label -> viewModel.copyToClipboard(text, label) }
                            )
                        }
                    }

                    // Add / Edit Credential Dialog
                    if (state.isAddEditSheetOpen) {
                        AddEditPasswordDialog(
                            item = state.editingItem,
                            onDismiss = { viewModel.closeAddEditDialog() },
                            onSave = { id, title, username, password, website, notes, isFavorite ->
                                viewModel.saveCredential(id, title, username, password, website, notes, isFavorite)
                            }
                        )
                    }
                }
            }
        }
    }
}
