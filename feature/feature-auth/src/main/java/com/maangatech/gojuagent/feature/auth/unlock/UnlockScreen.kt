package com.maangatech.gojuagent.feature.auth.unlock

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maangatech.gojuagent.core.designsystem.component.PinKeypad
import com.maangatech.gojuagent.core.security.BiometricAuthManager
import com.maangatech.gojuagent.core.security.BiometricResult
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun UnlockScreen(
    agentName: String,
    onUnlocked: () -> Unit,
    biometricAuthManager: BiometricAuthManager,
    viewModel: UnlockViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.unlocked) {
        if (uiState.unlocked) onUnlocked()
    }

    LaunchedEffect(Unit) {
        val fragmentActivity = activity as? androidx.fragment.app.FragmentActivity ?: return@LaunchedEffect
        if (biometricAuthManager.canAuthenticate(fragmentActivity)) {
            biometricAuthManager.authenticate(fragmentActivity, "Unlock GOJU Agent", "Verify it's you, $agentName")
                .onEach { result -> if (result is BiometricResult.Success) viewModel.onBiometricSuccess() }
                .launchIn(this)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Welcome back, $agentName", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Enter your PIN to continue",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(6) { index ->
                val filled = index < uiState.pinInput.length
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(
                            if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        ),
                )
            }
        }

        uiState.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
        }

        Spacer(Modifier.height(32.dp))

        PinKeypad(
            onDigit = viewModel::onDigitPressed,
            onBackspace = viewModel::onBackspace,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        val fragmentActivity = activity as? androidx.fragment.app.FragmentActivity
        if (fragmentActivity != null && biometricAuthManager.canAuthenticate(fragmentActivity)) {
            IconButton(onClick = {
                biometricAuthManager.authenticate(fragmentActivity, "Unlock GOJU Agent", "Verify it's you, $agentName")
                    .onEach { result -> if (result is BiometricResult.Success) viewModel.onBiometricSuccess() }
                    .launchIn(coroutineScope)
            }) {
                Icon(Icons.Filled.Fingerprint, contentDescription = "Use biometric unlock")
            }
        }
    }
}
