package com.maangatech.gojuagent.core.ussd.model

/** Emitted on [com.maangatech.gojuagent.core.ussd.UssdSessionEventBus] for the execution screen to render. */
sealed class UssdSessionEvent {
    data object Dialing : UssdSessionEvent()
    data class ScreenReceived(val rawText: String) : UssdSessionEvent()
    data class InputSent(val stepId: String) : UssdSessionEvent()
    data class SecureInputRequired(val stepId: String, val promptLabel: String) : UssdSessionEvent()
    data class Succeeded(val referenceCode: String?, val rawText: String) : UssdSessionEvent()
    data class Failed(val reason: String, val rawText: String?) : UssdSessionEvent()
    data object TimedOut : UssdSessionEvent()
    data object Cancelled : UssdSessionEvent()
    data class EngineError(val message: String) : UssdSessionEvent()
}
