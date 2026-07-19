package com.maangatech.gojuagent.feature.auth.pairing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maangatech.gojuagent.core.common.AppResult
import com.maangatech.gojuagent.feature.auth.data.AuthRepository
import com.maangatech.gojuagent.feature.auth.data.LoginOutcome
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PairingUiState(
    val isPolling: Boolean = true,
    val approved: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class DevicePairingViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PairingUiState())
    val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()

    init {
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (isActive && !_uiState.value.approved) {
                when (val result = authRepository.pollDeviceStatus()) {
                    is AppResult.Success -> when (result.data) {
                        is LoginOutcome.Approved -> _uiState.update { it.copy(isPolling = false, approved = true) }
                        is LoginOutcome.PendingApproval -> Unit // keep polling
                    }
                    is AppResult.Error -> _uiState.update { it.copy(errorMessage = result.error.message) }
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private companion object {
        const val POLL_INTERVAL_MS = 5000L
    }
}
