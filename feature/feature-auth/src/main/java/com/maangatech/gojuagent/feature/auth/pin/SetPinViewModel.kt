package com.maangatech.gojuagent.feature.auth.pin

import androidx.lifecycle.ViewModel
import com.maangatech.gojuagent.core.security.PinAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SetPinUiState(
    val stage: Stage = Stage.ENTER,
    val firstPin: String = "",
    val currentInput: String = "",
    val errorMessage: String? = null,
    val completed: Boolean = false,
) {
    enum class Stage { ENTER, CONFIRM }
}

@HiltViewModel
class SetPinViewModel @Inject constructor(private val pinAuthManager: PinAuthManager) : ViewModel() {

    private val _uiState = MutableStateFlow(SetPinUiState())
    val uiState: StateFlow<SetPinUiState> = _uiState.asStateFlow()

    fun onDigitPressed(digit: Char) {
        val state = _uiState.value
        if (state.currentInput.length >= MAX_PIN_LENGTH) return
        _uiState.update { it.copy(currentInput = it.currentInput + digit, errorMessage = null) }
    }

    fun onBackspace() {
        _uiState.update { it.copy(currentInput = it.currentInput.dropLast(1), errorMessage = null) }
    }

    fun onContinue() {
        val state = _uiState.value
        if (state.currentInput.length < MIN_PIN_LENGTH) {
            _uiState.update { it.copy(errorMessage = "PIN must be at least $MIN_PIN_LENGTH digits.") }
            return
        }

        when (state.stage) {
            SetPinUiState.Stage.ENTER -> _uiState.update {
                it.copy(stage = SetPinUiState.Stage.CONFIRM, firstPin = it.currentInput, currentInput = "")
            }

            SetPinUiState.Stage.CONFIRM -> {
                if (state.currentInput != state.firstPin) {
                    _uiState.update {
                        it.copy(
                            stage = SetPinUiState.Stage.ENTER,
                            firstPin = "",
                            currentInput = "",
                            errorMessage = "PINs didn't match. Try again.",
                        )
                    }
                    return
                }
                pinAuthManager.setPin(state.currentInput)
                _uiState.update { it.copy(completed = true) }
            }
        }
    }

    private companion object {
        const val MIN_PIN_LENGTH = 4
        const val MAX_PIN_LENGTH = 6
    }
}
