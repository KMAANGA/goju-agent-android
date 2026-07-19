package com.maangatech.gojuagent.core.ussd.engine

import com.maangatech.gojuagent.core.ussd.model.InputSource
import com.maangatech.gojuagent.core.ussd.model.TerminalOutcome
import com.maangatech.gojuagent.core.ussd.model.WorkflowDefinition

/** What the accessibility service must do next in response to a screen of dialog text. */
sealed class EngineDecision {
    data class SendInput(val stepId: String, val text: String) : EngineDecision()

    /**
     * The engine has paused: this screen needs a value (a PIN) that must come from the
     * teller right now, not from anything collected earlier. The service must NOT touch the
     * dialog until [com.maangatech.gojuagent.core.ussd.engine.WorkflowSessionEngine.submitSecureInput]
     * is called with what the teller typed.
     */
    data class AwaitSecureInput(val stepId: String, val promptLabel: String) : EngineDecision()

    data class Terminal(val outcome: TerminalOutcome, val referenceCode: String?, val failureReason: String?) :
        EngineDecision()
    data object Unrecognized : EngineDecision()
}

/**
 * Drives one USSD session end-to-end: holds the active [WorkflowDefinition], the teller's
 * form input, and everything captured off intermediate screens, and turns each new dialog
 * screen into a single [EngineDecision]. One instance = one in-flight transaction; discard
 * it once a [EngineDecision.Terminal] is reached.
 */
class WorkflowSessionEngine(
    private val workflow: WorkflowDefinition,
    private val formValues: Map<String, String>,
) {
    private val captures = mutableMapOf<String, String>()

    fun onDialogText(dialogText: String): EngineDecision {
        val step = WorkflowStepMatcher.findMatchingStep(workflow.steps, dialogText)
            ?: return EngineDecision.Unrecognized

        if (step.isTerminal) {
            val outcome = step.terminalOutcome ?: TerminalOutcome.FAILURE
            return if (outcome == TerminalOutcome.SUCCESS) {
                val reference = WorkflowStepMatcher.extractGroup(step, dialogText, step.referenceCaptureGroup)
                EngineDecision.Terminal(TerminalOutcome.SUCCESS, reference, null)
            } else {
                val reason = WorkflowStepMatcher.extractGroup(step, dialogText, step.failureCaptureGroup)
                    ?: "The provider declined this transaction."
                EngineDecision.Terminal(TerminalOutcome.FAILURE, null, reason)
            }
        }

        if (step.inputSource == InputSource.SECURE_PROMPT) {
            return EngineDecision.AwaitSecureInput(step.id, step.promptLabel ?: "Enter your PIN")
        }

        val input = WorkflowStepMatcher.resolveInputValue(step, formValues, captures)
            ?: return EngineDecision.Terminal(
                TerminalOutcome.FAILURE,
                null,
                "Workflow step '${step.id}' expected form field '${step.inputFieldKey}' but it was not provided.",
            )

        step.inputFieldKey?.let { captures[it] = input }
        return EngineDecision.SendInput(step.id, input)
    }

    /**
     * Resumes a session paused on [EngineDecision.AwaitSecureInput]. [secureValue] (the PIN)
     * is used only for this immediate return value — it is never written into [captures], so
     * it cannot leak into any later capture-group lookup, log line, or crash report.
     */
    fun submitSecureInput(stepId: String, secureValue: String): EngineDecision =
        EngineDecision.SendInput(stepId, secureValue)
}
