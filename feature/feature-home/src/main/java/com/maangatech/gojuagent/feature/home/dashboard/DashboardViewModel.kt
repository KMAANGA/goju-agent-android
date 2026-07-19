package com.maangatech.gojuagent.feature.home.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maangatech.gojuagent.core.database.entity.TransactionStatus
import com.maangatech.gojuagent.core.database.repository.TransactionRepository
import com.maangatech.gojuagent.core.security.SecurePrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DashboardUiState(
    val agentName: String = "",
    val branchName: String = "",
    val todayCount: Int = 0,
    val todaySuccessCount: Int = 0,
    val todayFailedCount: Int = 0,
    val todayTotalMinorUnits: Long = 0,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    securePrefs: SecurePrefs,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = transactionRepository.observeToday()
        .map { today ->
            DashboardUiState(
                agentName = securePrefs.agentUserName ?: "",
                branchName = securePrefs.agentBranchName ?: "",
                todayCount = today.size,
                todaySuccessCount = today.count { it.status == TransactionStatus.SUCCESS },
                todayFailedCount = today.count { it.status == TransactionStatus.FAILED },
                todayTotalMinorUnits = today.filter { it.status == TransactionStatus.SUCCESS }.sumOf { it.amountMinorUnits },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())
}
