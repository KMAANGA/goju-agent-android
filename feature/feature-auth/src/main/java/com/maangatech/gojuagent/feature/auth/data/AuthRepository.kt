package com.maangatech.gojuagent.feature.auth.data

import com.maangatech.gojuagent.core.common.AppError
import com.maangatech.gojuagent.core.common.AppResult
import com.maangatech.gojuagent.core.network.api.AgentAuthApi
import com.maangatech.gojuagent.core.network.dto.AgentProfileDto
import com.maangatech.gojuagent.core.network.dto.LoginRequestDto
import com.maangatech.gojuagent.core.security.DeviceIdentifier
import com.maangatech.gojuagent.core.security.SecurePrefs
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

data class AgentProfile(
    val id: Long,
    val name: String,
    val email: String,
    val branchName: String,
    val tenantName: String,
)

sealed class LoginOutcome {
    data class PendingApproval(val deviceId: String) : LoginOutcome()
    data class Approved(val agent: AgentProfile) : LoginOutcome()
}

interface AuthRepository {
    suspend fun login(email: String, password: String, appVersion: String): AppResult<LoginOutcome>
    suspend fun pollDeviceStatus(): AppResult<LoginOutcome>
    fun isSignedIn(): Boolean
    fun isDeviceApproved(): Boolean
    fun signOut()
}

@Singleton
class DefaultAuthRepository @Inject constructor(
    private val api: AgentAuthApi,
    private val securePrefs: SecurePrefs,
    private val deviceIdentifier: DeviceIdentifier,
) : AuthRepository {

    override suspend fun login(email: String, password: String, appVersion: String): AppResult<LoginOutcome> =
        safeCall {
            val response = api.login(
                LoginRequestDto(
                    email = email,
                    password = password,
                    deviceId = deviceIdentifier.stableDeviceId(),
                    deviceName = deviceIdentifier.deviceName(),
                    appVersion = appVersion,
                ),
            )
            val body = response.data
                ?: return AppResult.Error(mapMessage(response.message))

            securePrefs.deviceApprovalStatus = body.deviceStatus
            if (body.deviceStatus == "approved" && body.apiToken != null && body.agent != null) {
                persistSession(body.apiToken, body.agent)
                AppResult.Success(LoginOutcome.Approved(body.agent.toDomain()))
            } else {
                AppResult.Success(LoginOutcome.PendingApproval(deviceIdentifier.stableDeviceId()))
            }
        }

    override suspend fun pollDeviceStatus(): AppResult<LoginOutcome> = safeCall {
        val response = api.deviceStatus(deviceIdentifier.stableDeviceId())
        val body = response.data ?: return AppResult.Error(mapMessage(response.message))
        securePrefs.deviceApprovalStatus = body.deviceStatus
        if (body.deviceStatus == "approved" && body.apiToken != null && body.agent != null) {
            persistSession(body.apiToken, body.agent)
            AppResult.Success(LoginOutcome.Approved(body.agent.toDomain()))
        } else {
            AppResult.Success(LoginOutcome.PendingApproval(deviceIdentifier.stableDeviceId()))
        }
    }

    override fun isSignedIn(): Boolean = securePrefs.isSignedIn

    override fun isDeviceApproved(): Boolean = securePrefs.isDeviceApproved

    override fun signOut() {
        securePrefs.clearSession()
    }

    private fun persistSession(token: String, agent: AgentProfileDto) {
        securePrefs.apiToken = token
        securePrefs.agentUserId = agent.id
        securePrefs.agentUserName = agent.name
        securePrefs.agentBranchName = agent.branchName
    }

    private fun AgentProfileDto.toDomain() = AgentProfile(id, name, email, branchName, tenantName)

    private fun mapMessage(message: String?): AppError =
        AppError.ServerError(422, message ?: "Sign-in failed. Please check your credentials.")

    private inline fun <T> safeCall(block: () -> AppResult<T>): AppResult<T> = try {
        block()
    } catch (e: IOException) {
        AppResult.Error(AppError.NoConnection)
    } catch (e: HttpException) {
        if (e.code() == 401) AppResult.Error(AppError.Unauthorized) else AppResult.Error(AppError.ServerError(e.code(), null))
    } catch (e: Exception) {
        AppResult.Error(AppError.Unknown(e))
    }
}
