package com.maangatech.gojuagent.core.database.repository

import com.maangatech.gojuagent.core.database.dao.CustomerDao
import com.maangatech.gojuagent.core.database.dao.SyncQueueDao
import com.maangatech.gojuagent.core.database.dao.TransactionDao
import com.maangatech.gojuagent.core.database.entity.CustomerEntity
import com.maangatech.gojuagent.core.database.entity.SyncEntityType
import com.maangatech.gojuagent.core.database.entity.SyncQueueEntity
import com.maangatech.gojuagent.core.database.entity.TransactionEntity
import com.maangatech.gojuagent.core.database.entity.TransactionStatus
import com.maangatech.gojuagent.core.database.entity.TransactionSyncStatus
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface TransactionRepository {
    fun observeAll(): Flow<List<TransactionEntity>>
    fun observeToday(): Flow<List<TransactionEntity>>
    fun search(query: String?): Flow<List<TransactionEntity>>

    /** Called the instant a teller taps "Continue" — before the USSD session even starts. */
    suspend fun createPending(draft: TransactionEntity): TransactionEntity

    /** Called when the USSD engine reports a terminal result for [localUuid]. */
    suspend fun completeTransaction(
        localUuid: String,
        status: TransactionStatus,
        ussdReference: String?,
        rawResponse: String?,
        failureReason: String?,
        durationMs: Long,
        commissionMinorUnits: Long?,
        chargesMinorUnits: Long?,
    )

    suspend fun findByLocalUuid(localUuid: String): TransactionEntity?

    /** Called by the sync worker once GOJU Cloud has acknowledged this transaction. */
    suspend fun markSynced(localUuid: String, serverTransactionId: Long)
}

@Singleton
class DefaultTransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val customerDao: CustomerDao,
    private val syncQueueDao: SyncQueueDao,
) : TransactionRepository {

    override fun observeAll(): Flow<List<TransactionEntity>> = transactionDao.observeAll()

    override fun observeToday(): Flow<List<TransactionEntity>> = transactionDao.observeToday()

    override fun search(query: String?): Flow<List<TransactionEntity>> = transactionDao.search(query)

    override suspend fun findByLocalUuid(localUuid: String): TransactionEntity? = transactionDao.findByLocalUuid(localUuid)

    override suspend fun markSynced(localUuid: String, serverTransactionId: Long) {
        val existing = transactionDao.findByLocalUuid(localUuid) ?: return
        transactionDao.update(
            existing.copy(
                syncStatus = TransactionSyncStatus.SYNCED,
                syncedAt = System.currentTimeMillis(),
                serverTransactionId = serverTransactionId,
            ),
        )
    }

    override suspend fun createPending(draft: TransactionEntity): TransactionEntity {
        val withUuid = if (draft.localUuid.isBlank()) {
            draft.copy(localUuid = UUID.randomUUID().toString())
        } else {
            draft
        }
        transactionDao.insert(withUuid)
        ensureCustomerExists(withUuid.customerMsisdn, withUuid.customerName)
        return withUuid
    }

    override suspend fun completeTransaction(
        localUuid: String,
        status: TransactionStatus,
        ussdReference: String?,
        rawResponse: String?,
        failureReason: String?,
        durationMs: Long,
        commissionMinorUnits: Long?,
        chargesMinorUnits: Long?,
    ) {
        val existing = transactionDao.findByLocalUuid(localUuid) ?: return
        val updated = existing.copy(
            status = status,
            ussdReference = ussdReference,
            rawResponse = rawResponse,
            failureReason = failureReason,
            durationMs = durationMs,
            commissionMinorUnits = commissionMinorUnits ?: existing.commissionMinorUnits,
            chargesMinorUnits = chargesMinorUnits ?: existing.chargesMinorUnits,
        )
        transactionDao.update(updated)

        if (status == TransactionStatus.SUCCESS) {
            customerDao.recordTransaction(
                msisdn = updated.customerMsisdn,
                providerCode = updated.providerCode,
                serviceType = updated.serviceType,
                amountMinorUnits = updated.amountMinorUnits,
                timestamp = System.currentTimeMillis(),
            )
            enqueueSync(localUuid)
        }
    }

    private suspend fun ensureCustomerExists(msisdn: String, name: String?) {
        val existing = customerDao.findByMsisdn(msisdn)
        if (existing == null) {
            customerDao.insert(CustomerEntity(msisdn = msisdn, name = name, nickname = null, notes = null))
        }
    }

    private suspend fun enqueueSync(localUuid: String) {
        val now = System.currentTimeMillis()
        syncQueueDao.enqueue(
            SyncQueueEntity(
                entityType = SyncEntityType.TRANSACTION,
                entityLocalUuid = localUuid,
                nextAttemptAt = now,
                createdAt = now,
            ),
        )
    }
}
