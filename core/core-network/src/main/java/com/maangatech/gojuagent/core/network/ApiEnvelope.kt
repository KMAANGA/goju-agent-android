package com.maangatech.gojuagent.core.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * The consistent `{success, data, message, errors}` envelope every new `Api\Agent\*`
 * Laravel endpoint returns — see the GOJU Cloud API plan. There was no pre-existing
 * envelope convention in goju-saas to match, so this shape was defined fresh for the
 * mobile API and must stay identical on both ends.
 */
@JsonClass(generateAdapter = true)
data class ApiEnvelope<T>(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: T?,
    @Json(name = "message") val message: String?,
    @Json(name = "errors") val errors: Map<String, List<String>>? = null,
)
