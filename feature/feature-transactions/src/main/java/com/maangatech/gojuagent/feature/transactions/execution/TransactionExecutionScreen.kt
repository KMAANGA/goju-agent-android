package com.maangatech.gojuagent.feature.transactions.execution

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maangatech.gojuagent.core.designsystem.component.GojuPrimaryButton
import com.maangatech.gojuagent.core.designsystem.component.GojuSecondaryButton
import com.maangatech.gojuagent.core.designsystem.component.PinKeypad
import com.maangatech.gojuagent.core.ussd.accessibility.UssdAccessibilityService

@Composable
fun TransactionExecutionScreen(
    onDone: () -> Unit,
    viewModel: TransactionExecutionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.start(UssdAccessibilityService.isEnabled(context))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        when (uiState.phase) {
            ExecutionPhase.ACCESSIBILITY_REQUIRED -> AccessibilityRequiredContent(
                onOpenSettings = {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                },
                onIveEnabledIt = { viewModel.onAccessibilityEnabled() },
            )

            ExecutionPhase.PREPARING, ExecutionPhase.DIALING, ExecutionPhase.IN_PROGRESS -> InProgressContent(
                phase = uiState.phase,
                lastScreenText = uiState.lastScreenText,
                onCancel = viewModel::cancelTransaction,
            )

            ExecutionPhase.AWAITING_PIN -> PinPromptContent(
                promptLabel = uiState.pinPromptLabel ?: "Enter your PIN",
                onSubmit = viewModel::submitPin,
                onCancel = viewModel::cancelTransaction,
            )

            ExecutionPhase.SUCCEEDED -> ResultContent(
                success = true,
                title = "Transaction Successful",
                message = uiState.resultReference?.let { "Reference: $it" } ?: uiState.resultMessage,
                onDone = onDone,
            )

            ExecutionPhase.FAILED, ExecutionPhase.CANCELLED -> ResultContent(
                success = false,
                title = if (uiState.phase == ExecutionPhase.CANCELLED) "Transaction Cancelled" else "Transaction Failed",
                message = uiState.resultMessage,
                onDone = onDone,
            )
        }
    }
}

@Composable
private fun AccessibilityRequiredContent(onOpenSettings: () -> Unit, onIveEnabledIt: () -> Unit) {
    Text("One-time setup needed", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
    Text(
        "GOJU Agent needs the \"GOJU Agent — USSD Automation\" Accessibility Service turned on to run transactions automatically. This is a one-time step per device.",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
    )
    GojuPrimaryButton(text = "Open Accessibility Settings", onClick = onOpenSettings)
    Spacer(Modifier.height(12.dp))
    GojuSecondaryButton(text = "I've enabled it — Continue", onClick = onIveEnabledIt)
}

@Composable
private fun InProgressContent(phase: ExecutionPhase, lastScreenText: String?, onCancel: () -> Unit) {
    CircularProgressIndicator(modifier = Modifier.size(48.dp))
    Text(
        when (phase) {
            ExecutionPhase.PREPARING -> "Preparing transaction…"
            ExecutionPhase.DIALING -> "Dialing…"
            else -> "Processing…"
        },
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(top = 24.dp),
    )
    lastScreenText?.let {
        Text(
            it,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
    Spacer(Modifier.height(32.dp))
    GojuSecondaryButton(text = "Cancel", onClick = onCancel)
}

@Composable
private fun PinPromptContent(promptLabel: String, onSubmit: (String) -> Unit, onCancel: () -> Unit) {
    var pin by remember { mutableStateOf("") }

    Text(promptLabel, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
    Text(
        "This is sent directly to the provider — GOJU Agent does not store it.",
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
    )

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(6) { index ->
            Text(if (index < pin.length) "•" else "○", style = MaterialTheme.typography.headlineMedium)
        }
    }

    Spacer(Modifier.height(24.dp))

    PinKeypad(
        onDigit = { digit -> if (pin.length < 6) pin += digit },
        onBackspace = { pin = pin.dropLast(1) },
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(Modifier.height(16.dp))
    GojuPrimaryButton(text = "Submit", enabled = pin.length >= 4, onClick = {
        onSubmit(pin)
        pin = ""
    })
    Spacer(Modifier.height(8.dp))
    GojuSecondaryButton(text = "Cancel", onClick = onCancel)
}

@Composable
private fun ResultContent(success: Boolean, title: String, message: String?, onDone: () -> Unit) {
    Icon(
        if (success) Icons.Filled.CheckCircle else Icons.Filled.Error,
        contentDescription = null,
        tint = if (success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        modifier = Modifier.size(64.dp),
    )
    Text(title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 16.dp))
    message?.let {
        Text(it, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
    }
    Spacer(Modifier.height(32.dp))
    GojuPrimaryButton(text = "Done", onClick = onDone)
}
