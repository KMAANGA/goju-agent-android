package com.maangatech.gojuagent.feature.transactions.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maangatech.gojuagent.core.database.entity.TransactionEntity
import com.maangatech.gojuagent.core.designsystem.component.StatusBadge

@Composable
fun TransactionDetailScreen(transaction: TransactionEntity) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(transaction.serviceType.replace('_', ' ').replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.headlineSmall)
            StatusBadge(text = transaction.status.name, tone = transaction.status.toTone())
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        DetailRow("Provider", transaction.providerName)
        DetailRow("Customer", transaction.customerName ?: "—")
        DetailRow("Phone", transaction.customerMsisdn)
        DetailRow("Amount", "${transaction.currency} ${"%,d".format(transaction.amountMinorUnits / 100)}")
        transaction.commissionMinorUnits?.let { DetailRow("Commission", "${transaction.currency} ${"%,d".format(it / 100)}") }
        transaction.chargesMinorUnits?.let { DetailRow("Charges", "${transaction.currency} ${"%,d".format(it / 100)}") }
        DetailRow("Reference", transaction.ussdReference ?: "—")
        transaction.failureReason?.let { DetailRow("Reason", it) }
        transaction.durationMs?.let { DetailRow("Duration", "${it / 1000.0}s") }
        DetailRow("Sync Status", transaction.syncStatus.name)
        transaction.notes?.let { DetailRow("Notes", it) }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
