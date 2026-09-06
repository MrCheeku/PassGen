package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VaultItem
import com.example.ui.SortOption
import com.example.ui.VaultFilter
import com.example.ui.VaultUiState
import com.example.ui.components.CredentialCard
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkBorderSubtle
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceElevated
import com.example.ui.theme.ElegantDarkTextMuted
import com.example.ui.theme.ElegantDarkTextPrimary

@Composable
fun VaultListScreen(
    state: VaultUiState,
    onSearchChange: (String) -> Unit,
    onFilterChange: (VaultFilter) -> Unit,
    onSortChange: (SortOption) -> Unit,
    onCopyPassword: (String) -> Unit,
    onCopyUsername: (String) -> Unit,
    onToggleFavorite: (VaultItem) -> Unit,
    onEditItem: (VaultItem) -> Unit,
    onDeleteItem: (VaultItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("vault_list_screen")
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Search Bar & Sort Button Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        "Search title, username, website...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ElegantDarkTextMuted.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = ElegantDarkPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Search",
                                tint = ElegantDarkTextMuted
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElegantDarkPrimary,
                    unfocusedBorderColor = ElegantDarkBorder,
                    focusedContainerColor = ElegantDarkSurface,
                    unfocusedContainerColor = ElegantDarkSurface,
                    focusedTextColor = ElegantDarkTextPrimary,
                    unfocusedTextColor = ElegantDarkTextPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("vault_search_field")
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Sort Menu
            Box {
                IconButton(
                    onClick = { sortMenuExpanded = true },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ElegantDarkSurface)
                        .border(1.dp, ElegantDarkBorder, RoundedCornerShape(16.dp))
                        .testTag("vault_sort_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort Options",
                        tint = ElegantDarkPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false }
                ) {
                    for (option in SortOption.entries) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.displayName,
                                    fontWeight = if (state.sortOption == option) FontWeight.Bold else FontWeight.Normal,
                                    color = if (state.sortOption == option) ElegantDarkPrimary else ElegantDarkTextPrimary
                                )
                            },
                            onClick = {
                                onSortChange(option)
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (filter in VaultFilter.entries) {
                val isSelected = state.activeFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { onFilterChange(filter) },
                    label = {
                        Text(
                            text = filter.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) ElegantDarkPrimary else ElegantDarkBorderSubtle
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = ElegantDarkSurface,
                        selectedContainerColor = ElegantDarkPrimary.copy(alpha = 0.15f),
                        labelColor = ElegantDarkTextMuted,
                        selectedLabelColor = ElegantDarkPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Results count / Sort label indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${state.filteredItems.size} ${if (state.filteredItems.size == 1) "credential" else "credentials"}",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                color = ElegantDarkTextMuted
            )
            Text(
                text = "Sorted: ${state.sortOption.displayName}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = ElegantDarkTextMuted.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Vault list or empty state
        if (state.filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = ElegantDarkTextMuted.copy(alpha = 0.4f),
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (state.searchQuery.isNotEmpty()) "No results matching \"${state.searchQuery}\"" else "No credentials in this filter",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ElegantDarkTextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.filteredItems, key = { it.id }) { item ->
                    CredentialCard(
                        item = item,
                        onCopyPassword = onCopyPassword,
                        onCopyUsername = onCopyUsername,
                        onToggleFavorite = onToggleFavorite,
                        onEdit = onEditItem,
                        onDelete = onDeleteItem
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(72.dp)) // Space for FAB and bottom bar
                }
            }
        }
    }
}
