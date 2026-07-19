package com.maangatech.gojuagent.feature.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maangatech.gojuagent.core.database.entity.CustomerEntity
import com.maangatech.gojuagent.core.database.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CustomerListTab { RECENT, FAVORITES, SEARCH }

data class CustomerListUiState(
    val tab: CustomerListTab = CustomerListTab.RECENT,
    val query: String = "",
    val customers: List<CustomerEntity> = emptyList(),
)

@HiltViewModel
class CustomerListViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
) : ViewModel() {

    private val tab = MutableStateFlow(CustomerListTab.RECENT)
    private val query = MutableStateFlow("")

    val uiState: StateFlow<CustomerListUiState> = combine(tab, query) { t, q -> t to q }
        .flatMapLatest { (t, q) ->
            val source = when (t) {
                CustomerListTab.RECENT -> customerRepository.observeRecent()
                CustomerListTab.FAVORITES -> customerRepository.observeFavorites()
                CustomerListTab.SEARCH -> customerRepository.search(q)
            }
            source.map { customers -> CustomerListUiState(tab = t, query = q, customers = customers) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CustomerListUiState())

    fun onTabSelected(newTab: CustomerListTab) {
        tab.value = newTab
    }

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
        if (newQuery.isNotBlank()) tab.value = CustomerListTab.SEARCH
    }

    fun toggleFavorite(customer: CustomerEntity) {
        viewModelScope.launch {
            customerRepository.setFavorite(customer, !customer.isFavorite)
        }
    }
}
