package com.maangatech.gojuagent.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AgentTransactionSyncRequestDto(
    @Json(name = "local_uuid") val localUuid: String,
    @Json(name = "provider_code") val providerCode: String,
    @Json(name = "service_type") val serviceType: String,
    @Json(name = "customer_msisdn") val customerMsisdn: String,
    @Json(name = "customer_name") val customerName: String?,
    @Json(name = "amount_minor_units") val amountMinorUnits: Long,
    @Json(name = "currency") val currency: String,
    @Json(name = "commission_minor_units") val commissionMinorUnits: Long?,
    @Json(name = "charges_minor_units") val chargesMinorUnits: Long?,
    @Json(name = "status") val status: String,
    @Json(name = "ussd_reference") val ussdReference: String?,
    @Json(name = "raw_response") val rawResponse: String?,
    @Json(name = "duration_ms") val durationMs: Long?,
    @Json(name = "occurred_at") val occurredAt: String, // ISO-8601
    @Json(name = "latitude") val latitude: Double?,
    @Json(name = "longitude") val longitude: Double?,
    @Json(name = "notes") val notes: String?,
)

@JsonClass(generateAdapter = true)
data class AgentTransactionSyncResponseDto(
    @Json(name = "server_transaction_id") val serverTransactionId: Long,
    @Json(name = "status") val status: String,
    @Json(name = "local_uuid") val localUuid: String,
)
