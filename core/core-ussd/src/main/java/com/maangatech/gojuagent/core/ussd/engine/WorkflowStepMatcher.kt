package com.maangatech.gojuagent.core.ussd.engine

import com.maangatech.gojuagent.core.ussd.model.InputSource
import com.maangatech.gojuagent.core.ussd.model.WorkflowStep

/**
 * Pure regex-matching logic for the USSD automation engine — deliberately has zero Android
 * framework dependency so it can be exhaustively unit tested against recorded real dialog
 * text without a device/emulator (the actual dialing/accessibility side cannot be
 * unit-tested; this is the part that can and must be).
 */
object WorkflowStepMatcher {

    /** First step (in authored order) whose pattern matches [dialogText], or null if none do. */
    fun findMatchingStep(steps: List<WorkflowStep>, dialogText: String): WorkflowStep? =
        steps.firstOrNull { it.compiledPattern.containsMatchIn(dialogText) }

    /**
     * Resolves the literal string to inject for [step] given the teller's [formValues]
     * (keyed by [WorkflowStep.inputFieldKey]) and any values already captured from earlier
     * screens in this same session (e.g. a confirmation code echoed back mid-flow).
     */
    fun resolveInputValue(
        step: WorkflowStep,
        formValues: Map<String, String>,
        priorCaptures: Map<String, String>,
    ): String? = when (step.inputSource) {
        InputSource.LITERAL -> step.literalValue
        InputSource.FORM_FIELD -> step.inputFieldKey
            ?.let { key -> formValues[key] ?: priorCaptures[key] }
        InputSource.NONE -> null
        // WorkflowSessionEngine.onDialogText() intercepts SECURE_PROMPT steps before ever
        // calling this — a PIN must never be resolved from formValues/priorCaptures. Null
        // here is unreachable in practice, kept only so this `when` stays exhaustive.
        InputSource.SECURE_PROMPT -> null
    }

    /** Extracts regex group [groupIndex] from [dialogText] using [step]'s pattern, if present. */
    fun extractGroup(step: WorkflowStep, dialogText: String, groupIndex: Int?): String? {
        if (groupIndex == null) return null
        val match = step.compiledPattern.find(dialogText) ?: return null
        return match.groupValues.getOrNull(groupIndex)?.takeIf { it.isNotBlank() }
    }
}
