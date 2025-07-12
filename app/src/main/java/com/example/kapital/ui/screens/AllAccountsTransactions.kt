package com.example.kapital.ui.screens

import ExpandableCategoryItem
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kapital.model.Category
import com.example.kapital.model.Transaction
import com.example.kapital.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun AllAccountsTransactions(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val accounts = viewModel.accounts.collectAsState().value
    var showIncomes by remember { mutableStateOf(true) }
    var timeFilter by remember { mutableStateOf(TimeFilter.ALL) }
    var expandedCategory: Category? by remember { mutableStateOf(null) }
    var isBalanceVisible by remember { mutableStateOf(true) }

    // Calcular el balance total de todas las cuentas
    val totalBalance = accounts.sumOf { account ->
        viewModel.getAccountBalance(account.id)
    }

    // Obtener TODAS las transacciones de TODAS las cuentas
    val allTransactions = remember(accounts) {
        accounts.flatMap { account -> account.transactions }
    }

    // Filtrar por tipo y rango de fecha
    val filteredTransactions = remember(allTransactions, showIncomes, timeFilter) {
        val filtered = if (showIncomes) {
            allTransactions.filter { it.isIncome }
        } else {
            allTransactions.filter { !it.isIncome }
        }
        filtered.filter { transaction ->
            when (timeFilter) {
                TimeFilter.Dia -> isSameDay(transaction.date, Date())
                TimeFilter.Semana -> isSameWeek(transaction.date, Date())
                TimeFilter.Mes -> isSameMonth(transaction.date, Date())
                TimeFilter.Anio -> isSameYear(transaction.date, Date())
                TimeFilter.ALL -> true
            }
        }
    }

    // Agrupar por categoría y sumar montos
    val groupedTransactions = remember(filteredTransactions) {
        filteredTransactions
            .groupBy { it.category }
            .map { (category, transactions) ->
                Pair(category, transactions.sumOf { it.amount })
            }
    }

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var selectedTransactionToDelete: Transaction? by remember { mutableStateOf(null) }

    var showEditDialog by remember { mutableStateOf(false) }
    var editedTransaction by remember { mutableStateOf<Transaction?>(null) }

    // Campos editables
    var editTitle by remember { mutableStateOf("") }
    var editAmount by remember { mutableStateOf("") }
    var editDate by remember { mutableStateOf(Date()) }

    // Usamos LazyColumn para scroll vertical global
    LazyColumn(modifier = Modifier.fillMaxSize()) {

        item {
            // Barra superior personalizada
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = "Transacciones Globales",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            // Mostrar el balance total centrado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
        }

        item {
            // Contenido principal con padding ajustado
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Filtros de tipo (ingreso/gasto)
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { showIncomes = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showIncomes) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (showIncomes) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("Mostrar Ingresos")
                    }
                    Button(
                        onClick = { showIncomes = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!showIncomes) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (!showIncomes) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("Mostrar Gastos")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Filtros de tiempo
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(TimeFilter.values()) { filter ->
                        val isSelected = timeFilter == filter
                        Button(
                            onClick = { timeFilter = filter },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (timeFilter == filter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (timeFilter == filter) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(filter.displayName)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (groupedTransactions.isNotEmpty()) {
            item {
                Text(
                    text = if (showIncomes) "Resumen Global de Ingresos" else "Resumen Global de Gastos",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(groupedTransactions) { (category, totalAmount) ->
                ExpandableCategoryItem(
                    category = category,
                    totalAmount = totalAmount,
                    transactions = filteredTransactions.filter { it.category == category },
                    isExpanded = expandedCategory == category,
                    onExpand = {
                        expandedCategory = if (expandedCategory == category) null else category
                    },
                    onDeleteTransaction = { transaction ->
                        showDeleteConfirmation = true
                        selectedTransactionToDelete = transaction
                    },
                    onEditTransaction = { transaction ->
                        editedTransaction = transaction
                        editTitle = transaction.title
                        editAmount = formatCurrency(transaction.amount)
                        editDate = transaction.date
                        showEditDialog = true
                    }
                )
            }
        } else {
            item {
                Text(
                    text = if (showIncomes) "No hay ingresos globales registrados." else "No hay gastos globales registrados.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        if (showDeleteConfirmation && selectedTransactionToDelete != null) {
            item {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmation = false },
                    title = { Text("Confirmar eliminación") },
                    text = { Text("¿Estás seguro de eliminar esta transacción?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                val transaction = selectedTransactionToDelete
                                if (transaction != null) {
                                    viewModel.deleteTransaction(transaction.id)
                                    // Reinicia la variable después de eliminar
                                    showDeleteConfirmation = false
                                    selectedTransactionToDelete = null
                                }
                            }
                        ) {
                            Text("Sí, eliminar")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showDeleteConfirmation = false }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
    if (showEditDialog && editedTransaction != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar Transacción") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editAmount,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*\$"))) {
                                editAmount = it
                            }
                        },
                        label = { Text("Cantidad") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDate?.let { formatDate(it) } ?: "",
                        onValueChange = { input ->
                            try {
                                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                editDate = dateFormat.parse(input)
                            } catch (e: Exception) {
                                // Ignorar entrada inválida
                            }
                        },
                        label = { Text("Fecha (dd/MM/yyyy)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val transaction = editedTransaction
                        if (transaction != null && editDate != null) {
                            viewModel.updateTransaction(
                                transaction.copy(
                                    title = editTitle.trim(),
                                    amount = editAmount.toDoubleOrNull() ?: transaction.amount,
                                    date = editDate!!
                                )
                            )
                        }
                        showEditDialog = false
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Button(onClick = { showEditDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isSameWeek(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
}

private fun isSameMonth(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
}

private fun isSameYear(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(date)
}