package com.maangatech.gojuagent.feature.transactions.data

import com.maangatech.gojuagent.core.database.entity.CustomerEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Carries a "quick repeat" customer from the customer list into the transaction form so
 * their number/name — and, if they've transacted before, their last provider/service/amount
 * — are pre-filled. The teller still confirms/edits the amount on the form before
 * continuing; quick-repeat never dials straight from the customer list without that
 * confirmation step.
 */
@Singleton
class CustomerPrefillHolder @Inject constructor() {
    private var customer: CustomerEntity? = null

    fun set(customer: CustomerEntity) {
        this.customer = customer
    }

    fun consume(): CustomerEntity? {
        val current = customer
        customer = null
        return current
    }
}
