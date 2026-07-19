package com.maangatech.gojuagent.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TransactionStatus { PENDING, IN_PROGRESS, SUCCESS, FAILED, CANCELLED }

enum class TransactionSyncStatus { PENDING_SYNC, SYNCING, SYNCED, SYNC_FAILED }

/**
 * The on-device audit source of truth for every attempted transaction — written the moment
 * a teller taps "Continue" (before the USSD session even starts), independent of whether
 * sync to GOJU Cloud ever succeeds. [localUuid] is the idempotency key shared with the
 * server so a retried sync never double-posts.
 */
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["localUuid"], unique = true),
        Index(value = ["syncStatus"]),
        Index(value = ["customerMsisdn"]),
        Index(value = ["createdAt"]),
    ],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val localUuid: String,

    val providerCode: String,
    val providerName: String,
    val serviceType: String,

    val customerMsisdn: String,
    val customerName: String?,

    val amountMinorUnits: Long,
    val currency: String = "TZS",
    val commissionMinorUnits: Long? = null,
    val chargesMinorUnits: Long? = null,

    val status: TransactionStatus,
    val ussdReference: String? = null,
    val rawResponse: String? = null,
    val failureReason: String? = null,
    val notes: String? = null,

    val latitude: Double? = null,
    val longitude: Double? = null,

    val deviceId: String,
    val agentUserId: Long,
    val agentUserName: String,

    val durationMs: Long? = null,
    val createdAt: Long,

    val syncStatus: TransactionSyncStatus = TransactionSyncStatus.PENDING_SYNC,
    val syncedAt: Long? = null,
    val serverTransactionId: Long? = null,
)
