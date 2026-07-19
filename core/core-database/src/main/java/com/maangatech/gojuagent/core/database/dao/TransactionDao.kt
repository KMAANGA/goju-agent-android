package com.maangatech.gojuagent.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.maangatech.gojuagent.core.database.entity.TransactionEntity
import com.maangatech.gojuagent.core.database.entity.TransactionSyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE localUuid = :localUuid LIMIT 1")
    suspend fun findByLocalUuid(localUuid: String): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE (:query IS NULL OR customerMsisdn LIKE '%' || :query || '%'
            OR customerName LIKE '%' || :query || '%'
            OR ussdReference LIKE '%' || :query || '%'
            OR providerName LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
        """,
    )
    fun search(query: String?): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE syncStatus IN (:statuses) ORDER BY createdAt ASC")
    suspend fun findBySyncStatus(statuses: List<TransactionSyncStatus>): List<TransactionEntity>

    @Query("SELECT COUNT(*) FROM transactions WHERE syncStatus = :status")
    fun observeCountBySyncStatus(status: TransactionSyncStatus): Flow<Int>

    @Query(
        """
        SELECT * FROM transactions
        WHERE date(createdAt / 1000, 'unixepoch', 'localtime') = date('now', 'localtime')
        ORDER BY createdAt DESC
        """,
    )
    fun observeToday(): Flow<List<TransactionEntity>>
}
