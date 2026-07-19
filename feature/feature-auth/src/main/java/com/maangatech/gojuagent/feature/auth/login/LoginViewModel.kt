package com.maangatech.gojuagent.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maangatech.gojuagent.core.common.AppResult
import com.maangatech.gojuagent.feature.auth.data.AuthRepository
import com.maangatech.gojuagent.feature.auth.data.LoginOutcome
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed class LoginNavigationEvent {
    data object NavigateToPairing : LoginNavigationEvent()
    data object NavigateToSetPin : LoginNavigationEvent()
}

@HiltViewModel
class LoginViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _navigationEvents = MutableStateFlow<LoginNavigationEvent?>(null)
    val navigationEvents: StateFlow<LoginNavigationEvent?> = _navigationEvents.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }

    fun submit(appVersion: String) {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter your email and password.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = authRepository.login(state.email.trim(), state.password, appVersion)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    when (result.data) {
                        is LoginOutcome.PendingApproval -> _navigationEvents.value = LoginNavigationEvent.NavigateToPairing
                        is LoginOutcome.Approved -> _navigationEvents.value = LoginNavigationEvent.NavigateToSetPin
                    }
                }

                is AppResult.Error -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.error.message)
                }
            }
        }
    }

    fun consumeNavigationEvent() {
        _navigationEvents.value = null
    }
}
