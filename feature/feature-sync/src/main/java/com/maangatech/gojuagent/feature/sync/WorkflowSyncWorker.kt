package com.maangatech.gojuagent.feature.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.maangatech.gojuagent.core.database.dao.WorkflowDao
import com.maangatech.gojuagent.core.database.entity.WorkflowDefinitionEntity
import com.maangatech.gojuagent.core.network.api.AgentWorkflowApi
import com.maangatech.gojuagent.core.network.dto.WorkflowDefinitionDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException

/**
 * Pulls the latest active USSD workflow definitions from GOJU Cloud. Failure here is
 * silent-by-design at the call site — the app keeps running on whatever it last cached (see
 * `WorkflowDefinitionEntity`'s docs), so a flaky network never blocks a teller from
 * transacting, it just means they're on a slightly older menu version until the next sync.
 */
@HiltWorker
class WorkflowSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val workflowDao: WorkflowDao,
    private val workflowApi: AgentWorkflowApi,
    private val moshi: Moshi,
) : CoroutineWorker(context, params) {

    private val mapAdapter = moshi.adapter<Map<String, Any?>>(
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java),
    )

    override suspend fun doWork(): Result = try {
        val response = workflowApi.activeWorkflows()
        val workflows = response.data
        if (response.success && workflows != null) {
            workflows.forEach { dto -> workflowDao.upsert(dto.toEntity()) }
            Result.success()
        } else {
            Result.retry()
        }
    } catch (e: IOException) {
        Result.retry()
    } catch (e: Exception) {
        Result.failure()
    }

    private fun WorkflowDefinitionDto.toEntity() = WorkflowDefinitionEntity(
        providerCode = providerCode,
        providerName = providerName,
        serviceType = serviceType,
        version = version,
        definitionJson = mapAdapter.toJson(definition),
        isActive = true,
        updatedAt = System.currentTimeMillis(),
    )
}
