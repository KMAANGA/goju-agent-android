package com.maangatech.gojuagent.core.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import java.security.MessageDigest

/**
 * Confirms the running APK was signed with GOJU's release key. A resigned/repackaged copy
 * of this app (a realistic risk for a sideloaded financial tool) will fail this check —
 * callers should treat a mismatch as fatal (refuse to unlock / wipe secure prefs), not just
 * a warning, since a resigned APK could have modified USSD/sync logic.
 *
 * [expectedSignatureSha256] is baked in per build flavor (debug builds skip verification —
 * see [isDebuggable]) and must be replaced with the real release cert's SHA-256 fingerprint
 * before this app is distributed to agents.
 */
object TamperDetector {

    fun isSignatureValid(context: Context, expectedSignatureSha256: String): Boolean {
        if (isDebuggable(context)) return true
        val actual = currentSignatureSha256(context) ?: return false
        return actual.equals(expectedSignatureSha256, ignoreCase = true)
    }

    private fun isDebuggable(context: Context): Boolean =
        (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0

    @Suppress("DEPRECATION")
    private fun currentSignatureSha256(context: Context): String? = runCatching {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES,
            )
        } else {
            context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
        }

        val signature = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.signingInfo?.apkContentsSigners?.firstOrNull()
        } else {
            packageInfo.signatures?.firstOrNull()
        } ?: return null

        val digest = MessageDigest.getInstance("SHA-256").digest(signature.toByteArray())
        digest.joinToString(":") { "%02X".format(it) }
    }.getOrNull()

    fun versionCode(context: Context): Long = runCatching {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        PackageInfoCompat.getLongVersionCode(info)
    }.getOrDefault(-1)
}
