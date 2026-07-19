package com.maangatech.gojuagent.core.security

import android.os.Build

/** Flags obvious emulator/CI fingerprints. Same posture as [RootDetector]: signal, not proof. */
object EmulatorDetector {

    fun isProbablyEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT ?: ""
        val model = Build.MODEL ?: ""
        val manufacturer = Build.MANUFACTURER ?: ""
        val brand = Build.BRAND ?: ""
        val device = Build.DEVICE ?: ""
        val product = Build.PRODUCT ?: ""
        val hardware = Build.HARDWARE ?: ""

        return fingerprint.startsWith("generic") ||
            fingerprint.startsWith("unknown") ||
            model.contains("google_sdk") ||
            model.contains("Emulator") ||
            model.contains("Android SDK built for x86") ||
            manufacturer.contains("Genymotion") ||
            (brand.startsWith("generic") && device.startsWith("generic")) ||
            product == "google_sdk" ||
            hardware.contains("goldfish") ||
            hardware.contains("ranchu") ||
            Build.PRODUCT.contains("sdk_gphone")
    }
}
