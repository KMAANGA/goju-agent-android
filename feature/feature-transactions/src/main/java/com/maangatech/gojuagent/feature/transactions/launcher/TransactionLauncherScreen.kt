package com.maangatech.gojuagent.feature.transactions.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maangatech.gojuagent.feature.transactions.data.ServiceDefinition

private val serviceIcons: Map<String, ImageVector> = mapOf(
    "withdraw" to Icons.Filled.ArrowUpward,
    "deposit" to Icons.Filled.ArrowDownward,
    "float_purchase" to Icons.Filled.Payments,
    "balance_inquiry" to Icons.Filled.AccountBalanceWallet,
)

@Composable
fun TransactionLauncherScreen(
    onServiceSelected: (providerCode: String, serviceType: String) -> Unit,
    viewModel: TransactionLauncherViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val defaultProviderCode = uiState.providers.firstOrNull()?.providerCode ?: "MPESA"

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("New Transaction", style = MaterialTheme.typography.headlineSmall)
        Text(
            uiState.providers.firstOrNull()?.providerName ?: "M-Pesa",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp, top = 4.dp),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(uiState.services, key = { it.serviceType }) { service ->
                ServiceTile(service = service, onClick = { onServiceSelected(defaultProviderCode, service.serviceType) })
            }
        }
    }
}

@Composable
private fun ServiceTile(service: ServiceDefinition, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        Column {
            Icon(
                serviceIcons[service.serviceType] ?: Icons.Filled.Payments,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                service.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
