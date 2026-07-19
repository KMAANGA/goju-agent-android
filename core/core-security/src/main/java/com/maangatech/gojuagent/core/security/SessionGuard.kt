package com.maangatech.gojuagent.core.security

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Auto-lock timer. [recordActivity] is called from a single point at the navigation root
 * (any user interaction), and [isSessionExpired] is checked on process resume — if expired,
 * the app routes to the unlock screen instead of the last-visible content, so a device left
 * unattended at the counter doesn't stay open to a live transaction form.
 */
@Singleton
class SessionGuard @Inject constructor(private val securePrefs: SecurePrefs) {

    var timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS

    fun recordActivity() {
        securePrefs.lastActivityAtMillis = System.currentTimeMillis()
    }

    fun isSessionExpired(): Boolean {
        val last = securePrefs.lastActivityAtMillis
        if (last == 0L) return false
        return System.currentTimeMillis() - last > timeoutMillis
    }

    companion object {
        const val DEFAULT_TIMEOUT_MILLIS = 2 * 60 * 1000L // 2 minutes idle at a busy till
    }
}
