package com.maangatech.gojuagent.core.network.api

import com.maangatech.gojuagent.core.network.ApiEnvelope
import com.maangatech.gojuagent.core.network.dto.AgentTransactionSyncRequestDto
import com.maangatech.gojuagent.core.network.dto.AgentTransactionSyncResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AgentTransactionApi {

    /**
     * Idempotent on `local_uuid` — a retried sync (e.g. after a dropped connection right
     * after the server committed) must return the original `server_transaction_id`, not
     * create a duplicate ledger-bound record.
     */
    @POST("api/agent/v1/transactions")
    suspend fun submit(@Body body: AgentTransactionSyncRequestDto): ApiEnvelope<AgentTransactionSyncResponseDto>
}
