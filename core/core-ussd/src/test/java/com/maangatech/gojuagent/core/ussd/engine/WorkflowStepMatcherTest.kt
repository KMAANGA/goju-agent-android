package com.maangatech.gojuagent.core.ussd.engine

import com.maangatech.gojuagent.core.ussd.model.InputSource
import com.maangatech.gojuagent.core.ussd.model.TerminalOutcome
import com.maangatech.gojuagent.core.ussd.model.WorkflowStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkflowStepMatcherTest {

    private val amountStep = WorkflowStep(
        id = "enter_amount",
        matchPattern = "(?i)kiasi",
        inputSource = InputSource.FORM_FIELD,
        inputFieldKey = "amount",
    )

    private val menuStep = WorkflowStep(
        id = "main_menu",
        matchPattern = "(?i)chagua huduma",
        inputSource = InputSource.LITERAL,
        literalValue = "1",
    )

    private val successStep = WorkflowStep(
        id = "success",
        matchPattern = "(?i)umefanikiwa.*id:\\s*([A-Z0-9]+)",
        isTerminal = true,
        terminalOutcome = TerminalOutcome.SUCCESS,
        referenceCaptureGroup = 1,
    )

    @Test
    fun `findMatchingStep returns first step whose pattern matches`() {
        val steps = listOf(menuStep, amountStep, successStep)
        val result = WorkflowStepMatcher.findMatchingStep(steps, "Weka Kiasi cha kutoa")
        assertEquals(amountStep, result)
    }

    @Test
    fun `findMatchingStep returns null when nothing matches`() {
        val steps = listOf(menuStep, amountStep)
        val result = WorkflowStepMatcher.findMatchingStep(steps, "Some unrelated screen text")
        assertNull(result)
    }

    @Test
    fun `findMatchingStep is case-insensitive`() {
        val result = WorkflowStepMatcher.findMatchingStep(listOf(menuStep), "CHAGUA HUDUMA:")
        assertEquals(menuStep, result)
    }

    @Test
    fun `resolveInputValue pulls from form values for FORM_FIELD steps`() {
        val value = WorkflowStepMatcher.resolveInputValue(
            amountStep,
            formValues = mapOf("amount" to "50000"),
            priorCaptures = emptyMap(),
        )
        assertEquals("50000", value)
    }

    @Test
    fun `resolveInputValue falls back to prior captures when form values are missing`() {
        val value = WorkflowStepMatcher.resolveInputValue(
            amountStep,
            formValues = emptyMap(),
            priorCaptures = mapOf("amount" to "12000"),
        )
        assertEquals("12000", value)
    }

    @Test
    fun `resolveInputValue returns null when a required form field was never provided`() {
        val value = WorkflowStepMatcher.resolveInputValue(
            amountStep,
            formValues = emptyMap(),
            priorCaptures = emptyMap(),
        )
        assertNull(value)
    }

    @Test
    fun `resolveInputValue returns the literal for LITERAL steps regardless of form values`() {
        val value = WorkflowStepMatcher.resolveInputValue(
            menuStep,
            formValues = mapOf("amount" to "999"),
            priorCaptures = emptyMap(),
        )
        assertEquals("1", value)
    }

    @Test
    fun `extractGroup pulls the requested capture group from the dialog text`() {
        val reference = WorkflowStepMatcher.extractGroup(
            successStep,
            "Umefanikiwa. Transaction ID: ABC123XYZ",
            groupIndex = 1,
        )
        assertEquals("ABC123XYZ", reference)
    }

    @Test
    fun `extractGroup returns null when groupIndex is null`() {
        assertNull(WorkflowStepMatcher.extractGroup(successStep, "Umefanikiwa. ID: ABC123", groupIndex = null))
    }

    @Test
    fun `extractGroup returns null when the pattern does not match`() {
        assertNull(WorkflowStepMatcher.extractGroup(successStep, "Something else entirely", groupIndex = 1))
    }
}
