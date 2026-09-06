package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.PasswordGenerator
import com.example.security.PasswordStrengthCalculator
import com.example.ui.components.PasswordStrengthBar
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkBorderSubtle
import com.example.ui.theme.ElegantDarkOnPrimary
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkPrimaryMedium
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceElevated
import com.example.ui.theme.ElegantDarkTextMuted
import com.example.ui.theme.ElegantDarkTextPrimary
import kotlinx.coroutines.launch

@Composable
fun GeneratorScreen(
    generatedPassword: String,
    options: PasswordGenerator.GeneratorOptions,
    onOptionsChanged: (PasswordGenerator.GeneratorOptions) -> Unit,
    onRegenerate: () -> Unit,
    onCopy: (String) -> Unit,
    onSaveToVault: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }
    val strength = remember(generatedPassword) {
        PasswordStrengthCalculator.calculate(generatedPassword)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("generator_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Large Password Display Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceElevated),
            border = BorderStroke(1.dp, ElegantDarkBorderSubtle),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Generated Password",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    ),
                    color = ElegantDarkTextMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Monospace preview box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ElegantDarkSurface)
                        .border(1.dp, ElegantDarkBorderSubtle, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = generatedPassword,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (generatedPassword.length > 24) 18.sp else 22.sp,
                            letterSpacing = 1.sp
                        ),
                        color = ElegantDarkPrimary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Strength Indicator Bar
                PasswordStrengthBar(strength = strength)

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons: Regenerate, Copy, Save to Vault
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                rotation.snapTo(0f)
                                rotation.animateTo(360f, tween(400))
                            }
                            onRegenerate()
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ElegantDarkSurface)
                            .border(1.dp, ElegantDarkBorder, RoundedCornerShape(14.dp))
                            .testTag("generator_regenerate_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate",
                            tint = ElegantDarkPrimary,
                            modifier = Modifier.rotate(rotation.value)
                        )
                    }

                    Button(
                        onClick = { onCopy(generatedPassword) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantDarkSurface,
                            contentColor = ElegantDarkPrimary
                        ),
                        border = BorderStroke(1.dp, ElegantDarkBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("generator_copy_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = ElegantDarkPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Copy", fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = { onSaveToVault(generatedPassword) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantDarkPrimary,
                            contentColor = ElegantDarkOnPrimary
                        ),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(50.dp)
                            .testTag("generator_save_vault_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Save to Vault", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Configuration Options Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceElevated),
            border = BorderStroke(1.dp, ElegantDarkBorderSubtle),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Generator Settings",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = ElegantDarkTextPrimary
                )

                // Length Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Password Length",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = ElegantDarkTextPrimary
                        )
                        Text(
                            text = "${options.length} characters",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ElegantDarkPrimary
                        )
                    }

                    Slider(
                        value = options.length.toFloat(),
                        onValueChange = { newLength ->
                            val updated = options.copy(length = newLength.toInt())
                            onOptionsChanged(updated)
                        },
                        valueRange = 8f..48f,
                        steps = 39,
                        colors = SliderDefaults.colors(
                            thumbColor = ElegantDarkPrimary,
                            activeTrackColor = ElegantDarkPrimaryMedium,
                            inactiveTrackColor = ElegantDarkBorder
                        ),
                        modifier = Modifier.testTag("generator_length_slider")
                    )
                }

                // Character Set Toggles
                OptionSwitchRow(
                    label = "Uppercase Letters (A-Z)",
                    checked = options.includeUppercase,
                    onCheckedChange = {
                        val updated = options.copy(includeUppercase = it)
                        ensureAtLeastOne(updated, onOptionsChanged)
                    }
                )

                OptionSwitchRow(
                    label = "Lowercase Letters (a-z)",
                    checked = options.includeLowercase,
                    onCheckedChange = {
                        val updated = options.copy(includeLowercase = it)
                        ensureAtLeastOne(updated, onOptionsChanged)
                    }
                )

                OptionSwitchRow(
                    label = "Numbers (0-9)",
                    checked = options.includeNumbers,
                    onCheckedChange = {
                        val updated = options.copy(includeNumbers = it)
                        ensureAtLeastOne(updated, onOptionsChanged)
                    }
                )

                OptionSwitchRow(
                    label = "Symbols (!@#$%^&*)",
                    checked = options.includeSymbols,
                    onCheckedChange = {
                        val updated = options.copy(includeSymbols = it)
                        ensureAtLeastOne(updated, onOptionsChanged)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun ensureAtLeastOne(
    options: PasswordGenerator.GeneratorOptions,
    onApply: (PasswordGenerator.GeneratorOptions) -> Unit
) {
    if (!options.includeUppercase && !options.includeLowercase && !options.includeNumbers && !options.includeSymbols) {
        onApply(options.copy(includeLowercase = true))
    } else {
        onApply(options)
    }
}

@Composable
private fun OptionSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = ElegantDarkTextPrimary
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ElegantDarkPrimary,
                checkedTrackColor = ElegantDarkPrimaryMedium,
                uncheckedThumbColor = ElegantDarkTextMuted,
                uncheckedTrackColor = ElegantDarkSurface
            )
        )
    }
}
