package com.maangatech.gojuagent.feature.auth.unlock

import androidx.lifecycle.ViewModel
import com.maangatech.gojuagent.core.security.PinAuthManager
import com.maangatech.gojuagent.core.security.SessionGuard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class UnlockUiState(
    val pinInput: String = "",
    val errorMessage: String? = null,
    val unlocked: Boolean = false,
)

@HiltViewModel
class UnlockViewModel @Inject constructor(
    private val pinAuthManager: PinAuthManager,
    private val sessionGuard: SessionGuard,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UnlockUiState())
    val uiState: StateFlow<UnlockUiState> = _uiState.asStateFlow()

    fun onDigitPressed(digit: Char) {
        val newInput = (_uiState.value.pinInput + digit).takeLast(MAX_PIN_LENGTH)
        _uiState.update { it.copy(pinInput = newInput, errorMessage = null) }
        if (newInput.length >= MIN_PIN_LENGTH) tryUnlockWithPin(newInput)
    }

    fun onBackspace() {
        _uiState.update { it.copy(pinInput = it.pinInput.dropLast(1), errorMessage = null) }
    }

    fun onBiometricSuccess() = markUnlocked()

    private fun tryUnlockWithPin(pin: String) {
        if (pin.length < MIN_PIN_LENGTH) return
        if (pinAuthManager.verifyPin(pin)) {
            markUnlocked()
        } else if (pin.length >= MAX_PIN_LENGTH) {
            _uiState.update { it.copy(pinInput = "", errorMessage = "Incorrect PIN. Try again.") }
        }
    }

    private fun markUnlocked() {
        sessionGuard.recordActivity()
        _uiState.update { it.copy(unlocked = true) }
    }

    private companion object {
        const val MIN_PIN_LENGTH = 4
        const val MAX_PIN_LENGTH = 6
    }
}
