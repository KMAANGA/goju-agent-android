package com.maangatech.gojuagent.feature.transactions.data

import com.maangatech.gojuagent.core.database.entity.TransactionEntity
import javax.inject.Inject
import javax.inject.Singleton

/** Passes the tapped row from history straight to the detail screen — same rationale as [TransactionDraftHolder]. */
@Singleton
class SelectedTransactionHolder @Inject constructor() {
    private var selected: TransactionEntity? = null

    fun set(transaction: TransactionEntity) {
        selected = transaction
    }

    fun get(): TransactionEntity? = selected
}
