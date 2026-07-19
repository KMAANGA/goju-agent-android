package com.maangatech.gojuagent.feature.transactions.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maangatech.gojuagent.core.designsystem.component.GojuPrimaryButton
import com.maangatech.gojuagent.feature.transactions.data.FormField

@Composable
fun TransactionFormScreen(
    onContinueToExecution: () -> Unit,
    viewModel: TransactionFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.readyToExecute) {
        if (uiState.readyToExecute) onContinueToExecution()
    }

    val service = uiState.service ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(service.displayName, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))

        if (service.fields.contains(FormField.CUSTOMER_MSISDN)) {
            OutlinedTextField(
                value = uiState.customerMsisdn,
                onValueChange = viewModel::onCustomerMsisdnChange,
                label = { Text("Customer Number") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.customerName,
                onValueChange = viewModel::onCustomerNameChange,
                label = { Text("Customer Name (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
        }

        if (service.fields.contains(FormField.AMOUNT)) {
            OutlinedTextField(
                value = uiState.amountText,
                onValueChange = viewModel::onAmountChange,
                label = { Text("Amount (TZS)") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
        }

        if (service.fields.contains(FormField.NOTES)) {
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Reference / Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
        }

        uiState.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(8.dp))
        GojuPrimaryButton(text = "Continue", onClick = viewModel::onContinue)
    }
}
