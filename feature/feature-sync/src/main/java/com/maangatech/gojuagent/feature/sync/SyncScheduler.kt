package com.maangatech.gojuagent.feature.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** The one place transaction sync and workflow sync get scheduled — call [schedulePeriodic] once at app start. */
@Singleton
class SyncScheduler @Inject constructor(private val workManager: WorkManager) {

    private val networkConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun schedulePeriodic() {
        workManager.enqueueUniquePeriodicWork(
            TRANSACTION_SYNC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<SyncOutboxWorker>(15, TimeUnit.MINUTES)
                .setConstraints(networkConstraint)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build(),
        )

        workManager.enqueueUniquePeriodicWork(
            WORKFLOW_SYNC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<WorkflowSyncWorker>(6, TimeUnit.HOURS)
                .setConstraints(networkConstraint)
                .build(),
        )
    }

    /** Triggered by the teller's "Sync Now" button — runs immediately, ignoring the periodic schedule's interval. */
    fun syncNow() {
        workManager.enqueueUniqueWork(
            TRANSACTION_SYNC_WORK_ONE_TIME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<SyncOutboxWorker>().setConstraints(networkConstraint).build(),
        )
        workManager.enqueueUniqueWork(
            WORKFLOW_SYNC_WORK_ONE_TIME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<WorkflowSyncWorker>().setConstraints(networkConstraint).build(),
        )
    }

    fun observePendingSyncCount() = workManager.getWorkInfosForUniqueWorkLiveData(TRANSACTION_SYNC_WORK)

    private companion object {
        const val TRANSACTION_SYNC_WORK = "transaction_sync_periodic"
        const val WORKFLOW_SYNC_WORK = "workflow_sync_periodic"
        const val TRANSACTION_SYNC_WORK_ONE_TIME = "transaction_sync_now"
        const val WORKFLOW_SYNC_WORK_ONE_TIME = "workflow_sync_now"
    }
}
