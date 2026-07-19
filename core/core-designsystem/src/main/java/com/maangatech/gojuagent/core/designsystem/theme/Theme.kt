package com.maangatech.gojuagent.core.designsystem.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val GojuLightColorScheme = lightColorScheme(
    primary = GojuIndigo40,
    onPrimary = GojuNeutral99,
    primaryContainer = GojuIndigo90,
    onPrimaryContainer = GojuIndigo10,
    secondary = GojuTeal40,
    onSecondary = GojuNeutral99,
    secondaryContainer = GojuTeal90,
    onSecondaryContainer = GojuIndigo10,
    background = GojuNeutral99,
    onBackground = GojuNeutral10,
    surface = GojuNeutral99,
    onSurface = GojuNeutral10,
    surfaceVariant = GojuNeutral95,
    error = GojuError,
    errorContainer = GojuErrorContainer,
)

private val GojuDarkColorScheme = darkColorScheme(
    primary = GojuIndigo80,
    onPrimary = GojuIndigo10,
    primaryContainer = GojuIndigo20,
    onPrimaryContainer = GojuIndigo90,
    secondary = GojuTeal80,
    onSecondary = GojuIndigo10,
    secondaryContainer = GojuIndigo20,
    onSecondaryContainer = GojuTeal90,
    background = GojuNeutral10,
    onBackground = GojuNeutral90,
    surface = GojuNeutral20,
    onSurface = GojuNeutral90,
    surfaceVariant = GojuNeutral20,
    error = GojuErrorContainer,
    errorContainer = GojuError,
)

/**
 * Root theme. Deliberately does NOT use Android 12+ dynamic color — a wallpaper-derived
 * palette would drift from the GOJU brand identity across agents' personal devices.
 */
@Composable
fun GojuAgentTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) GojuDarkColorScheme else GojuLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GojuTypography,
        content = content,
    )
}
