package com.maangatech.gojuagent

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.maangatech.gojuagent.core.common.ApplicationScope
import com.maangatech.gojuagent.feature.sync.SyncScheduler
import com.maangatech.gojuagent.workflow.WorkflowSeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class GojuAgentApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var workflowSeeder: WorkflowSeeder
    @Inject lateinit var syncScheduler: SyncScheduler
    @Inject @ApplicationScope lateinit var applicationScope: CoroutineScope

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // ExistingPeriodicWorkPolicy.KEEP makes this idempotent across every process start.
        syncScheduler.schedulePeriodic()
        applicationScope.launch {
            workflowSeeder.seedIfEmpty()
        }
    }
}
