package com.maangatech.gojuagent.feature.transactions.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maangatech.gojuagent.core.database.entity.TransactionEntity
import com.maangatech.gojuagent.core.database.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HistoryUiState(val query: String = "", val transactions: List<TransactionEntity> = emptyList())

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionHistoryViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")

    val uiState: StateFlow<HistoryUiState> = query
        .flatMapLatest { q -> transactionRepository.search(q.ifBlank { null }).map { HistoryUiState(q, it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }
}
