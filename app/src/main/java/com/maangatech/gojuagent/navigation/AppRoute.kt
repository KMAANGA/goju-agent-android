package com.maangatech.gojuagent.navigation

object AppRoute {
    const val LOGIN = "login"
    const val PAIRING = "pairing"
    const val SET_PIN = "set_pin"
    const val UNLOCK = "unlock"
    const val MAIN = "main"
    const val TRANSACTION_LAUNCHER = "transaction_launcher"
    const val TRANSACTION_FORM = "transaction_form/{providerCode}/{serviceType}"
    const val TRANSACTION_EXECUTION = "transaction_execution"
    const val TRANSACTION_DETAIL = "transaction_detail"

    fun transactionForm(providerCode: String, serviceType: String) = "transaction_form/$providerCode/$serviceType"
}
