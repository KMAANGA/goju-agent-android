package com.maangatech.gojuagent.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.maangatech.gojuagent.core.database.entity.SyncEntityType
import com.maangatech.gojuagent.core.database.entity.SyncQueueEntity

@Dao
interface SyncQueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: SyncQueueEntity)

    @Query("SELECT * FROM sync_queue WHERE nextAttemptAt <= :now ORDER BY createdAt ASC LIMIT :limit")
    suspend fun dueItems(now: Long, limit: Int = 25): List<SyncQueueEntity>

    @Query("SELECT * FROM sync_queue WHERE entityType = :type AND entityLocalUuid = :localUuid LIMIT 1")
    suspend fun find(type: SyncEntityType, localUuid: String): SyncQueueEntity?

    @Query(
        "UPDATE sync_queue SET attemptCount = attemptCount + 1, lastAttemptAt = :now, lastError = :error, nextAttemptAt = :nextAttemptAt WHERE id = :id",
    )
    suspend fun recordFailedAttempt(id: Long, now: Long, error: String, nextAttemptAt: Long)

    @Query("DELETE FROM sync_queue WHERE entityType = :type AND entityLocalUuid = :localUuid")
    suspend fun remove(type: SyncEntityType, localUuid: String)

    @Query("SELECT COUNT(*) FROM sync_queue")
    suspend fun pendingCount(): Int
}
