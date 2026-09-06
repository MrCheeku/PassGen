package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VaultUiState
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkBorderSubtle
import com.example.ui.theme.ElegantDarkGood
import com.example.ui.theme.ElegantDarkMedium
import com.example.ui.theme.ElegantDarkOnPrimary
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkPrimaryMedium
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceElevated
import com.example.ui.theme.ElegantDarkTextMuted
import com.example.ui.theme.ElegantDarkTextPrimary
import com.example.ui.theme.ElegantDarkWeak
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    state: VaultUiState,
    onEnableMasterLock: (String) -> Boolean,
    onChangeMasterLock: (String, String) -> Boolean,
    onDisableMasterLock: (String) -> Boolean,
    onThemeSelected: (String) -> Unit,
    onExportBackup: suspend (String) -> Result<String>,
    onImportBackup: suspend (String, String) -> Result<Int>,
    onClearAllData: () -> Unit,
    onCopyText: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var showSetupPinDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showDisablePinDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showClearAllConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Section: Master Lock Protection
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceElevated),
            border = BorderStroke(1.dp, ElegantDarkBorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElegantDarkSurface)
                                .border(1.dp, ElegantDarkBorderSubtle, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (state.isMasterLockEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (state.isMasterLockEnabled) ElegantDarkPrimary else ElegantDarkTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Vault Master Lock",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                ),
                                color = ElegantDarkTextPrimary
                            )
                            Text(
                                text = if (state.isMasterLockEnabled) "Protected with local Master PIN" else "Disabled (Vault accessible on device)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (state.isMasterLockEnabled) ElegantDarkGood else ElegantDarkTextMuted
                            )
                        }
                    }

                    Switch(
                        checked = state.isMasterLockEnabled,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                showSetupPinDialog = true
                            } else {
                                showDisablePinDialog = true
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ElegantDarkPrimary,
                            checkedTrackColor = ElegantDarkPrimaryMedium,
                            uncheckedThumbColor = ElegantDarkTextMuted,
                            uncheckedTrackColor = ElegantDarkSurface
                        ),
                        modifier = Modifier.testTag("settings_master_lock_switch")
                    )
                }

                if (state.isMasterLockEnabled) {
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = { showChangePinDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ElegantDarkBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ElegantDarkPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Password, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Change Master PIN", fontWeight = FontWeight.Medium)
                    }
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Because PassGen requires no online account, you can enable a local Master PIN so only you can view your vault credentials.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ElegantDarkTextMuted
                    )
                }
            }
        }

        // Section: Theme Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceElevated),
            border = BorderStroke(1.dp, ElegantDarkBorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.BrightnessMedium, contentDescription = null, tint = ElegantDarkPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Appearance & Theme",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = ElegantDarkTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                ThemeOptionItem(
                    title = "System Default",
                    selected = state.appTheme == "system",
                    onClick = { onThemeSelected("system") }
                )
                ThemeOptionItem(
                    title = "Elegant Dark",
                    selected = state.appTheme == "dark",
                    onClick = { onThemeSelected("dark") }
                )
                ThemeOptionItem(
                    title = "Minimal Light",
                    selected = state.appTheme == "light",
                    onClick = { onThemeSelected("light") }
                )
            }
        }

        // Section: Encrypted Backup & Restore
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceElevated),
            border = BorderStroke(1.dp, ElegantDarkBorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = ElegantDarkPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Encrypted Backup & Restore",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = ElegantDarkTextPrimary
                    )
                }

                Text(
                    text = "Backups are encrypted at rest with AES-256-GCM using your private export password. No plaintext credentials are ever exported.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElegantDarkTextMuted
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showExportDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantDarkSurface,
                            contentColor = ElegantDarkPrimary
                        ),
                        border = BorderStroke(1.dp, ElegantDarkBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, tint = ElegantDarkPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Export Vault", fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = { showImportDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantDarkSurface,
                            contentColor = ElegantDarkPrimary
                        ),
                        border = BorderStroke(1.dp, ElegantDarkBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = ElegantDarkPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Import Vault", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Section: Danger Zone (Clear Data)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkWeak.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, ElegantDarkWeak.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = ElegantDarkWeak)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Clear Vault Data",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = ElegantDarkWeak
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Permanently deletes all saved passwords and credentials on this device. This action cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElegantDarkTextMuted
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { showClearAllConfirm = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantDarkWeak,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clear_all_vault_button")
                ) {
                    Text(text = "Clear All Vault Data", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section: About & Developer Credit
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceElevated),
            border = BorderStroke(1.dp, ElegantDarkBorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = ElegantDarkPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "About PassGen",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = ElegantDarkTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "PassGen is a secure, modern, 100% offline password generator and encrypted personal vault. It requires no registration, no email login, and makes zero network requests. Your credentials stay strictly on your device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElegantDarkTextMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ElegantDarkSurface,
                    border = BorderStroke(1.dp, ElegantDarkBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "PassGen v1.0",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ElegantDarkTextPrimary
                            )
                            Text(
                                text = "Engineered by Mr.Cheeku",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = ElegantDarkPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ElegantDarkPrimary.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, ElegantDarkPrimary.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "OFFLINE VAULT",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ElegantDarkPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // --- Dialogs ---

    // Setup Master PIN Dialog
    if (showSetupPinDialog) {
        var pin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showSetupPinDialog = false },
            containerColor = ElegantDarkSurfaceElevated,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "Set Up Master PIN",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
                    color = ElegantDarkTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Create a 4-8 digit Master PIN. This PIN will be required to access your vault on this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ElegantDarkTextMuted
                    )
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it },
                        label = { Text("Enter PIN (4-8 digits)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantDarkPrimary,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedContainerColor = ElegantDarkSurface,
                            unfocusedContainerColor = ElegantDarkSurface,
                            focusedTextColor = ElegantDarkTextPrimary,
                            unfocusedTextColor = ElegantDarkTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { confirmPin = it },
                        label = { Text("Confirm PIN") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantDarkPrimary,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedContainerColor = ElegantDarkSurface,
                            unfocusedContainerColor = ElegantDarkSurface,
                            focusedTextColor = ElegantDarkTextPrimary,
                            unfocusedTextColor = ElegantDarkTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error != null) {
                        Text(text = error ?: "", color = ElegantDarkWeak, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pin.length < 4) {
                            error = "PIN must be at least 4 digits"
                            return@Button
                        }
                        if (pin != confirmPin) {
                            error = "PINs do not match"
                            return@Button
                        }
                        val success = onEnableMasterLock(pin)
                        if (success) {
                            showSetupPinDialog = false
                        } else {
                            error = "Failed to save PIN"
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantDarkPrimary,
                        contentColor = ElegantDarkOnPrimary
                    )
                ) {
                    Text("Enable PIN", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showSetupPinDialog = false },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantDarkTextMuted)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Change Master PIN Dialog
    if (showChangePinDialog) {
        var oldPin by remember { mutableStateOf("") }
        var newPin by remember { mutableStateOf("") }
        var confirmNewPin by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            containerColor = ElegantDarkSurfaceElevated,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "Change Master PIN",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
                    color = ElegantDarkTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = oldPin,
                        onValueChange = { oldPin = it },
                        label = { Text("Current PIN") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantDarkPrimary,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedContainerColor = ElegantDarkSurface,
                            unfocusedContainerColor = ElegantDarkSurface,
                            focusedTextColor = ElegantDarkTextPrimary,
                            unfocusedTextColor = ElegantDarkTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { newPin = it },
                        label = { Text("New PIN (4-8 digits)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantDarkPrimary,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedContainerColor = ElegantDarkSurface,
                            unfocusedContainerColor = ElegantDarkSurface,
                            focusedTextColor = ElegantDarkTextPrimary,
                            unfocusedTextColor = ElegantDarkTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmNewPin,
                        onValueChange = { confirmNewPin = it },
                        label = { Text("Confirm New PIN") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantDarkPrimary,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedContainerColor = ElegantDarkSurface,
                            unfocusedContainerColor = ElegantDarkSurface,
                            focusedTextColor = ElegantDarkTextPrimary,
                            unfocusedTextColor = ElegantDarkTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error != null) {
                        Text(text = error ?: "", color = ElegantDarkWeak, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length < 4) {
                            error = "New PIN must be at least 4 digits"
                            return@Button
                        }
                        if (newPin != confirmNewPin) {
                            error = "New PINs do not match"
                            return@Button
                        }
                        val success = onChangeMasterLock(oldPin, newPin)
                        if (success) {
                            showChangePinDialog = false
                        } else {
                            error = "Incorrect current PIN"
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantDarkPrimary,
                        contentColor = ElegantDarkOnPrimary
                    )
                ) {
                    Text("Update PIN", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showChangePinDialog = false },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantDarkTextMuted)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Disable Master PIN Dialog
    if (showDisablePinDialog) {
        var pin by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showDisablePinDialog = false },
            containerColor = ElegantDarkSurfaceElevated,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "Disable Master Lock",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
                    color = ElegantDarkTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Enter your current Master PIN to disable lock protection. Your vault will be accessible directly on this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ElegantDarkTextMuted
                    )
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it },
                        label = { Text("Master PIN") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantDarkPrimary,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedContainerColor = ElegantDarkSurface,
                            unfocusedContainerColor = ElegantDarkSurface,
                            focusedTextColor = ElegantDarkTextPrimary,
                            unfocusedTextColor = ElegantDarkTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error != null) {
                        Text(text = error ?: "", color = ElegantDarkWeak, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = onDisableMasterLock(pin)
                        if (success) {
                            showDisablePinDialog = false
                        } else {
                            error = "Incorrect PIN"
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantDarkWeak,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Disable", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDisablePinDialog = false },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantDarkTextMuted)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Export Backup Dialog
    if (showExportDialog) {
        var passphrase by remember { mutableStateOf("") }
        var exportedPayload by remember { mutableStateOf<String?>(null) }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            containerColor = ElegantDarkSurfaceElevated,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "Export Encrypted Backup",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
                    color = ElegantDarkTextPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (exportedPayload == null) {
                        Text(
                            text = "Set a passphrase to encrypt your backup with AES-256-GCM. Keep this passphrase secure, as it cannot be recovered if lost.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantDarkTextMuted
                        )
                        OutlinedTextField(
                            value = passphrase,
                            onValueChange = { passphrase = it },
                            label = { Text("Encryption Passphrase") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElegantDarkPrimary,
                                unfocusedBorderColor = ElegantDarkBorder,
                                focusedContainerColor = ElegantDarkSurface,
                                unfocusedContainerColor = ElegantDarkSurface,
                                focusedTextColor = ElegantDarkTextPrimary,
                                unfocusedTextColor = ElegantDarkTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (error != null) {
                            Text(text = error ?: "", color = ElegantDarkWeak, style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        Text(
                            text = "Backup generated! Warning: An exported backup contains your credentials and must be kept strictly private.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantDarkMedium
                        )
                        OutlinedTextField(
                            value = exportedPayload ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Encrypted Payload") },
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElegantDarkPrimary,
                                unfocusedBorderColor = ElegantDarkBorder,
                                focusedContainerColor = ElegantDarkSurface,
                                unfocusedContainerColor = ElegantDarkSurface,
                                focusedTextColor = ElegantDarkTextPrimary,
                                unfocusedTextColor = ElegantDarkTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                if (exportedPayload == null) {
                    Button(
                        onClick = {
                            if (passphrase.isBlank()) {
                                error = "Passphrase cannot be empty"
                                return@Button
                            }
                            coroutineScope.launch {
                                val result = onExportBackup(passphrase)
                                result.onSuccess { payload ->
                                    exportedPayload = payload
                                }.onFailure {
                                    error = "Export failed: ${it.message}"
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantDarkPrimary,
                            contentColor = ElegantDarkOnPrimary
                        )
                    ) {
                        Text("Generate Backup", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            onCopyText(exportedPayload ?: "", "Encrypted Backup")
                            showExportDialog = false
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantDarkPrimary,
                            contentColor = ElegantDarkOnPrimary
                        )
                    ) {
                        Text("Copy to Clipboard", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showExportDialog = false },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantDarkTextMuted)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Import Backup Dialog
    if (showImportDialog) {
        var payload by remember { mutableStateOf("") }
        var passphrase by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = ElegantDarkSurfaceElevated,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "Import Encrypted Backup",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
                    color = ElegantDarkTextPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Paste your encrypted PassGen backup JSON and enter the passphrase used during export.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ElegantDarkTextMuted
                    )
                    OutlinedTextField(
                        value = payload,
                        onValueChange = { payload = it },
                        label = { Text("Encrypted Backup JSON") },
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantDarkPrimary,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedContainerColor = ElegantDarkSurface,
                            unfocusedContainerColor = ElegantDarkSurface,
                            focusedTextColor = ElegantDarkTextPrimary,
                            unfocusedTextColor = ElegantDarkTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = passphrase,
                        onValueChange = { passphrase = it },
                        label = { Text("Decryption Passphrase") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElegantDarkPrimary,
                            unfocusedBorderColor = ElegantDarkBorder,
                            focusedContainerColor = ElegantDarkSurface,
                            unfocusedContainerColor = ElegantDarkSurface,
                            focusedTextColor = ElegantDarkTextPrimary,
                            unfocusedTextColor = ElegantDarkTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error != null) {
                        Text(text = error ?: "", color = ElegantDarkWeak, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (payload.isBlank() || passphrase.isBlank()) {
                            error = "Both payload and passphrase are required"
                            return@Button
                        }
                        coroutineScope.launch {
                            val result = onImportBackup(payload, passphrase)
                            result.onSuccess {
                                showImportDialog = false
                            }.onFailure {
                                error = "Import failed: incorrect passphrase or corrupt data"
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantDarkPrimary,
                        contentColor = ElegantDarkOnPrimary
                    )
                ) {
                    Text("Import", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showImportDialog = false },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantDarkTextMuted)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear All Confirmation Dialog
    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            containerColor = ElegantDarkSurfaceElevated,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Clear All Vault Data?",
                    fontWeight = FontWeight.Bold,
                    color = ElegantDarkWeak
                )
            },
            text = {
                Text(
                    text = "This will permanently delete ALL saved passwords, credentials, and settings stored in PassGen on this device. This action is irreversible.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ElegantDarkTextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showClearAllConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantDarkWeak,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("confirm_clear_all_button")
                ) {
                    Text("Yes, Permanently Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearAllConfirm = false },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantDarkTextMuted)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ThemeOptionItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = ElegantDarkPrimary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (selected) ElegantDarkPrimary else ElegantDarkTextPrimary
        )
    }
}
