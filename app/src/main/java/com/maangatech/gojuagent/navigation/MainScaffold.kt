package com.maangatech.gojuagent.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.maangatech.gojuagent.core.database.entity.TransactionEntity
import com.maangatech.gojuagent.feature.customers.CustomerListScreen
import com.maangatech.gojuagent.feature.home.dashboard.DashboardScreen
import com.maangatech.gojuagent.feature.home.navigation.BottomNavItem
import com.maangatech.gojuagent.feature.home.navigation.GojuBottomBar
import com.maangatech.gojuagent.feature.sync.ui.SyncScreen
import com.maangatech.gojuagent.feature.transactions.history.TransactionHistoryScreen

/** The tabbed shell behind the bottom bar — everything reachable without leaving "home". */
@Composable
fun MainScaffold(
    onNewTransaction: () -> Unit,
    onTransactionSelected: (TransactionEntity) -> Unit,
    onCustomerSelected: (com.maangatech.gojuagent.core.database.entity.CustomerEntity) -> Unit,
) {
    val tabNavController = rememberNavController()
    val backStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            GojuBottomBar(currentRoute = currentRoute) { item ->
                tabNavController.navigate(item.route) {
                    popUpTo(tabNavController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = tabNavController,
            startDestination = BottomNavItem.DASHBOARD.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(BottomNavItem.DASHBOARD.route) {
                DashboardScreen(onNewTransaction = onNewTransaction)
            }
            composable(BottomNavItem.CUSTOMERS.route) {
                CustomerListScreen(onCustomerSelected = onCustomerSelected)
            }
            composable(BottomNavItem.HISTORY.route) {
                TransactionHistoryScreen(onTransactionSelected = onTransactionSelected)
            }
            composable(BottomNavItem.SYNC.route) {
                SyncScreen()
            }
        }
    }
}
