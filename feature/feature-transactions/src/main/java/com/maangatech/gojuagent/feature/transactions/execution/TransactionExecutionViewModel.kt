package com.maangatech.gojuagent.feature.transactions.execution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maangatech.gojuagent.core.database.entity.TransactionEntity
import com.maangatech.gojuagent.core.database.entity.TransactionStatus
import com.maangatech.gojuagent.core.database.repository.TransactionRepository
import com.maangatech.gojuagent.core.security.DeviceIdentifier
import com.maangatech.gojuagent.core.security.SecurePrefs
import com.maangatech.gojuagent.core.ussd.UssdSessionCoordinator
import com.maangatech.gojuagent.core.ussd.model.UssdSessionEvent
import com.maangatech.gojuagent.feature.transactions.data.TransactionDraftHolder
import com.maangatech.gojuagent.feature.transactions.data.WorkflowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ExecutionPhase { PREPARING, ACCESSIBILITY_REQUIRED, DIALING, IN_PROGRESS, AWAITING_PIN, SUCCEEDED, FAILED, CANCELLED }

data class ExecutionUiState(
    val phase: ExecutionPhase = ExecutionPhase.PREPARING,
    val lastScreenText: String? = null,
    val pinPromptLabel: String? = null,
    val resultReference: String? = null,
    val resultMessage: String? = null,
)

