package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VaultItem
import com.example.security.PasswordGenerator
import com.example.security.PasswordStrengthCalculator
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkBorderSubtle
import com.example.ui.theme.ElegantDarkOnPrimary
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceElevated
import com.example.ui.theme.ElegantDarkTextMuted
import com.example.ui.theme.ElegantDarkTextPrimary
import com.example.ui.theme.ElegantDarkWeak

@Composable
fun AddEditPasswordDialog(
    item: VaultItem?,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        title: String,
        username: String,
        password: String,
        website: String,
        notes: String,
        isFavorite: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditing = (item?.id ?: 0L) != 0L

    var title by remember(item) { mutableStateOf(item?.title ?: "") }
    var username by remember(item) { mutableStateOf(item?.username ?: "") }
    var password by remember(item) { mutableStateOf(item?.password ?: "") }
    var website by remember(item) { mutableStateOf(item?.website ?: "") }
    var notes by remember(item) { mutableStateOf(item?.notes ?: "") }
    var isFavorite by remember(item) { mutableStateOf(item?.isFavorite ?: false) }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    val strength = remember(password) { PasswordStrengthCalculator.calculate(password) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("add_edit_dialog"),
        shape = RoundedCornerShape(24.dp),
        containerColor = ElegantDarkSurfaceElevated,
        title = {
            Text(
                text = if (isEditing) "Edit Credential" else "New Saved Password",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                ),
                color = ElegantDarkTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text("Title * (e.g. GitHub, Netflix)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Title, contentDescription = null, tint = ElegantDarkPrimary)
                    },
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("Title is required", color = ElegantDarkWeak) }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_title"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElegantDarkPrimary,
                        unfocusedBorderColor = ElegantDarkBorder,
                        focusedLabelColor = ElegantDarkPrimary,
                        unfocusedLabelColor = ElegantDarkTextMuted,
                        focusedContainerColor = ElegantDarkSurface,
                        unfocusedContainerColor = ElegantDarkSurface,
                        focusedTextColor = ElegantDarkTextPrimary,
                        unfocusedTextColor = ElegantDarkTextPrimary
                    )
                )

                // Username / Email Field
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username or Email") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = ElegantDarkPrimary)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_username"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElegantDarkPrimary,
                        unfocusedBorderColor = ElegantDarkBorder,
                        focusedLabelColor = ElegantDarkPrimary,
                        unfocusedLabelColor = ElegantDarkTextMuted,
                        focusedContainerColor = ElegantDarkSurface,
                        unfocusedContainerColor = ElegantDarkSurface,
                        focusedTextColor = ElegantDarkTextPrimary,
                        unfocusedTextColor = ElegantDarkTextPrimary
                    )
                )

                // Password Field + Inline Generate Button
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (it.isNotBlank()) passwordError = false
                    },
                    label = { Text("Password *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = ElegantDarkPrimary)
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Quick auto-generate assist button
                            IconButton(
                                onClick = {
                                    password = PasswordGenerator.generate(
                                        PasswordGenerator.GeneratorOptions(length = 18)
                                    )
                                    isPasswordVisible = true
                                    passwordError = false
                                },
                                modifier = Modifier.testTag("dialog_quick_generate")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Generate Strong Password",
                                    tint = ElegantDarkPrimary
                                )
                            }
                            // Visibility toggle
                            IconButton(
                                onClick = { isPasswordVisible = !isPasswordVisible },
                                modifier = Modifier.testTag("dialog_toggle_pass_visibility")
                            ) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isPasswordVisible) "Hide" else "Show",
                                    tint = ElegantDarkTextMuted
                                )
                            }
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = passwordError,
                    supportingText = if (passwordError) {
                        { Text("Password is required", color = ElegantDarkWeak) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_password"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElegantDarkPrimary,
                        unfocusedBorderColor = ElegantDarkBorder,
                        focusedLabelColor = ElegantDarkPrimary,
                        unfocusedLabelColor = ElegantDarkTextMuted,
                        focusedContainerColor = ElegantDarkSurface,
                        unfocusedContainerColor = ElegantDarkSurface,
                        focusedTextColor = ElegantDarkTextPrimary,
                        unfocusedTextColor = ElegantDarkTextPrimary
                    )
                )

                // Strength bar preview
                if (password.isNotEmpty()) {
                    PasswordStrengthBar(strength = strength)
                }

                // Website / App
                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = { Text("Website or App (e.g. github.com)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = ElegantDarkPrimary)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_website"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElegantDarkPrimary,
                        unfocusedBorderColor = ElegantDarkBorder,
                        focusedLabelColor = ElegantDarkPrimary,
                        unfocusedLabelColor = ElegantDarkTextMuted,
                        focusedContainerColor = ElegantDarkSurface,
                        unfocusedContainerColor = ElegantDarkSurface,
                        focusedTextColor = ElegantDarkTextPrimary,
                        unfocusedTextColor = ElegantDarkTextPrimary
                    )
                )

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Encrypted locally)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Notes, contentDescription = null, tint = ElegantDarkPrimary)
                    },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_notes"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElegantDarkPrimary,
                        unfocusedBorderColor = ElegantDarkBorder,
                        focusedLabelColor = ElegantDarkPrimary,
                        unfocusedLabelColor = ElegantDarkTextMuted,
                        focusedContainerColor = ElegantDarkSurface,
                        unfocusedContainerColor = ElegantDarkSurface,
                        focusedTextColor = ElegantDarkTextPrimary,
                        unfocusedTextColor = ElegantDarkTextPrimary
                    )
                )

                // Pin / Favorite Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Checkbox(
                        checked = isFavorite,
                        onCheckedChange = { isFavorite = it },
                        colors = CheckboxDefaults.colors(checkedColor = ElegantDarkPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pin to favorites / top of list",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ElegantDarkTextPrimary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedTitle = title.trim()
                    val trimmedPass = password.trim()
                    if (trimmedTitle.isEmpty()) {
                        titleError = true
                        return@Button
                    }
                    if (trimmedPass.isEmpty()) {
                        passwordError = true
                        return@Button
                    }
                    onSave(
                        item?.id ?: 0L,
                        trimmedTitle,
                        username.trim(),
                        trimmedPass,
                        website.trim(),
                        notes.trim(),
                        isFavorite
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElegantDarkPrimary,
                    contentColor = ElegantDarkOnPrimary
                ),
                modifier = Modifier.testTag("save_credential_button")
            ) {
                Text(
                    text = if (isEditing) "Update" else "Save",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ElegantDarkBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ElegantDarkTextMuted
                ),
                modifier = Modifier.testTag("cancel_credential_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
