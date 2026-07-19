package com.maangatech.gojuagent.core.ussd.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * SECURE_PROMPT is distinct from FORM_FIELD on purpose: a mobile-money PIN must be typed by
 * the teller at the moment the USSD menu asks for it, never collected up-front on the
 * transaction form and never persisted (not in Room, not in logs, not even transiently in
 * the engine's capture map) — see [com.maangatech.gojuagent.core.ussd.engine.WorkflowSessionEngine].
 */
enum class InputSource { FORM_FIELD, LITERAL, SECURE_PROMPT, NONE }

enum class TerminalOutcome { SUCCESS, FAILURE }

/**
 * One node in a provider's USSD menu tree. Steps are evaluated top-to-bottom against the
 * live dialog text; the first whose [matchPattern] matches wins — order matters, so more
 * specific patterns (e.g. an explicit error message) must precede generic ones (e.g. "any
 * screen with an amount prompt") in the authored JSON.
 */
@JsonClass(generateAdapter = true)
data class WorkflowStep(
    @Json(name = "id") val id: String,
    @Json(name = "match_pattern") val matchPattern: String,
    @Json(name = "input_source") val inputSource: InputSource = InputSource.NONE,
    @Json(name = "input_field_key") val inputFieldKey: String? = null,
    @Json(name = "literal_value") val literalValue: String? = null,
    /** Shown to the teller only when [inputSource] is [InputSource.SECURE_PROMPT], e.g. "Enter your M-Pesa PIN". */
    @Json(name = "prompt_label") val promptLabel: String? = null,
    @Json(name = "is_terminal") val isTerminal: Boolean = false,
    @Json(name = "terminal_outcome") val terminalOutcome: TerminalOutcome? = null,
    @Json(name = "reference_capture_group") val referenceCaptureGroup: Int? = null,
    @Json(name = "failure_capture_group") val failureCaptureGroup: Int? = null,
) {
    /** Compiles lazily and once per step instance — steps are matched on every dialog screen. */
    val compiledPattern: Regex by lazy { Regex(matchPattern, RegexOption.IGNORE_CASE) }
}

@JsonClass(generateAdapter = true)
data class WorkflowDefinition(
    @Json(name = "provider_code") val providerCode: String,
    @Json(name = "provider_name") val providerName: String,
    @Json(name = "service_type") val serviceType: String,
    @Json(name = "version") val version: Int,
    @Json(name = "dial_code") val dialCode: String,
    @Json(name = "steps") val steps: List<WorkflowStep>,
    /** Hard ceiling on total session wall-clock time before the engine force-cancels. */
    @Json(name = "timeout_seconds") val timeoutSeconds: Int = 90,
)
