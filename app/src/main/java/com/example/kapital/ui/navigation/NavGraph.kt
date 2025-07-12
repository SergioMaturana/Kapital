package com.example.kapital.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.kapital.ui.screens.AccountDetailScreen
import com.example.kapital.ui.screens.AccountsScreen
import com.example.kapital.ui.screens.AddAccountScreen
import com.example.kapital.viewmodel.MainViewModel

sealed class Screen(val route: String) {
    object Accounts : Screen("accounts")
    object AccountDetail : Screen("accountDetail/{accountId}") {
        fun createRoute(accountId: Int) = "accountDetail/$accountId"
    }
    object AddAccount : Screen("add_account")
}

@Composable
fun NavGraph(navController: NavHostController, viewModel: MainViewModel, onBack: () -> Unit) {
    NavHost(navController = navController, startDestination = Screen.Accounts.route, modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        composable(Screen.Accounts.route) {
            AccountsScreen(
                navController = navController, // ¡Importante!
                viewModel = viewModel,
                onAccountClick = { accountId ->
                    navController.navigate(Screen.AccountDetail.createRoute(accountId))
                }
            )
        }
        composable(Screen.AccountDetail.route) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId")?.toIntOrNull() ?: return@composable
            AccountDetailScreen(accountId = accountId, viewModel = viewModel, onBack = onBack)
        }
        composable(Screen.AddAccount.route) {
            AddAccountScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}
