package com.maangatech.gojuagent.navigation

import androidx.lifecycle.ViewModel
import com.maangatech.gojuagent.core.security.SecurePrefs
import com.maangatech.gojuagent.core.security.SessionGuard
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Decides which screen the app opens to — computed once at process start, not observed reactively. */
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val securePrefs: SecurePrefs,
    private val sessionGuard: SessionGuard,
) : ViewModel() {

    val agentName: String = securePrefs.agentUserName ?: "Agent"

    // `isSignedIn` (has an API token) only becomes true once the device is actually approved
    // — see AuthRepository.login/pollDeviceStatus — so a still-pending device must be
    // detected from deviceApprovalStatus directly, not treated as "never logged in".
    val startRoute: String = when {
        securePrefs.isSignedIn && !securePrefs.hasPinConfigured -> AppRoute.SET_PIN
        securePrefs.isSignedIn && sessionGuard.isSessionExpired() -> AppRoute.UNLOCK
        securePrefs.isSignedIn -> AppRoute.MAIN
        securePrefs.deviceApprovalStatus == "pending" -> AppRoute.PAIRING
        else -> AppRoute.LOGIN
    }
}
