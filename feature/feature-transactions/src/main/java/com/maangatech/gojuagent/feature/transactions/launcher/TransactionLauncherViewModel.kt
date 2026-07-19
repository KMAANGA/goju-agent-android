package com.maangatech.gojuagent.feature.transactions.launcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maangatech.gojuagent.core.database.dao.ProviderSummary
import com.maangatech.gojuagent.feature.transactions.data.ServiceCatalog
import com.maangatech.gojuagent.feature.transactions.data.ServiceDefinition
import com.maangatech.gojuagent.feature.transactions.data.WorkflowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class LauncherUiState(
    val providers: List<ProviderSummary> = emptyList(),
    val services: List<ServiceDefinition> = ServiceCatalog.MPESA_SERVICES,
)

@HiltViewModel
class TransactionLauncherViewModel @Inject constructor(
    workflowRepository: WorkflowRepository,
) : ViewModel() {

    val uiState: StateFlow<LauncherUiState> = workflowRepository.observeActiveProviders()
        .map { providers -> LauncherUiState(providers = providers) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LauncherUiState())
}
