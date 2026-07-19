package com.maangatech.gojuagent.core.security

import android.app.Activity
import android.view.WindowManager

/**
 * Blocks screenshots/screen recording and hides content in the recent-apps switcher —
 * applied to every activity in this app (customer numbers, amounts, and USSD reference
 * codes must never end up in a screenshot, a screen recording, or the task switcher
 * thumbnail).
 */
object ScreenSecurity {
    fun apply(activity: Activity) {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE,
        )
    }
}
