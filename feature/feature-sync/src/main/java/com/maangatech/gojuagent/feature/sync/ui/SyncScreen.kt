package com.maangatech.gojuagent.feature.sync.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maangatech.gojuagent.core.designsystem.component.GojuPrimaryButton

@Composable
fun SyncScreen(viewModel: SyncViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Sync with GOJU Cloud", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Transactions sync automatically every 15 minutes when you have a connection. Use Sync Now if you need it immediately.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SyncStat("Pending", uiState.pendingCount)
            SyncStat("Failed", uiState.failedCount)
            SyncStat("Synced", uiState.syncedTodayCount)
        }

        Column(modifier = Modifier.padding(top = 32.dp)) {
            GojuPrimaryButton(text = "Sync Now", onClick = viewModel::syncNow)
        }
    }
}

@Composable
private fun SyncStat(label: String, value: Int) {
    Column {
        Text(value.toString(), style = MaterialTheme.typography.headlineMedium)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
