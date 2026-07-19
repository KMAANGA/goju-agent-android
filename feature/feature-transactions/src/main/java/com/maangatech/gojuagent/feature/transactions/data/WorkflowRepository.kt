package com.maangatech.gojuagent.feature.transactions.data

import com.maangatech.gojuagent.core.database.dao.ProviderSummary
import com.maangatech.gojuagent.core.database.dao.WorkflowDao
import com.maangatech.gojuagent.core.ussd.WorkflowJsonParser
import com.maangatech.gojuagent.core.ussd.model.WorkflowDefinition
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridges the opaque JSON cached in Room (`WorkflowDefinitionEntity`) to the typed
 * [WorkflowDefinition] the USSD engine runs. Lives here, not in core-database or core-ussd,
 * so those two core modules stay decoupled from each other — this feature module is the
 * first place that actually needs both.
 */
interface WorkflowRepository {
    fun observeActiveProviders(): Flow<List<ProviderSummary>>
    suspend fun getActiveWorkflow(providerCode: String, serviceType: String): WorkflowDefinition?
}

@Singleton
class DefaultWorkflowRepository @Inject constructor(
    private val workflowDao: WorkflowDao,
    private val jsonParser: WorkflowJsonParser,
) : WorkflowRepository {

    override fun observeActiveProviders(): Flow<List<ProviderSummary>> = workflowDao.observeActiveProviders()

    override suspend fun getActiveWorkflow(providerCode: String, serviceType: String): WorkflowDefinition? {
        val entity = workflowDao.findActive(providerCode, serviceType) ?: return null
        return jsonParser.parse(entity.definitionJson)
    }
}
