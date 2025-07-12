package com.example.kapital.ui.screens

import TransactionsList
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kapital.viewmodel.MainViewModel

@Composable
fun AccountDetailScreen(accountId: Int, viewModel: MainViewModel, onBack: () -> Unit) {
    val account = viewModel.accounts.collectAsState().value.find { it.id == accountId } ?: return

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Encabezado con el nombre de la cuenta
        Text(text = account.name, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)

        // Balance de la cuenta
        Text(
            text = "Balance: ${formatCurrency(viewModel.getAccountBalance(accountId))}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de transacciones
            TransactionsList(accountId = accountId, viewModel = viewModel, onBack = onBack )
    }
}