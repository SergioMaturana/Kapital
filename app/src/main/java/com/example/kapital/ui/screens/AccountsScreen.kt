package com.example.kapital.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.kapital.model.Account
import com.example.kapital.viewmodel.MainViewModel
import com.example.kapital.ui.navigation.Screen
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun AccountsScreen(
    navController: NavHostController,
    viewModel: MainViewModel = viewModel(),
    onAccountClick: (Int) -> Unit
) {
    val accounts = viewModel.accounts.collectAsState().value
    var showGlobalTransactions by remember { mutableStateOf(false) }
    var isBalanceVisible by remember { mutableStateOf(true) }

    // Variables de edición de cuenta
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedAccountForEdit by remember { mutableStateOf<Account?>(null) }
    var newName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    val totalBalance = accounts.sumOf { account ->
        viewModel.getAccountBalance(account.id)
    }
    var selectedAccountId by remember { mutableStateOf<Int?>(null) }

    // Recargar nombre cuando cambie la cuenta seleccionada
    LaunchedEffect(selectedAccountId) {
        selectedAccountId?.let {
            val account = accounts.find { acc -> acc.id == it }
            newName = account?.name ?: ""
        }
    }

    if (showGlobalTransactions) {
        AllAccountsTransactions(
            viewModel = viewModel,
            onBack = { showGlobalTransactions = false }
        )
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center, // ¡Cambiado a Arrangement.Center!
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cuentas",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp) // Espacio opcional entre ícono y texto
                    )
                }

                // Mostrar el balance total centrado
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isBalanceVisible = !isBalanceVisible },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = if (isBalanceVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isBalanceVisible) "Ocultar balance" else "Mostrar balance",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = " ${if (isBalanceVisible) formatCurrency(totalBalance) else "*****"}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(start = 8.dp) // Espaciado entre ícono y texto
                    )
                }
            }
            // Item: Botón para transacciones globales
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { showGlobalTransactions = true },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {

                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Ver transacciones globales",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            items(accounts) { account ->
                val balance = viewModel.getAccountBalance(account.id)

                // Mostrar AccountItem con edición
                AccountItem(
                    account = account,
                    balance = balance,
                    isBalanceVisible = isBalanceVisible,
                    onClick = { onAccountClick(account.id) },
                    onDelete = { viewModel.deleteAccount(account.id) },
                    onEdit = { accountId ->
                        selectedAccountId = accountId
                        showEditDialog = true
                    }
                )

                // Diálogo de edición (dentro del item para acceso directo a 'account')
                if (showEditDialog && selectedAccountId == account.id) {
                    var tempName by remember { mutableStateOf(account.name) }
                    var tempNameError by remember { mutableStateOf(false) }

                    AlertDialog(
                        onDismissRequest = {
                            showEditDialog = false
                            selectedAccountId = null
                        },
                        title = { Text("Editar nombre de la cuenta") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = tempName,
                                    onValueChange = {
                                        tempName = it
                                        tempNameError = false
                                    },
                                    label = { Text("Nombre") },
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = tempNameError
                                )
                                if (tempNameError) {
                                    Text(
                                        text = "El nombre no puede estar vacío",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (tempName.isNotBlank()) {
                                        viewModel.updateAccount(account.copy(name = tempName))
                                        showEditDialog = false
                                        selectedAccountId = null
                                    } else {
                                        tempNameError = true
                                    }
                                }
                            ) {
                                Text("Guardar")
                            }
                        },
                        dismissButton = {
                            Button(onClick = {
                                showEditDialog = false
                                selectedAccountId = null
                            }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
            }

            // Item: Botón para añadir cuenta
            item {
                Button(
                    onClick = { navController.navigate(Screen.AddAccount.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Añadir Cuenta")
                }
            }
        }
    }
}

@Composable
fun AccountItem(
    account: Account,
    balance: Double,
    isBalanceVisible: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onEdit: (Int) -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = account.name)
                Text(text = " ${if (isBalanceVisible) formatCurrency(balance) else "****"}")
            }
            Row {
                // Botón de edición que pasa el ID
                IconButton(onClick = { onEdit(account.id) }) {
                    Icon(Icons.Default.Edit, "Editar")
                }
                IconButton(onClick = { showDeleteConfirmation = true }) {
                    Icon(Icons.Default.Delete, "Eliminar")
                }
            }
        }

        // Diálogo de eliminación (opcional aquí también)
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Confirmar eliminación") },
                text = { Text("¿Estás seguro de eliminar esta cuenta?") },
                confirmButton = {
                    Button(
                        onClick = {
                            onDelete()
                            showDeleteConfirmation = false
                        }
                    ) {
                        Text("Sí, eliminar")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDeleteConfirmation = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

fun formatCurrency(amount: Double): String {
    val formatter = DecimalFormat("###,###,##0.00")
    return "${formatter.format(amount)} €"
}