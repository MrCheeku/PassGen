package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VaultItem
import com.example.ui.theme.ElegantDarkBorderSubtle
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceElevated
import com.example.ui.theme.ElegantDarkTextMuted
import com.example.ui.theme.ElegantDarkTextPrimary
import com.example.ui.theme.ElegantDarkWeak
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CredentialCard(
    item: VaultItem,
    onCopyPassword: (String) -> Unit,
    onCopyUsername: (String) -> Unit,
    onToggleFavorite: (VaultItem) -> Unit,
    onEdit: (VaultItem) -> Unit,
    onDelete: (VaultItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPasswordRevealed by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val formattedUpdated = remember(item.updatedAt) { dateFormat.format(Date(item.updatedAt)) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("credential_card_${item.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ElegantDarkSurfaceElevated
        ),
        border = BorderStroke(1.dp, ElegantDarkBorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Avatar, Title, Website, and Favorite button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Initial Avatar (styled as in design: rounded-xl bg-white/5)
                    val initial = item.title.firstOrNull()?.uppercaseChar()?.toString() ?: "P"
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(1.dp, ElegantDarkBorderSubtle, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ElegantDarkPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            color = ElegantDarkTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (item.website.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = ElegantDarkTextMuted.copy(alpha = 0.7f),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = item.website,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = ElegantDarkTextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Favorite Pin Icon Button
                IconButton(
                    onClick = { onToggleFavorite(item) },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("pin_button_${item.id}")
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (item.isFavorite) "Unpin" else "Pin",
                        tint = if (item.isFavorite) ElegantDarkPrimary else ElegantDarkTextMuted.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Username Row with Copy
            if (item.username.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ElegantDarkSurface)
                        .border(1.dp, ElegantDarkBorderSubtle, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Username / Email",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = ElegantDarkTextMuted
                        )
                        Text(
                            text = item.username,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, fontSize = 13.sp),
                            color = ElegantDarkTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = { onCopyUsername(item.username) },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("copy_username_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Username",
                            tint = ElegantDarkPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Password Row with Masking, Reveal Toggle & Copy
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ElegantDarkSurface)
                    .border(1.dp, ElegantDarkBorderSubtle, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Password",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = ElegantDarkTextMuted
                    )
                    Text(
                        text = if (isPasswordRevealed) item.password else "••••••••••••",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = if (isPasswordRevealed) 0.5.sp else 2.sp
                        ),
                        color = ElegantDarkTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Show/Hide Toggle
                    IconButton(
                        onClick = { isPasswordRevealed = !isPasswordRevealed },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("toggle_pass_visibility_${item.id}")
                    ) {
                        Icon(
                            imageVector = if (isPasswordRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPasswordRevealed) "Hide Password" else "Show Password",
                            tint = ElegantDarkTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Copy Password Button
                    IconButton(
                        onClick = { onCopyPassword(item.password) },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("copy_password_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Password",
                            tint = ElegantDarkPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Strength Badge & Health Warnings
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Strength pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = item.strength.level.color.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = item.strength.level.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = item.strength.level.color,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    if (item.isShort) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ElegantDarkWeak.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Short (<10)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ElegantDarkWeak,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (item.isOld) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Old (>90d)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFF59E0B),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Updated $formattedUpdated",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = ElegantDarkTextMuted.copy(alpha = 0.8f)
                )
            }

            // Optional Notes section
            if (item.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isExpanded) "Notes: ${item.notes}" else "Notes: ${item.notes.take(40)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElegantDarkTextMuted,
                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                )
            }

            // Actions row: Edit and Delete
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onEdit(item) },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("edit_button_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Credential",
                        tint = ElegantDarkTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { onDelete(item) },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_button_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Credential",
                        tint = ElegantDarkWeak,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
