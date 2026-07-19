package com.maangatech.gojuagent.feature.transactions.data

import javax.inject.Inject
import javax.inject.Singleton

data class TransactionDraft(
    val providerCode: String,
    val serviceType: String,
    val customerMsisdn: String,
    val customerName: String?,
    val amountMinorUnits: Long,
    val notes: String?,
)

/**
 * Hands a fully-validated [TransactionDraft] from the form screen to the execution screen
 * without round-tripping every field through a nav-route string (amount/notes/free text
 * don't belong URL-encoded in a route). Single-slot by design — only one transaction can be
 * in flight at a time, matching [com.maangatech.gojuagent.core.ussd.UssdSessionEventBus].
 */
@Singleton
class TransactionDraftHolder @Inject constructor() {
    private var draft: TransactionDraft? = null

    fun set(draft: TransactionDraft) {
        this.draft = draft
    }

    fun consume(): TransactionDraft? {
        val current = draft
        draft = null
        return current
    }
}
