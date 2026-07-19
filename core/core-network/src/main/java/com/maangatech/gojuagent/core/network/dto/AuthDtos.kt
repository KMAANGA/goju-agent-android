package com.maangatech.gojuagent.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequestDto(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "device_id") val deviceId: String,
    @Json(name = "device_name") val deviceName: String,
    @Json(name = "platform") val platform: String = "android",
    @Json(name = "app_version") val appVersion: String,
)

@JsonClass(generateAdapter = true)
data class LoginResponseDto(
    @Json(name = "device_status") val deviceStatus: String, // pending | approved | revoked
    @Json(name = "api_token") val apiToken: String?, // present only once device_status == approved
    @Json(name = "agent") val agent: AgentProfileDto?,
)

@JsonClass(generateAdapter = true)
data class AgentProfileDto(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "branch_id") val branchId: Long,
    @Json(name = "branch_name") val branchName: String?,
    @Json(name = "tenant_name") val tenantName: String?,
)

@JsonClass(generateAdapter = true)
data class DeviceStatusResponseDto(
    @Json(name = "device_status") val deviceStatus: String,
    @Json(name = "api_token") val apiToken: String?,
    @Json(name = "agent") val agent: AgentProfileDto?,
)
