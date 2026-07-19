package com.maangatech.gojuagent.feature.customers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maangatech.gojuagent.core.database.entity.CustomerEntity

@Composable
fun CustomerListScreen(
    onCustomerSelected: (CustomerEntity) -> Unit,
    viewModel: CustomerListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::onQueryChange,
            placeholder = { Text("Search by name, number, or nickname") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )

        if (uiState.query.isBlank()) {
            TabRow(selectedTabIndex = if (uiState.tab == CustomerListTab.FAVORITES) 1 else 0) {
                Tab(
                    selected = uiState.tab == CustomerListTab.RECENT,
                    onClick = { viewModel.onTabSelected(CustomerListTab.RECENT) },
                    text = { Text("Recent") },
                )
                Tab(
                    selected = uiState.tab == CustomerListTab.FAVORITES,
                    onClick = { viewModel.onTabSelected(CustomerListTab.FAVORITES) },
                    text = { Text("Favorites") },
                )
            }
        }

        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            items(uiState.customers, key = { it.id }) { customer ->
                CustomerRow(
                    customer = customer,
                    onClick = { onCustomerSelected(customer) },
                    onToggleFavorite = { viewModel.toggleFavorite(customer) },
                )
            }
        }
    }
}

@Composable
private fun CustomerRow(customer: CustomerEntity, onClick: () -> Unit, onToggleFavorite: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(customer.nickname ?: customer.name ?: customer.msisdn) },
        supportingContent = {
            Column {
                Text(customer.msisdn, style = MaterialTheme.typography.bodySmall)
                if (customer.transactionCount > 0) {
                    Text(
                        "${customer.transactionCount} transaction${if (customer.transactionCount == 1) "" else "s"}",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        },
        trailingContent = {
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (customer.isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Toggle favorite",
                    tint = if (customer.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}
