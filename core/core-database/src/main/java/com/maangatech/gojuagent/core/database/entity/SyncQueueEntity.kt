package com.maangatech.gojuagent.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SyncEntityType { TRANSACTION }

/**
 * Retry bookkeeping for the offline outbox. Kept separate from [TransactionEntity] so other
 * syncable entity types can share the same outbox machinery later without adding
 * sync-specific columns to every domain table.
 */
@Entity(
    tableName = "sync_queue",
    indices = [
        Index(value = ["entityType", "entityLocalUuid"], unique = true),
        Index(value = ["nextAttemptAt"]),
    ],
)
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: SyncEntityType,
    val entityLocalUuid: String,
    val attemptCount: Int = 0,
    val lastAttemptAt: Long? = null,
    val lastError: String? = null,
    val nextAttemptAt: Long,
    val createdAt: Long,
)
