package com.maangatech.gojuagent.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.maangatech.gojuagent.core.database.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(customer: CustomerEntity): Long

    @Update
    suspend fun update(customer: CustomerEntity)

    @Query("SELECT * FROM customers WHERE msisdn = :msisdn LIMIT 1")
    suspend fun findByMsisdn(msisdn: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE isFavorite = 1 ORDER BY name ASC")
    fun observeFavorites(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers ORDER BY lastTransactionAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 20): Flow<List<CustomerEntity>>

    @Query(
        """
        SELECT * FROM customers
        WHERE msisdn LIKE '%' || :query || '%'
           OR name LIKE '%' || :query || '%'
           OR nickname LIKE '%' || :query || '%'
        ORDER BY lastTransactionAt DESC
        """,
    )
    fun search(query: String): Flow<List<CustomerEntity>>

    /** Called after every completed transaction to keep "recent"/"quick repeat" data fresh. */
    @Query(
        """
        UPDATE customers SET
            lastProviderCode = :providerCode,
            lastServiceType = :serviceType,
            lastAmountMinorUnits = :amountMinorUnits,
            lastTransactionAt = :timestamp,
            transactionCount = transactionCount + 1
        WHERE msisdn = :msisdn
        """,
    )
    suspend fun recordTransaction(
        msisdn: String,
        providerCode: String,
        serviceType: String,
        amountMinorUnits: Long,
        timestamp: Long,
    )
}
