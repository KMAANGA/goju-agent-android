package com.maangatech.gojuagent.core.security

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stable per-install device identity used for device-pairing/approval with GOJU Cloud.
 * `ANDROID_ID` is scoped per app-signing-key/user/device since Android 8, which is exactly
 * the granularity device-binding needs; a random UUID (persisted in [SecurePrefs], which
 * survives app updates but not reinstalls) backstops the rare case `ANDROID_ID` is blank.
 */
@Singleton
class DeviceIdentifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securePrefs: SecurePrefs,
) {

    @SuppressLint("HardwareIds")
    fun stableDeviceId(): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
            return "and_$androidId"
        }

        var fallback = securePrefs.fallbackDeviceUuid
        if (fallback == null) {
            fallback = "fallback_${UUID.randomUUID()}"
            securePrefs.fallbackDeviceUuid = fallback
        }
        return fallback
    }

    fun deviceName(): String = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"

    fun osVersion(): String = "Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})"
}
