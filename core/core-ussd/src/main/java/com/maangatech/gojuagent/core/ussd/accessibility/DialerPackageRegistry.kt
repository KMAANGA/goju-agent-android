package com.maangatech.gojuagent.core.ussd.accessibility

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The set of package names whose windows the [UssdAccessibilityService] is allowed to read
 * and act on — i.e. "what does the phone/dialer app look like on this OEM's Android skin".
 *
 * This list is a best-effort starting point, NOT a verified guarantee — OEM package names
 * for the in-call/USSD-response UI drift across Android versions and regional builds, and
 * budget devices common with agents (itel/Tecno/Infinix, all built on Transsion's HiOS/
 * Itel-OS) are the least documented. Per the rollout plan, every entry here must be
 * confirmed on a real device before that OEM is declared supported, and the list stays
 * user-extensible at runtime (see [addPackage]) precisely so a field agent's unrecognized
 * device doesn't require an app update to fix.
 */
@Singleton
class DialerPackageRegistry @Inject constructor(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ussd_dialer_packages", Context.MODE_PRIVATE)

    private val defaults = setOf(
        "com.android.phone",              // AOSP / stock — hosts the USSD response AlertDialog on most devices
        "com.google.android.dialer",       // Pixel / Android One
        "com.android.dialer",
        "com.samsung.android.dialer",
        "com.samsung.android.incallui",
        "com.samsung.android.contacts",
        "com.miui.contacts",               // Xiaomi/MIUI/HyperOS
        "com.android.contacts",
        "com.transsion.phonemanager",      // itel / Tecno / Infinix (HiOS / iOS-based on AOSP)
        "com.transsion.contacts",
        "com.coloros.dialer",              // OPPO / ColorOS
        "com.oppo.dialer",
        "com.vivo.dialer",                 // vivo / FuntouchOS
        "com.huawei.contacts",
    )

    fun allowedPackages(): Set<String> =
        defaults + (prefs.getStringSet(KEY_EXTRA_PACKAGES, emptySet()) ?: emptySet())

    fun addPackage(packageName: String) {
        val current = prefs.getStringSet(KEY_EXTRA_PACKAGES, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(packageName)
        prefs.edit().putStringSet(KEY_EXTRA_PACKAGES, current).apply()
    }

    private companion object {
        const val KEY_EXTRA_PACKAGES = "extra_packages"
    }
}
