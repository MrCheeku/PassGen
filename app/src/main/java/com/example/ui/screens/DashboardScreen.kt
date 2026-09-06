package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VaultItem
import com.example.ui.VaultFilter
import com.example.ui.VaultUiState
import com.example.ui.components.CredentialCard
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkBorderSubtle
import com.example.ui.theme.ElegantDarkOnPrimary
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkPrimaryMedium
import com.example.ui.theme.ElegantDarkPrimarySubtle
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceElevated
import com.example.ui.theme.ElegantDarkTextMuted
import com.example.ui.theme.ElegantDarkTextPrimary
import com.example.ui.theme.ElegantDarkWeak

@Composable
fun DashboardScreen(
    state: VaultUiState,
    onNavigateToVault: (VaultFilter) -> Unit,
    onNavigateToGenerator: () -> Unit,
    onAddPassword: () -> Unit,
    onCopyPassword: (String) -> Unit,
    onCopyUsername: (String) -> Unit,
    onToggleFavorite: (VaultItem) -> Unit,
    onEditItem: (VaultItem) -> Unit,
    onDeleteItem: (VaultItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val health = state.healthReport

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Elegant Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PassGen",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 26.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        color = ElegantDarkPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ENGINEERED BY MR.CHEEKU",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            letterSpacing = 1.4.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = ElegantDarkTextMuted
                    )
                }

                // Header Search Icon Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ElegantDarkSurface)
                        .border(1.dp, ElegantDarkBorder, CircleShape)
                        .clickable { onNavigateToVault(VaultFilter.ALL) }
                        .testTag("dashboard_header_search"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Vault",
                        tint = ElegantDarkPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. Vault Health Hero Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToVault(VaultFilter.NEEDS_ATTENTION) }
                    .testTag("dashboard_vault_health_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ElegantDarkPrimary,
                    contentColor = ElegantDarkOnPrimary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Vault Health",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            ),
                            color = ElegantDarkOnPrimary.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = health.summaryLabel,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = ElegantDarkOnPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val secureText = if (state.items.isEmpty()) {
                            "0 passwords saved locally"
                        } else {
                            "${health.strongCount} passwords are secure"
                        }
                        Text(
                            text = secureText,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = ElegantDarkPrimarySubtle
                        )
                    }

                    // Circular Progress Meter
                    val scorePercent = if (state.items.isEmpty()) 100 else health.overallScore
                    Box(
                        modifier = Modifier.size(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(64.dp)) {
                            val strokeW = 6.dp.toPx()
                            drawCircle(
                                color = ElegantDarkPrimarySubtle.copy(alpha = 0.22f),
                                style = Stroke(width = strokeW)
                            )
                            val sweep = (scorePercent / 100f) * 360f
                            drawArc(
                                color = ElegantDarkPrimaryMedium,
                                startAngle = -90f,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = strokeW)
                            )
                        }
                        Text(
                            text = "$scorePercent%",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = ElegantDarkOnPrimary
                        )
                    }
                }
            }
        }

        // 3. Grid Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MetricCard(
                    title = "Total Entries",
                    count = state.items.size.toString(),
                    icon = Icons.Default.Fingerprint,
                    iconTint = ElegantDarkPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToVault(VaultFilter.ALL) }
                )
                MetricCard(
                    title = "Weak Passwords",
                    count = health.needingAttentionCount.toString(),
                    icon = Icons.Default.Warning,
                    iconTint = ElegantDarkWeak,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToVault(VaultFilter.NEEDS_ATTENTION) }
                )
            }
        }

        // 4. Quick Action Buttons: Generate & New Vault
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToGenerator,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantDarkSurface,
                        contentColor = ElegantDarkPrimary
                    ),
                    border = BorderStroke(1.dp, ElegantDarkBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("dashboard_quick_generate")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Generate", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }

                Button(
                    onClick = onAddPassword,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElegantDarkPrimary,
                        contentColor = ElegantDarkOnPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("dashboard_add_password")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "New Vault", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // 5. Password Health issues breakdown (if any)
        if (health.needingAttentionCount > 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HealthIssueBadge(
                        label = "Weak: ${health.weakCount}",
                        hasIssue = health.weakCount > 0,
                        accentColor = ElegantDarkWeak,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToVault(VaultFilter.WEAK) }
                    )
                    HealthIssueBadge(
                        label = "Short: ${health.shortCount}",
                        hasIssue = health.shortCount > 0,
                        accentColor = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToVault(VaultFilter.SHORT) }
                    )
                    HealthIssueBadge(
                        label = "Reused: ${health.reusedCount}",
                        hasIssue = health.reusedCount > 0,
                        accentColor = Color(0xFFE11D48),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToVault(VaultFilter.REUSED) }
                    )
                }
            }
        }

        // 6. Recent Activity Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = ElegantDarkPrimary
                )

                Text(
                    text = if (state.items.isNotEmpty()) "View All (${state.items.size})" else "View All",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    ),
                    color = ElegantDarkTextMuted,
                    modifier = Modifier.clickable { onNavigateToVault(VaultFilter.ALL) }
                )
            }
        }

        // List of recent credentials or empty state
        if (state.items.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantDarkSurfaceElevated),
                    border = BorderStroke(1.dp, ElegantDarkBorderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = ElegantDarkPrimary.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No saved passwords yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = ElegantDarkTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Generate a strong password and save it locally in your encrypted vault.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElegantDarkTextMuted,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToGenerator,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElegantDarkPrimary,
                                contentColor = ElegantDarkOnPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Generate Password", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Show up to 5 recently updated items on the dashboard
            items(state.items.take(5), key = { it.id }) { item ->
                CredentialCard(
                    item = item,
                    onCopyPassword = onCopyPassword,
                    onCopyUsername = onCopyUsername,
                    onToggleFavorite = onToggleFavorite,
                    onEdit = onEditItem,
                    onDelete = onDeleteItem
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    count: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        border = BorderStroke(1.dp, ElegantDarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = count,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                color = ElegantDarkTextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = ElegantDarkTextMuted
            )
        }
    }
}

@Composable
private fun HealthIssueBadge(
    label: String,
    hasIssue: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (hasIssue) accentColor.copy(alpha = 0.15f) else ElegantDarkSurface,
        border = BorderStroke(1.dp, if (hasIssue) accentColor.copy(alpha = 0.3f) else ElegantDarkBorderSubtle),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (hasIssue) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            ),
            color = if (hasIssue) accentColor else ElegantDarkTextMuted,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}
