package com.maangatech.gojuagent.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WorkflowDefinitionDto(
    @Json(name = "provider_code") val providerCode: String,
    @Json(name = "provider_name") val providerName: String,
    @Json(name = "service_type") val serviceType: String,
    @Json(name = "version") val version: Int,
    /** Raw step-engine JSON — passed through opaquely and parsed by core-ussd. */
    @Json(name = "definition") val definition: Map<String, Any?>,
)
