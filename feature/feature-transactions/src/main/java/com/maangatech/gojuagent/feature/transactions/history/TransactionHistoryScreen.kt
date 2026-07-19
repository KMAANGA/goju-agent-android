package com.maangatech.gojuagent.feature.transactions.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maangatech.gojuagent.core.database.entity.TransactionEntity
import com.maangatech.gojuagent.core.database.entity.TransactionStatus
import com.maangatech.gojuagent.core.designsystem.component.StatusBadge
import com.maangatech.gojuagent.core.designsystem.component.StatusTone
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionHistoryScreen(
    onTransactionSelected: (TransactionEntity) -> Unit,
    viewModel: TransactionHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::onQueryChange,
            placeholder = { Text("Search reference, customer, provider…") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )

        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            items(uiState.transactions, key = { it.id }) { transaction ->
                TransactionRow(transaction = transaction, onClick = { onTransactionSelected(transaction) })
            }
        }
    }
}

@Composable
private fun TransactionRow(transaction: TransactionEntity, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text("${transaction.serviceType.replace('_', ' ').replaceFirstChar { it.uppercase() }} — ${formatMoney(transaction.amountMinorUnits, transaction.currency)}") },
        supportingContent = {
            Text("${transaction.customerName ?: transaction.customerMsisdn} · ${dateFormat.format(Date(transaction.createdAt))}")
        },
        trailingContent = { StatusBadge(text = transaction.status.name, tone = transaction.status.toTone()) },
    )
}

internal fun TransactionStatus.toTone(): StatusTone = when (this) {
    TransactionStatus.SUCCESS -> StatusTone.SUCCESS
    TransactionStatus.PENDING, TransactionStatus.IN_PROGRESS -> StatusTone.PENDING
    TransactionStatus.FAILED -> StatusTone.FAILED
    TransactionStatus.CANCELLED -> StatusTone.WARNING
}

private fun formatMoney(minorUnits: Long, currency: String): String {
    val whole = minorUnits / 100
    return "$currency ${"%,d".format(whole)}"
}
