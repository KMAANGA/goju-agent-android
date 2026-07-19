package com.maangatech.gojuagent.core.ussd

import com.maangatech.gojuagent.core.common.ApplicationScope
import com.maangatech.gojuagent.core.ussd.accessibility.UssdAccessibilityService
import com.maangatech.gojuagent.core.ussd.engine.WorkflowSessionEngine
import com.maangatech.gojuagent.core.ussd.model.UssdSessionEvent
import com.maangatech.gojuagent.core.ussd.model.WorkflowDefinition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single entry point feature-transactions uses to run a transaction — hides the
 * dial/accessibility/timeout plumbing behind one call. Callers just collect [run]'s Flow
 * until a terminal ([UssdSessionEvent.Succeeded]/[Failed]/[Cancelled]/[TimedOut]) event.
 */
@Singleton
class UssdSessionCoordinator @Inject constructor(
    private val dialer: UssdDialer,
    private val eventBus: UssdSessionEventBus,
    @ApplicationScope private val applicationScope: CoroutineScope,
) {

    fun run(workflow: WorkflowDefinition, formValues: Map<String, String>): Flow<UssdSessionEvent> {
        val engine = WorkflowSessionEngine(workflow, formValues)
        eventBus.beginSession(engine)
        dialer.dial(workflow.dialCode)

        return eventBus.events.transformWhile { event ->
            emit(event)
            when (event) {
                is UssdSessionEvent.Succeeded,
                is UssdSessionEvent.Failed,
                is UssdSessionEvent.Cancelled,
                is UssdSessionEvent.TimedOut,
                is UssdSessionEvent.EngineError,
                -> false
                else -> true
            }
        }
    }

    /** Races [run]'s Flow against the workflow's own timeout; call from the ViewModel with `withTimeoutOrNull`-style usage isn't enough on its own since a stalled accessibility read never emits — this actively force-cancels. */
    suspend fun runWithTimeout(
        workflow: WorkflowDefinition,
        formValues: Map<String, String>,
        onEvent: suspend (UssdSessionEvent) -> Unit,
    ) {
        val timeoutMs = workflow.timeoutSeconds * 1000L
        var timedOut = false
        val watchdog = applicationScope.launch {
            delay(timeoutMs)
            timedOut = true
            eventBus.endSession()
            eventBus.emit(UssdSessionEvent.TimedOut)
        }
        try {
            run(workflow, formValues).collect { event -> if (!timedOut) onEvent(event) }
        } finally {
            watchdog.cancel()
        }
    }

    fun cancel() {
        UssdAccessibilityService.requestCancel()
    }

    /** [secureValue] (the PIN) is passed straight through to the live dialog and never stored. */
    fun submitSecurePin(stepId: String, secureValue: String) {
        UssdAccessibilityService.requestSubmitSecureInput(stepId, secureValue)
    }

    fun isAccessibilityServiceEnabled(context: android.content.Context): Boolean =
        UssdAccessibilityService.isEnabled(context)
}
