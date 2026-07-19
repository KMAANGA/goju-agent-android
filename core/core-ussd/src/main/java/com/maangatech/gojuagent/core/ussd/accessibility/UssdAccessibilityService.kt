package com.maangatech.gojuagent.core.ussd.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import com.maangatech.gojuagent.core.ussd.UssdSessionEventBus
import com.maangatech.gojuagent.core.ussd.engine.EngineDecision
import com.maangatech.gojuagent.core.ussd.model.TerminalOutcome
import com.maangatech.gojuagent.core.ussd.model.UssdSessionEvent
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ref.WeakReference
import javax.inject.Inject

/**
 * Watches the phone app's USSD-response dialog and drives it through the active
 * [com.maangatech.gojuagent.core.ussd.engine.WorkflowSessionEngine] one screen at a time.
 *
 * Requires the teller to manually grant this in system Accessibility settings — there is no
 * programmatic grant path, and the app must never claim otherwise in its onboarding copy.
 * When no session is active ([UssdSessionEventBus.activeEngine] is null) every event is
 * ignored — this service does not "listen in" on ordinary phone calls or any other app.
 */
@AndroidEntryPoint
class UssdAccessibilityService : AccessibilityService() {

    @Inject lateinit var eventBus: UssdSessionEventBus
    @Inject lateinit var dialerPackageRegistry: DialerPackageRegistry

    private var lastHandledText: String? = null

    override fun onServiceConnected() {
        instanceRef = WeakReference(this)
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
            // Package filtering is done manually in onAccessibilityEvent (see
            // DialerPackageRegistry) rather than via `packageNames` here, so the allowlist
            // can be extended at runtime without re-binding the service.
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val engine = eventBus.activeEngine ?: return
        val packageName = event.packageName?.toString() ?: return
        if (packageName !in dialerPackageRegistry.allowedPackages()) return

        val root = rootInActiveWindow ?: return
        val dialogText = try {
            UssdDialogInteractor.extractDialogText(root)
        } finally {
            // root is owned by the system; recycling it here would be incorrect on API < 33
            // since other callbacks may still reference it — leave lifecycle to the framework.
        }

        if (dialogText.isBlank() || dialogText == lastHandledText) return
        lastHandledText = dialogText
        eventBus.emit(UssdSessionEvent.ScreenReceived(dialogText))

        when (val decision = engine.onDialogText(dialogText)) {
            is EngineDecision.SendInput -> {
                val sent = UssdDialogInteractor.submitInput(root, decision.text)
                if (sent) {
                    eventBus.emit(UssdSessionEvent.InputSent(decision.stepId))
                } else {
                    finishSession(
                        UssdSessionEvent.EngineError(
                            "Could not find an input field to continue the USSD session (step ${decision.stepId}).",
                        ),
                    )
                }
            }

            is EngineDecision.Terminal -> {
                finishSession(
                    if (decision.outcome == TerminalOutcome.SUCCESS) {
                        UssdSessionEvent.Succeeded(decision.referenceCode, dialogText)
                    } else {
                        UssdSessionEvent.Failed(decision.failureReason ?: "Transaction failed.", dialogText)
                    },
                )
            }

            is EngineDecision.AwaitSecureInput -> {
                // Deliberately do not touch the dialog node here — see EngineDecision docs.
                // lastHandledText is left set to the current screen so this branch doesn't
                // re-fire on the next no-op content-changed event for the same screen.
                eventBus.emit(UssdSessionEvent.SecureInputRequired(decision.stepId, decision.promptLabel))
            }

            EngineDecision.Unrecognized -> {
                // Not every content-changed event is a new menu screen (e.g. a spinner tick);
                // only escalate to a hard failure once the teller-visible timeout in
                // UssdSessionCoordinator fires, not on the first unmatched event here.
            }
        }
    }

    /** Resumes a session paused on [EngineDecision.AwaitSecureInput] with what the teller just typed. */
    private fun submitSecureInput(stepId: String, secureValue: String) {
        val engine = eventBus.activeEngine ?: return
        val root = rootInActiveWindow ?: run {
            finishSession(UssdSessionEvent.EngineError("The USSD dialog closed before the PIN could be submitted."))
            return
        }

        val decision = engine.submitSecureInput(stepId, secureValue)
        if (decision is EngineDecision.SendInput) {
            val sent = UssdDialogInteractor.submitInput(root, decision.text)
            if (sent) {
                eventBus.emit(UssdSessionEvent.InputSent(decision.stepId))
            } else {
                finishSession(UssdSessionEvent.EngineError("Could not submit the PIN — input field not found."))
            }
        }
    }

    private fun cancelActiveSession() {
        rootInActiveWindow?.let { UssdDialogInteractor.cancelDialog(it) }
        finishSession(UssdSessionEvent.Cancelled)
    }

    private fun finishSession(event: UssdSessionEvent) {
        lastHandledText = null
        eventBus.endSession()
        eventBus.emit(event)
    }

    override fun onInterrupt() {
        lastHandledText = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instanceRef?.get() === this) instanceRef = null
    }

    companion object {
        private var instanceRef: WeakReference<UssdAccessibilityService>? = null

        /** Called by [com.maangatech.gojuagent.core.ussd.UssdSessionCoordinator]'s Cancel action. */
        fun requestCancel() {
            instanceRef?.get()?.cancelActiveSession()
        }

        /** Called by [com.maangatech.gojuagent.core.ussd.UssdSessionCoordinator] once the teller enters the requested PIN. */
        fun requestSubmitSecureInput(stepId: String, secureValue: String) {
            instanceRef?.get()?.submitSecureInput(stepId, secureValue)
        }

        fun isEnabled(context: android.content.Context): Boolean {
            val enabledServices = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            ) ?: return false
            val expected = android.content.ComponentName(context, UssdAccessibilityService::class.java)
                .flattenToString()
            return enabledServices.split(':').any { it.equals(expected, ignoreCase = true) }
        }
    }
}
