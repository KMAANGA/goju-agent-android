package com.maangatech.gojuagent.core.security

import android.os.Build
import java.io.File

/**
 * Heuristic root detection — not a hard sandbox guarantee (nothing on Android is), but
 * enough signal to flag the session as reduced-trust so the app can, per policy, warn the
 * teller and tag transactions performed on a rooted device for back-office review rather
 * than silently trusting the environment.
 */
object RootDetector {

    private val SU_PATHS = listOf(
        "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su",
        "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
        "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su",
    )

    private val ROOT_MANAGEMENT_APPS = listOf(
        "com.noshufou.android.su", "com.koushikdutta.superuser", "eu.chainfire.supersu",
        "com.topjohnwu.magisk",
    )

    fun isProbablyRooted(): Boolean =
        checkSuBinaries() || checkTestKeys() || checkRootManagementApps()

    private fun checkSuBinaries(): Boolean = SU_PATHS.any { File(it).exists() }

    private fun checkTestKeys(): Boolean = Build.TAGS?.contains("test-keys") == true

    private fun checkRootManagementApps(): Boolean =
        ROOT_MANAGEMENT_APPS.any { pkg ->
            runCatching { File("/data/data/$pkg").exists() }.getOrDefault(false)
        }
}
