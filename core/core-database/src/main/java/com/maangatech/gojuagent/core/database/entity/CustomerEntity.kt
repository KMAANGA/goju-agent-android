package com.maangatech.gojuagent.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A frequently-served customer. Populated automatically the first time a transaction is
 * completed against a new [msisdn], then enriched (nickname/notes/favorite) by the teller —
 * this is what powers "recent customers" and "quick repeat" so numbers are never retyped.
 */
@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["msisdn"], unique = true),
        Index(value = ["isFavorite"]),
        Index(value = ["lastTransactionAt"]),
    ],
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val msisdn: String,
    val name: String?,
    val nickname: String?,
    val notes: String?,
    val isFavorite: Boolean = false,

    val lastProviderCode: String? = null,
    val lastServiceType: String? = null,
    val lastAmountMinorUnits: Long? = null,
    val lastTransactionAt: Long? = null,
    val transactionCount: Int = 0,
)
