package com.maangatech.gojuagent.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.maangatech.gojuagent.core.security.BiometricAuthManager
import com.maangatech.gojuagent.feature.auth.login.LoginScreen
import com.maangatech.gojuagent.feature.auth.pairing.DevicePairingScreen
import com.maangatech.gojuagent.feature.auth.pin.SetPinScreen
import com.maangatech.gojuagent.feature.auth.unlock.UnlockScreen
import com.maangatech.gojuagent.feature.transactions.data.CustomerPrefillHolder
import com.maangatech.gojuagent.feature.transactions.data.SelectedTransactionHolder
import com.maangatech.gojuagent.feature.transactions.execution.TransactionExecutionScreen
import com.maangatech.gojuagent.feature.transactions.form.TransactionFormScreen
import com.maangatech.gojuagent.feature.transactions.history.TransactionDetailScreen
import com.maangatech.gojuagent.feature.transactions.launcher.TransactionLauncherScreen

@Composable
fun AppNavHost(
    appVersion: String,
    selectedTransactionHolder: SelectedTransactionHolder,
    customerPrefillHolder: CustomerPrefillHolder,
    biometricAuthManager: BiometricAuthManager,
    sessionViewModel: SessionViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = sessionViewModel.startRoute) {
        composable(AppRoute.LOGIN) {
            LoginScreen(
                appVersion = appVersion,
                onPendingApproval = { navController.navigate(AppRoute.PAIRING) { popUpTo(AppRoute.LOGIN) { inclusive = true } } },
                onApproved = { navController.navigate(AppRoute.SET_PIN) { popUpTo(AppRoute.LOGIN) { inclusive = true } } },
            )
        }

        composable(AppRoute.PAIRING) {
            DevicePairingScreen(
                onApproved = { navController.navigate(AppRoute.SET_PIN) { popUpTo(AppRoute.PAIRING) { inclusive = true } } },
            )
        }

        composable(AppRoute.SET_PIN) {
            SetPinScreen(
                onCompleted = { navController.navigate(AppRoute.MAIN) { popUpTo(0) { inclusive = true } } },
            )
        }

        composable(AppRoute.UNLOCK) {
            UnlockScreen(
                agentName = sessionViewModel.agentName,
                biometricAuthManager = biometricAuthManager,
                onUnlocked = { navController.navigate(AppRoute.MAIN) { popUpTo(0) { inclusive = true } } },
            )
        }

        composable(AppRoute.MAIN) {
            MainScaffold(
                onNewTransaction = { navController.navigate(AppRoute.TRANSACTION_LAUNCHER) },
                onTransactionSelected = {
                    selectedTransactionHolder.set(it)
                    navController.navigate(AppRoute.TRANSACTION_DETAIL)
                },
                onCustomerSelected = { customer ->
                    customerPrefillHolder.set(customer)
                    val providerCode = customer.lastProviderCode
                    val serviceType = customer.lastServiceType
                    if (providerCode != null && serviceType != null) {
                        navController.navigate(AppRoute.transactionForm(providerCode, serviceType))
                    } else {
                        navController.navigate(AppRoute.TRANSACTION_LAUNCHER)
                    }
                },
            )
        }

        composable(AppRoute.TRANSACTION_LAUNCHER) {
            TransactionLauncherScreen(
                onServiceSelected = { providerCode, serviceType ->
                    navController.navigate(AppRoute.transactionForm(providerCode, serviceType))
                },
            )
        }

        composable(
            route = AppRoute.TRANSACTION_FORM,
            arguments = listOf(
                navArgument("providerCode") { type = NavType.StringType },
                navArgument("serviceType") { type = NavType.StringType },
            ),
        ) {
            TransactionFormScreen(
                onContinueToExecution = { navController.navigate(AppRoute.TRANSACTION_EXECUTION) },
            )
        }

        composable(AppRoute.TRANSACTION_EXECUTION) {
            TransactionExecutionScreen(
                onDone = { navController.navigate(AppRoute.MAIN) { popUpTo(AppRoute.MAIN) { inclusive = true } } },
            )
        }

        composable(AppRoute.TRANSACTION_DETAIL) {
            selectedTransactionHolder.get()?.let { transaction ->
                TransactionDetailScreen(transaction = transaction)
            }
        }
    }
}
