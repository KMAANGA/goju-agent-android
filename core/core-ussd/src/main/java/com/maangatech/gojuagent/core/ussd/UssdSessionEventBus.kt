package com.maangatech.gojuagent.core.ussd

import com.maangatech.gojuagent.core.ussd.engine.WorkflowSessionEngine
import com.maangatech.gojuagent.core.ussd.model.UssdSessionEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single hand-off point between [com.maangatech.gojuagent.core.ussd.accessibility.UssdAccessibilityService]
 * (which only exists while a system dialog is on screen and cannot be constructor-injected
 * per-call) and the feature-transactions execution screen. Also holds the one active
 * [WorkflowSessionEngine] instance, since only one USSD session can realistically be in
 * flight on a single-SIM-dialog device at a time.
 */
@Singleton
class UssdSessionEventBus @Inject constructor() {

    private val _events = MutableSharedFlow<UssdSessionEvent>(replay = 1, extraBufferCapacity = 8)
    val events: SharedFlow<UssdSessionEvent> = _events

    @Volatile
    var activeEngine: WorkflowSessionEngine? = null
        private set

    fun beginSession(engine: WorkflowSessionEngine) {
        activeEngine = engine
        emit(UssdSessionEvent.Dialing)
    }

    fun endSession() {
        activeEngine = null
    }

    fun emit(event: UssdSessionEvent) {
        _events.tryEmit(event)
    }
}
