package com.maangatech.gojuagent

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.maangatech.gojuagent.core.designsystem.theme.GojuAgentTheme
import com.maangatech.gojuagent.core.security.BiometricAuthManager
import com.maangatech.gojuagent.core.security.ScreenSecurity
import com.maangatech.gojuagent.core.security.SessionGuard
import com.maangatech.gojuagent.feature.transactions.data.CustomerPrefillHolder
import com.maangatech.gojuagent.feature.transactions.data.SelectedTransactionHolder
import com.maangatech.gojuagent.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Uses [FragmentActivity] (not plain [ComponentActivity]) because [BiometricAuthManager]
 * needs a FragmentActivity host to show the system biometric prompt.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var sessionGuard: SessionGuard
    @Inject lateinit var selectedTransactionHolder: SelectedTransactionHolder
    @Inject lateinit var customerPrefillHolder: CustomerPrefillHolder
    @Inject lateinit var biometricAuthManager: BiometricAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScreenSecurity.apply(this)

        setContent {
            GojuAgentTheme {
                AppNavHost(
                    appVersion = BuildConfig.VERSION_NAME,
                    selectedTransactionHolder = selectedTransactionHolder,
                    customerPrefillHolder = customerPrefillHolder,
                    biometricAuthManager = biometricAuthManager,
                )
            }
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        sessionGuard.recordActivity()
    }

    override fun onPause() {
        super.onPause()
        sessionGuard.recordActivity()
    }
}
