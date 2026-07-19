package com.maangatech.gojuagent.feature.sync.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maangatech.gojuagent.core.database.entity.TransactionSyncStatus
import com.maangatech.gojuagent.core.database.repository.TransactionRepository
import com.maangatech.gojuagent.feature.sync.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SyncUiState(val pendingCount: Int = 0, val failedCount: Int = 0, val syncedTodayCount: Int = 0)

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncScheduler: SyncScheduler,
    transactionRepository: TransactionRepository,
) : ViewModel() {

    val uiState: StateFlow<SyncUiState> = transactionRepository.observeAll()
        .map { transactions ->
            SyncUiState(
                pendingCount = transactions.count { it.syncStatus == TransactionSyncStatus.PENDING_SYNC },
                failedCount = transactions.count { it.syncStatus == TransactionSyncStatus.SYNC_FAILED },
                syncedTodayCount = transactions.count { it.syncStatus == TransactionSyncStatus.SYNCED },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncUiState())

    fun syncNow() {
        syncScheduler.syncNow()
    }
}
