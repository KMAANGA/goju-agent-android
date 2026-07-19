package com.maangatech.gojuagent.feature.transactions.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.maangatech.gojuagent.feature.transactions.data.CustomerPrefillHolder
import com.maangatech.gojuagent.feature.transactions.data.FormField
import com.maangatech.gojuagent.feature.transactions.data.ServiceCatalog
import com.maangatech.gojuagent.feature.transactions.data.ServiceDefinition
import com.maangatech.gojuagent.feature.transactions.data.TransactionDraft
import com.maangatech.gojuagent.feature.transactions.data.TransactionDraftHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class TransactionFormUiState(
    val service: ServiceDefinition? = null,
    val customerMsisdn: String = "",
    val customerName: String = "",
    val amountText: String = "",
    val notes: String = "",
    val errorMessage: String? = null,
    val readyToExecute: Boolean = false,
)

@HiltViewModel
class TransactionFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val draftHolder: TransactionDraftHolder,
    private val customerPrefillHolder: CustomerPrefillHolder,
) : ViewModel() {

    private val providerCode: String = checkNotNull(savedStateHandle["providerCode"])
    private val serviceType: String = checkNotNull(savedStateHandle["serviceType"])

    private val _uiState = MutableStateFlow(
        TransactionFormUiState(service = ServiceCatalog.find(serviceType)),
    )
    val uiState: StateFlow<TransactionFormUiState> = _uiState.asStateFlow()

    init {
        // Only apply the prefill if it matches what this form was actually opened for —
        // e.g. a customer with no transaction history yet lands here via the plain launcher
        // grid instead of a service-specific quick-repeat, and should start blank.
        customerPrefillHolder.consume()?.let { customer ->
            _uiState.update {
                it.copy(
                    customerMsisdn = customer.msisdn,
                    customerName = customer.nickname ?: customer.name ?: "",
                    amountText = if (customer.lastProviderCode == providerCode && customer.lastServiceType == serviceType) {
                        customer.lastAmountMinorUnits?.let { minor -> formatMinorUnitsAsInput(minor) } ?: ""
                    } else {
                        ""
                    },
                )
            }
        }
    }

    private fun formatMinorUnitsAsInput(minorUnits: Long): String {
        val whole = minorUnits / 100
        val fraction = minorUnits % 100
        return if (fraction == 0L) whole.toString() else "$whole.${fraction.toString().padStart(2, '0')}"
    }

    fun onCustomerMsisdnChange(value: String) = _uiState.update { it.copy(customerMsisdn = value, errorMessage = null) }
    fun onCustomerNameChange(value: String) = _uiState.update { it.copy(customerName = value) }
    fun onAmountChange(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d{0,12}(\\.\\d{0,2})?$"))) {
            _uiState.update { it.copy(amountText = value, errorMessage = null) }
        }
    }
    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }

    fun onContinue() {
        val state = _uiState.value
        val service = state.service ?: return

        if (service.requiresCustomer && !isValidMsisdn(state.customerMsisdn)) {
            _uiState.update { it.copy(errorMessage = "Enter a valid customer phone number.") }
            return
        }

        val requiresAmount = service.fields.contains(FormField.AMOUNT)
        val amountMinorUnits = if (requiresAmount) {
            val amount = state.amountText.toDoubleOrNull()
            if (amount == null || amount <= 0.0) {
                _uiState.update { it.copy(errorMessage = "Enter a valid amount.") }
                return
            }
            Math.round(amount * 100)
        } else {
            0L
        }

        draftHolder.set(
            TransactionDraft(
                providerCode = providerCode,
                serviceType = serviceType,
                customerMsisdn = state.customerMsisdn.trim(),
                customerName = state.customerName.trim().ifBlank { null },
                amountMinorUnits = amountMinorUnits,
                notes = state.notes.trim().ifBlank { null },
            ),
        )
        _uiState.update { it.copy(readyToExecute = true) }
    }

    private fun isValidMsisdn(value: String): Boolean = value.trim().length in 9..13 && value.trim().all { it.isDigit() || it == '+' }
}
