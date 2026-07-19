package com.maangatech.gojuagent.feature.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.maangatech.gojuagent.core.database.dao.SyncQueueDao
import com.maangatech.gojuagent.core.database.entity.SyncEntityType
import com.maangatech.gojuagent.core.database.entity.TransactionEntity
import com.maangatech.gojuagent.core.database.entity.TransactionStatus
import com.maangatech.gojuagent.core.database.repository.TransactionRepository
import com.maangatech.gojuagent.core.network.api.AgentTransactionApi
import com.maangatech.gojuagent.core.network.dto.AgentTransactionSyncRequestDto
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.pow

/**
 * Drains the offline outbox — each [com.maangatech.gojuagent.core.database.entity.SyncQueueEntity]
 * only holds a `local_uuid`; the transaction row itself (Room's audit source of truth) is
 * looked up fresh each attempt so a transaction edited/completed between enqueue and sync
 * (shouldn't happen, but) never syncs stale data.
 */
@HiltWorker
class SyncOutboxWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncQueueDao: SyncQueueDao,
    private val transactionRepository: TransactionRepository,
    private val transactionApi: AgentTransactionApi,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dueItems = syncQueueDao.dueItems(now = System.currentTimeMillis())
        if (dueItems.isEmpty()) return Result.success()

        var anyFailure = false
        for (item in dueItems) {
            if (item.entityType != SyncEntityType.TRANSACTION) continue
            val transaction = transactionRepository.findByLocalUuid(item.entityLocalUuid)
            if (transaction == null) {
                syncQueueDao.remove(item.entityType, item.entityLocalUuid)
                continue
            }

            val outcome = syncOne(transaction)
            if (outcome) {
                syncQueueDao.remove(item.entityType, item.entityLocalUuid)
            } else {
                anyFailure = true
                val backoffMs = backoffFor(item.attemptCount + 1)
                syncQueueDao.recordFailedAttempt(
                    id = item.id,
                    now = System.currentTimeMillis(),
                    error = "Sync failed",
                    nextAttemptAt = System.currentTimeMillis() + backoffMs,
                )
            }
        }

        return if (anyFailure) Result.retry() else Result.success()
    }

    private suspend fun syncOne(transaction: TransactionEntity): Boolean = try {
        val response = transactionApi.submit(transaction.toDto())
        val body = response.data
        if (response.success && body != null) {
            transactionRepository.markSynced(transaction.localUuid, body.serverTransactionId)
            true
        } else {
            false
        }
    } catch (e: IOException) {
        false // no connection — retry later, not a permanent failure
    } catch (e: HttpException) {
        // A 409/422 (validation/idempotency conflict) will never succeed on retry with the
        // same payload; treat as "handled" so it doesn't jam the queue forever. A real
        // implementation should surface this to the teller via a "sync error" badge.
        e.code() !in listOf(409, 422)
    }

    private fun backoffFor(attemptCount: Int): Long {
        val minutes = minOf(2.0.pow(attemptCount).toLong(), MAX_BACKOFF_MINUTES)
        return minutes * 60_000L
    }

    private fun TransactionEntity.toDto() = AgentTransactionSyncRequestDto(
        localUuid = localUuid,
        providerCode = providerCode,
        serviceType = serviceType,
        customerMsisdn = customerMsisdn,
        customerName = customerName,
        amountMinorUnits = amountMinorUnits,
        currency = currency,
        commissionMinorUnits = commissionMinorUnits,
        chargesMinorUnits = chargesMinorUnits,
        status = status.name.lowercase(),
        ussdReference = ussdReference,
        rawResponse = rawResponse,
        durationMs = durationMs,
        occurredAt = isoFormat(createdAt),
        latitude = latitude,
        longitude = longitude,
        notes = notes,
    )

    private fun isoFormat(epochMillis: Long): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(java.util.Date(epochMillis))

    private companion object {
        const val MAX_BACKOFF_MINUTES = 60L
    }
}
