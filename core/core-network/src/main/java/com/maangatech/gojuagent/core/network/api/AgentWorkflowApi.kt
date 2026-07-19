package com.maangatech.gojuagent.core.network.api

import com.maangatech.gojuagent.core.network.ApiEnvelope
import com.maangatech.gojuagent.core.network.dto.WorkflowDefinitionDto
import retrofit2.http.GET
import retrofit2.http.Query

interface AgentWorkflowApi {

    /** Returns every active workflow for the tenant's configured commission operators. */
    @GET("api/agent/v1/workflows")
    suspend fun activeWorkflows(
        @Query("since_version") sinceVersion: Int? = null,
    ): ApiEnvelope<List<WorkflowDefinitionDto>>
}