@HiltViewModel
class TransactionExecutionViewModel @Inject constructor(
    private val draftHolder: TransactionDraftHolder,
    private val workflowRepository: WorkflowRepository,
    private val transactionRepository: TransactionRepository,
    private val ussdSessionCoordinator: UssdSessionCoordinator,
    private val securePrefs: SecurePrefs,
    private val deviceIdentifier: DeviceIdentifier,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExecutionUiState())
    val uiState: StateFlow<ExecutionUiState> = _uiState.asStateFlow()

    private var pendingStepId: String? = null
    private var localUuid: String? = null

    fun start(accessibilityEnabled: Boolean) {
        if (!accessibilityEnabled) {
            _uiState.update { it.copy(phase = ExecutionPhase.ACCESSIBILITY_REQUIRED) }
            return
        }
        runTransaction()
    }

    private fun runTransaction() {
        val draft = draftHolder.consume() ?: run {
            _uiState.update { it.copy(phase = ExecutionPhase.FAILED, resultMessage = "No transaction to run.") }
            return
        }

        viewModelScope.launch {
            val workflow = workflowRepository.getActiveWorkflow(draft.providerCode, draft.serviceType)
            if (workflow == null) {
                _uiState.update {
                    it.copy(
                        phase = ExecutionPhase.FAILED,
                        resultMessage = "No workflow is configured for this service yet. Sync with GOJU Cloud or contact support.",
                    )
                }
                return@launch
            }

            val pending = transactionRepository.createPending(
                TransactionEntity(
                    localUuid = "",
                    providerCode = draft.providerCode,
                    providerName = workflow.providerName,
                    serviceType = draft.serviceType,
                    customerMsisdn = draft.customerMsisdn,
                    customerName = draft.customerName,
                    amountMinorUnits = draft.amountMinorUnits,
                    status = TransactionStatus.PENDING,
                    notes = draft.notes,
                    deviceId = deviceIdentifier.stableDeviceId(),
                    agentUserId = securePrefs.agentUserId,
                    agentUserName = securePrefs.agentUserName ?: "",
                    createdAt = System.currentTimeMillis(),
                ),
            )
            localUuid = pending.localUuid

            val formValues = mapOf(
                "customer_msisdn" to draft.customerMsisdn,
                "amount" to formatAmountForUssd(draft.amountMinorUnits),
            )

            val startedAt = System.currentTimeMillis()
            _uiState.update { it.copy(phase = ExecutionPhase.DIALING) }

            ussdSessionCoordinator.runWithTimeout(workflow, formValues) { event ->
                handleEvent(event, pending.localUuid, startedAt)
            }
        }
    }

    private suspend fun handleEvent(event: UssdSessionEvent, localUuid: String, startedAt: Long) {
        val durationMs = System.currentTimeMillis() - startedAt
        when (event) {
            UssdSessionEvent.Dialing -> _uiState.update { it.copy(phase = ExecutionPhase.DIALING) }

            is UssdSessionEvent.ScreenReceived -> _uiState.update {
                it.copy(phase = ExecutionPhase.IN_PROGRESS, lastScreenText = event.rawText)
            }

            is UssdSessionEvent.InputSent -> _uiState.update { it.copy(phase = ExecutionPhase.IN_PROGRESS) }

            is UssdSessionEvent.SecureInputRequired -> {
                pendingStepId = event.stepId
                _uiState.update {
                    it.copy(phase = ExecutionPhase.AWAITING_PIN, pinPromptLabel = event.promptLabel)
                }
            }

            is UssdSessionEvent.Succeeded -> {
                transactionRepository.completeTransaction(
                    localUuid = localUuid,
                    status = TransactionStatus.SUCCESS,
                    ussdReference = event.referenceCode,
                    rawResponse = event.rawText,
                    failureReason = null,
                    durationMs = durationMs,
                    commissionMinorUnits = null,
                    chargesMinorUnits = null,
                )
                _uiState.update {
                    it.copy(phase = ExecutionPhase.SUCCEEDED, resultReference = event.referenceCode, resultMessage = "Transaction completed.")
                }
            }

            is UssdSessionEvent.Failed -> {
                transactionRepository.completeTransaction(
                    localUuid = localUuid,
                    status = TransactionStatus.FAILED,
                    ussdReference = null,
                    rawResponse = event.rawText,
                    failureReason = event.reason,
                    durationMs = durationMs,
                    commissionMinorUnits = null,
                    chargesMinorUnits = null,
                )
                _uiState.update { it.copy(phase = ExecutionPhase.FAILED, resultMessage = event.reason) }
            }

            UssdSessionEvent.TimedOut -> {
                transactionRepository.completeTransaction(
                    localUuid = localUuid,
                    status = TransactionStatus.FAILED,
                    ussdReference = null,
                    rawResponse = _uiState.value.lastScreenText,
                    failureReason = "Timed out waiting for a response.",
                    durationMs = durationMs,
                    commissionMinorUnits = null,
                    chargesMinorUnits = null,
                )
                _uiState.update { it.copy(phase = ExecutionPhase.FAILED, resultMessage = "The transaction timed out. Please check your balance/history before retrying.") }
            }

            UssdSessionEvent.Cancelled -> {
                transactionRepository.completeTransaction(
                    localUuid = localUuid,
                    status = TransactionStatus.CANCELLED,
                    ussdReference = null,
                    rawResponse = _uiState.value.lastScreenText,
                    failureReason = "Cancelled by teller.",
                    durationMs = durationMs,
                    commissionMinorUnits = null,
                    chargesMinorUnits = null,
                )
                _uiState.update { it.copy(phase = ExecutionPhase.CANCELLED, resultMessage = "Transaction cancelled.") }
            }

            is UssdSessionEvent.EngineError -> {
                transactionRepository.completeTransaction(
                    localUuid = localUuid,
                    status = TransactionStatus.FAILED,
                    ussdReference = null,
                    rawResponse = _uiState.value.lastScreenText,
                    failureReason = event.message,
                    durationMs = durationMs,
                    commissionMinorUnits = null,
                    chargesMinorUnits = null,
                )
                _uiState.update { it.copy(phase = ExecutionPhase.FAILED, resultMessage = event.message) }
            }
        }
    }

    fun submitPin(pin: String) {
        val stepId = pendingStepId ?: return
        pendingStepId = null
        _uiState.update { it.copy(phase = ExecutionPhase.IN_PROGRESS, pinPromptLabel = null) }
        ussdSessionCoordinator.submitSecurePin(stepId, pin)
    }

    fun cancelTransaction() {
        ussdSessionCoordinator.cancel()
    }

    fun onAccessibilityEnabled() {
        runTransaction()
    }

    /** Integer-only conversion — avoids floating-point round-trip artifacts (e.g. 123.45 -> 123.44999...). */
    private fun formatAmountForUssd(amountMinorUnits: Long): String {
        val whole = amountMinorUnits / 100
        val fraction = amountMinorUnits % 100
        return if (fraction == 0L) whole.toString() else "$whole.${fraction.toString().padStart(2, '0')}"
    }
}
