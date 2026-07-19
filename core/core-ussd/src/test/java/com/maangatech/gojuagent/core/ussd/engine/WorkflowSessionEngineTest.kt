package com.maangatech.gojuagent.core.ussd.engine

import com.maangatech.gojuagent.core.ussd.model.InputSource
import com.maangatech.gojuagent.core.ussd.model.TerminalOutcome
import com.maangatech.gojuagent.core.ussd.model.WorkflowDefinition
import com.maangatech.gojuagent.core.ussd.model.WorkflowStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkflowSessionEngineTest {

    private val withdrawWorkflow = WorkflowDefinition(
        providerCode = "MPESA",
        providerName = "M-Pesa",
        serviceType = "withdraw",
        version = 1,
        dialCode = "*150*00#",
        steps = listOf(
            WorkflowStep(id = "menu", matchPattern = "(?i)chagua huduma", inputSource = InputSource.LITERAL, literalValue = "1"),
            WorkflowStep(id = "customer", matchPattern = "(?i)namba ya mteja", inputSource = InputSource.FORM_FIELD, inputFieldKey = "customer_msisdn"),
            WorkflowStep(id = "amount", matchPattern = "(?i)kiasi", inputSource = InputSource.FORM_FIELD, inputFieldKey = "amount"),
            WorkflowStep(id = "pin", matchPattern = "(?i)weka pin", inputSource = InputSource.SECURE_PROMPT, promptLabel = "Enter your PIN"),
            WorkflowStep(
                id = "success", matchPattern = "(?i)umefanikiwa.*id:\\s*([A-Z0-9]+)",
                isTerminal = true, terminalOutcome = TerminalOutcome.SUCCESS, referenceCaptureGroup = 1,
            ),
            WorkflowStep(
                id = "failure", matchPattern = "(?i)samahani[:\\s]*(.*)",
                isTerminal = true, terminalOutcome = TerminalOutcome.FAILURE, failureCaptureGroup = 1,
            ),
        ),
    )

    private fun newEngine() = WorkflowSessionEngine(
        withdrawWorkflow,
        formValues = mapOf("customer_msisdn" to "0712345678", "amount" to "50000"),
    )

    @Test
    fun `walks the menu step to the customer number step`() {
        val engine = newEngine()
        val decision = engine.onDialogText("Chagua huduma: 1. Toa Pesa 2. Weka Pesa")
        assertEquals(EngineDecision.SendInput("menu", "1"), decision)
    }

    @Test
    fun `pulls the customer number from form values`() {
        val engine = newEngine()
        val decision = engine.onDialogText("Weka Namba ya Mteja")
        assertEquals(EngineDecision.SendInput("customer", "0712345678"), decision)
    }

    @Test
    fun `pauses on a SECURE_PROMPT step instead of resolving from form values`() {
        val engine = newEngine()
        val decision = engine.onDialogText("Weka PIN yako")
        assertTrue(decision is EngineDecision.AwaitSecureInput)
        assertEquals("pin", (decision as EngineDecision.AwaitSecureInput).stepId)
        assertEquals("Enter your PIN", decision.promptLabel)
    }

    @Test
    fun `submitSecureInput resumes with exactly the value it was given`() {
        val engine = newEngine()
        engine.onDialogText("Weka PIN yako") // pause
        val resumed = engine.submitSecureInput("pin", "1234")
        assertEquals(EngineDecision.SendInput("pin", "1234"), resumed)
    }

    @Test
    fun `recognizes a terminal success screen and extracts the reference code`() {
        val engine = newEngine()
        val decision = engine.onDialogText("Umefanikiwa. Muamala ID: QAX7Y8Z9")
        assertEquals(EngineDecision.Terminal(TerminalOutcome.SUCCESS, "QAX7Y8Z9", null), decision)
    }

    @Test
    fun `recognizes a terminal failure screen and extracts the failure reason`() {
        val engine = newEngine()
        val decision = engine.onDialogText("Samahani: Salio halitoshi")
        assertEquals(EngineDecision.Terminal(TerminalOutcome.FAILURE, null, "Salio halitoshi"), decision)
    }

    @Test
    fun `returns Unrecognized for a screen matching no step`() {
        val engine = newEngine()
        val decision = engine.onDialogText("Please wait...")
        assertEquals(EngineDecision.Unrecognized, decision)
    }

    @Test
    fun `fails the session when a required form field was never collected`() {
        val engineWithNoAmount = WorkflowSessionEngine(withdrawWorkflow, formValues = mapOf("customer_msisdn" to "0712345678"))
        engineWithNoAmount.onDialogText("Chagua huduma") // menu
        engineWithNoAmount.onDialogText("Weka Namba ya Mteja") // customer
        val decision = engineWithNoAmount.onDialogText("Weka Kiasi")
        assertTrue(decision is EngineDecision.Terminal)
        assertEquals(TerminalOutcome.FAILURE, (decision as EngineDecision.Terminal).outcome)
    }
}
