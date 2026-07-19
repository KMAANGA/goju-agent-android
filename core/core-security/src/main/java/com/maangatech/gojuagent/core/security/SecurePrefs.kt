package com.maangatech.gojuagent.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for everything that must never sit in plain text: API/session
 * tokens, the paired device identity, and the PIN hash. Backed by AndroidX Security's
 * EncryptedSharedPreferences (AES-256-GCM, key wrapped in the Keystore) — never store
 * secrets in plain SharedPreferences or Room columns.
 */
@Singleton
class SecurePrefs @Inject constructor(@ApplicationContext context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "goju_agent_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    var apiToken: String?
        get() = prefs.getString(KEY_API_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_API_TOKEN, value).apply()

    var deviceToken: String?
        get() = prefs.getString(KEY_DEVICE_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_DEVICE_TOKEN, value).apply()

    /** Only used when `Settings.Secure.ANDROID_ID` is unavailable — see [DeviceIdentifier]. */
    var fallbackDeviceUuid: String?
        get() = prefs.getString(KEY_FALLBACK_DEVICE_UUID, null)
        set(value) = prefs.edit().putString(KEY_FALLBACK_DEVICE_UUID, value).apply()

    var deviceApprovalStatus: String?
        get() = prefs.getString(KEY_DEVICE_STATUS, null)
        set(value) = prefs.edit().putString(KEY_DEVICE_STATUS, value).apply()

    var agentUserId: Long
        get() = prefs.getLong(KEY_AGENT_USER_ID, -1)
        set(value) = prefs.edit().putLong(KEY_AGENT_USER_ID, value).apply()

    var agentUserName: String?
        get() = prefs.getString(KEY_AGENT_USER_NAME, null)
        set(value) = prefs.edit().putString(KEY_AGENT_USER_NAME, value).apply()

    var agentBranchName: String?
        get() = prefs.getString(KEY_AGENT_BRANCH_NAME, null)
        set(value) = prefs.edit().putString(KEY_AGENT_BRANCH_NAME, value).apply()

    var pinHash: String?
        get() = prefs.getString(KEY_PIN_HASH, null)
        set(value) = prefs.edit().putString(KEY_PIN_HASH, value).apply()

    var pinSalt: String?
        get() = prefs.getString(KEY_PIN_SALT, null)
        set(value) = prefs.edit().putString(KEY_PIN_SALT, value).apply()

    var lastActivityAtMillis: Long
        get() = prefs.getLong(KEY_LAST_ACTIVITY, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_ACTIVITY, value).apply()

    val isSignedIn: Boolean get() = !apiToken.isNullOrBlank()
    val isDeviceApproved: Boolean get() = deviceApprovalStatus == "approved"
    val hasPinConfigured: Boolean get() = !pinHash.isNullOrBlank()

    fun clearSession() {
        prefs.edit()
            .remove(KEY_API_TOKEN)
            .remove(KEY_AGENT_USER_ID)
            .remove(KEY_AGENT_USER_NAME)
            .remove(KEY_AGENT_BRANCH_NAME)
            .apply()
    }

    /** Full wipe — used on device revoke or "forget this device". */
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_API_TOKEN = "api_token"
        const val KEY_DEVICE_TOKEN = "device_token"
        const val KEY_FALLBACK_DEVICE_UUID = "fallback_device_uuid"
        const val KEY_DEVICE_STATUS = "device_status"
        const val KEY_AGENT_USER_ID = "agent_user_id"
        const val KEY_AGENT_USER_NAME = "agent_user_name"
        const val KEY_AGENT_BRANCH_NAME = "agent_branch_name"
        const val KEY_PIN_HASH = "pin_hash"
        const val KEY_PIN_SALT = "pin_salt"
        const val KEY_LAST_ACTIVITY = "last_activity_at"
    }
}
