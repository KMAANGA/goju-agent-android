package com.maangatech.gojuagent.core.network.api

import com.maangatech.gojuagent.core.network.ApiEnvelope
import com.maangatech.gojuagent.core.network.dto.DeviceStatusResponseDto
import com.maangatech.gojuagent.core.network.dto.LoginRequestDto
import com.maangatech.gojuagent.core.network.dto.LoginResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AgentAuthApi {

    /** Authenticates + registers/looks up this device. Returns `device_status: pending` on a new device. */
    @POST("api/agent/v1/auth/login")
    suspend fun login(@Body body: LoginRequestDto): ApiEnvelope<LoginResponseDto>

    /** Polled by the pairing screen while `device_status == pending`, same shape as the existing web device-trust poll. */
    @GET("api/agent/v1/auth/device-status")
    suspend fun deviceStatus(@Query("device_id") deviceId: String): ApiEnvelope<DeviceStatusResponseDto>

    @POST("api/agent/v1/auth/logout")
    suspend fun logout(): ApiEnvelope<Unit>
}
