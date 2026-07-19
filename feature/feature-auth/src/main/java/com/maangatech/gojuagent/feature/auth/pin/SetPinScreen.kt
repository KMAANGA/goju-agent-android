package com.maangatech.gojuagent.feature.auth.pin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maangatech.gojuagent.core.designsystem.component.GojuSecondaryButton
import com.maangatech.gojuagent.core.designsystem.component.PinKeypad

@Composable
fun SetPinScreen(
    onCompleted: () -> Unit,
    viewModel: SetPinViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.completed) {
        if (uiState.completed) onCompleted()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            if (uiState.stage == com.maangatech.gojuagent.feature.auth.pin.SetPinUiState.Stage.ENTER) {
                "Create a PIN"
            } else {
                "Confirm your PIN"
            },
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            "You'll use this PIN to unlock GOJU Agent when offline or when biometrics aren't available.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
        )

        PinDots(length = uiState.currentInput.length, maxLength = 6)

        uiState.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
        }

        Spacer(Modifier.height(32.dp))

        PinKeypad(
            onDigit = viewModel::onDigitPressed,
            onBackspace = viewModel::onBackspace,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp))

        GojuSecondaryButton(text = "Continue", onClick = viewModel::onContinue)
    }
}

@Composable
private fun PinDots(length: Int, maxLength: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(maxLength) { index ->
            val filled = index < length
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(
                        if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    ),
            )
        }
    }
}
