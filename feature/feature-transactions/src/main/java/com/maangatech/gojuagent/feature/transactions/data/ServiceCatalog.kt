package com.maangatech.gojuagent.feature.transactions.data

/** One entry per Smart Transaction Launcher tile. MVP scope is M-Pesa only — see plan Phase 1. */
enum class FormField { CUSTOMER_MSISDN, AMOUNT, NOTES }

data class ServiceDefinition(
    val serviceType: String,
    val displayName: String,
    val requiresCustomer: Boolean,
    val fields: List<FormField>,
)

object ServiceCatalog {
    val MPESA_SERVICES = listOf(
        ServiceDefinition(
            serviceType = "withdraw",
            displayName = "Cash Withdrawal",
            requiresCustomer = true,
            fields = listOf(FormField.CUSTOMER_MSISDN, FormField.AMOUNT, FormField.NOTES),
        ),
        ServiceDefinition(
            serviceType = "deposit",
            displayName = "Cash Deposit",
            requiresCustomer = true,
            fields = listOf(FormField.CUSTOMER_MSISDN, FormField.AMOUNT, FormField.NOTES),
        ),
        ServiceDefinition(
            serviceType = "float_purchase",
            displayName = "Float Purchase",
            requiresCustomer = false,
            fields = listOf(FormField.AMOUNT, FormField.NOTES),
        ),
        ServiceDefinition(
            serviceType = "balance_inquiry",
            displayName = "Balance Inquiry",
            requiresCustomer = false,
            fields = emptyList(),
        ),
    )

    fun find(serviceType: String): ServiceDefinition? = MPESA_SERVICES.firstOrNull { it.serviceType == serviceType }
}
