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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** In-memory fakes stand in for Room here — this suite is about repository behavior, not SQL. */
private class FakeTransactionDao : TransactionDao {
    val stored = mutableMapOf<String, TransactionEntity>()
    private var nextId = 1L

    override suspend fun insert(transaction: TransactionEntity): Long {
        val id = nextId++
        stored[transaction.localUuid] = transaction.copy(id = id)
        return id
    }

    override suspend fun update(transaction: TransactionEntity) {
        stored[transaction.localUuid] = transaction
    }

    override suspend fun findByLocalUuid(localUuid: String): TransactionEntity? = stored[localUuid]
    override fun observeAll(): Flow<List<TransactionEntity>> = MutableStateFlow(stored.values.toList())
    override fun search(query: String?): Flow<List<TransactionEntity>> = MutableStateFlow(stored.values.toList())
    override suspend fun findBySyncStatus(statuses: List<TransactionSyncStatus>): List<TransactionEntity> =
        stored.values.filter { it.syncStatus in statuses }
    override fun observeCountBySyncStatus(status: TransactionSyncStatus): Flow<Int> =
        MutableStateFlow(stored.values.count { it.syncStatus == status })
    override fun observeToday(): Flow<List<TransactionEntity>> = MutableStateFlow(stored.values.toList())
}

private class FakeCustomerDao : CustomerDao {
    val stored = mutableMapOf<String, CustomerEntity>()
    var recordTransactionCalls = 0

    override suspend fun insert(customer: CustomerEntity): Long {
        stored[customer.msisdn] = customer
        return 1L
    }

    override suspend fun update(customer: CustomerEntity) {
        stored[customer.msisdn] = customer
    }

    override suspend fun findByMsisdn(msisdn: String): CustomerEntity? = stored[msisdn]
    override fun observeFavorites(): Flow<List<CustomerEntity>> = MutableStateFlow(stored.values.filter { it.isFavorite })
    override fun observeRecent(limit: Int): Flow<List<CustomerEntity>> = MutableStateFlow(stored.values.toList())
    override fun search(query: String): Flow<List<CustomerEntity>> = MutableStateFlow(stored.values.toList())

    override suspend fun recordTransaction(
        msisdn: String,
        providerCode: String,
        serviceType: String,
        amountMinorUnits: Long,
        timestamp: Long,
    ) {
        recordTransactionCalls++
        stored[msisdn]?.let {
            stored[msisdn] = it.copy(
                lastProviderCode = providerCode,
                lastServiceType = serviceType,
                lastAmountMinorUnits = amountMinorUnits,
                lastTransactionAt = timestamp,
                transactionCount = it.transactionCount + 1,
            )
        }
    }
}

private class FakeSyncQueueDao : SyncQueueDao {
    val enqueued = mutableListOf<SyncQueueEntity>()
    override suspend fun enqueue(item: SyncQueueEntity) { enqueued.add(item) }
    override suspend fun dueItems(now: Long, limit: Int): List<SyncQueueEntity> = enqueued.filter { it.nextAttemptAt <= now }
    override suspend fun find(type: SyncEntityType, localUuid: String): SyncQueueEntity? =
        enqueued.find { it.entityType == type && it.entityLocalUuid == localUuid }
    override suspend fun recordFailedAttempt(id: Long, now: Long, error: String, nextAttemptAt: Long) {}
    override suspend fun remove(type: SyncEntityType, localUuid: String) {
        enqueued.removeAll { it.entityType == type && it.entityLocalUuid == localUuid }
    }
    override suspend fun pendingCount(): Int = enqueued.size
}

class DefaultTransactionRepositoryTest {

    private lateinit var transactionDao: FakeTransactionDao
    private lateinit var customerDao: FakeCustomerDao
    private lateinit var syncQueueDao: FakeSyncQueueDao
    private lateinit var repository: DefaultTransactionRepository

    @Before
    fun setUp() {
        transactionDao = FakeTransactionDao()
        customerDao = FakeCustomerDao()
        syncQueueDao = FakeSyncQueueDao()
        repository = DefaultTransactionRepository(transactionDao, customerDao, syncQueueDao)
    }

    private fun draft(localUuid: String = "") = TransactionEntity(
        localUuid = localUuid,
        providerCode = "MPESA",
        providerName = "M-Pesa",
        serviceType = "withdraw",
        customerMsisdn = "0712345678",
        customerName = "Jane",
        amountMinorUnits = 5_000_00,
        status = TransactionStatus.PENDING,
        deviceId = "device-1",
        agentUserId = 42L,
        agentUserName = "Agent Smith",
        createdAt = 1_700_000_000_000L,
    )

    @Test
    fun `createPending assigns a UUID when none was provided`() = runBlocking {
        val result = repository.createPending(draft())
        assertTrue(result.localUuid.isNotBlank())
    }

    @Test
    fun `createPending does not overwrite an already-set localUuid`() = runBlocking {
        val result = repository.createPending(draft(localUuid = "fixed-uuid"))
        assertEquals("fixed-uuid", result.localUuid)
    }

    @Test
    fun `createPending registers a new customer the first time this msisdn is seen`() = runBlocking {
        repository.createPending(draft())
        assertNotNull(customerDao.stored["0712345678"])
    }

    @Test
    fun `createPending does not duplicate an existing customer`() = runBlocking {
        customerDao.insert(CustomerEntity(msisdn = "0712345678", name = "Existing", nickname = null, notes = null))
        repository.createPending(draft())
        assertEquals(1, customerDao.stored.size)
    }

    @Test
    fun `completeTransaction with SUCCESS enqueues a sync item and updates customer history`() = runBlocking {
        val pending = repository.createPending(draft())
        repository.completeTransaction(
            localUuid = pending.localUuid,
            status = TransactionStatus.SUCCESS,
            ussdReference = "ABC123",
            rawResponse = "Umefanikiwa",
            failureReason = null,
            durationMs = 4200,
            commissionMinorUnits = 150_00,
            chargesMinorUnits = null,
        )

        val updated = transactionDao.stored[pending.localUuid]!!
        assertEquals(TransactionStatus.SUCCESS, updated.status)
        assertEquals("ABC123", updated.ussdReference)
        assertEquals(1, syncQueueDao.enqueued.size)
        assertEquals(1, customerDao.recordTransactionCalls)
    }

    @Test
    fun `completeTransaction with FAILED does not enqueue a sync item`() = runBlocking {
        val pending = repository.createPending(draft())
        repository.completeTransaction(
            localUuid = pending.localUuid,
            status = TransactionStatus.FAILED,
            ussdReference = null,
            rawResponse = "Samahani",
            failureReason = "Insufficient balance",
            durationMs = 1200,
            commissionMinorUnits = null,
            chargesMinorUnits = null,
        )

        assertEquals(TransactionStatus.FAILED, transactionDao.stored[pending.localUuid]!!.status)
        assertTrue(syncQueueDao.enqueued.isEmpty())
    }

    @Test
    fun `completeTransaction is a no-op for an unknown localUuid`() = runBlocking {
        repository.completeTransaction(
            localUuid = "does-not-exist",
            status = TransactionStatus.SUCCESS,
            ussdReference = null,
            rawResponse = null,
            failureReason = null,
            durationMs = 0,
            commissionMinorUnits = null,
            chargesMinorUnits = null,
        )
        assertTrue(transactionDao.stored.isEmpty())
        assertTrue(syncQueueDao.enqueued.isEmpty())
    }

    @Test
    fun `markSynced sets syncStatus, serverTransactionId, and syncedAt`() = runBlocking {
        val pending = repository.createPending(draft())
        repository.markSynced(pending.localUuid, serverTransactionId = 999L)

        val updated = transactionDao.stored[pending.localUuid]!!
        assertEquals(TransactionSyncStatus.SYNCED, updated.syncStatus)
        assertEquals(999L, updated.serverTransactionId)
        assertNotNull(updated.syncedAt)
    }
}
