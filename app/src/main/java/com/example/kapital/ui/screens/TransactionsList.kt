import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kapital.model.Category
import com.example.kapital.model.Transaction
import com.example.kapital.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.kapital.ui.screens.DropdownMenuCategory
import com.example.kapital.ui.screens.formatCurrency
import java.util.Calendar


enum class TimeFilter(val displayName: String) {
    Dia("Hoy"),
    Semana("Semana"),
    Mes("Mes"),
    Anio("Año"),
    ALL("Todo")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsList(accountId: Int, viewModel: MainViewModel, onBack: () -> Unit) {
    val account = viewModel.accounts.collectAsState().value.find { it.id == accountId } ?: return

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(true) }
    var selectedCategory by remember {
        mutableStateOf(if (isIncome) Category.getIncomeCategories().first() else Category.getExpenseCategories().first())
    }

    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var dateError by remember { mutableStateOf(false) }
    var futureDateError by remember { mutableStateOf(false) }
    var newDate by remember { mutableStateOf("") }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Formateador de fechas
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    var showEditDialog by remember { mutableStateOf(false) }
    var editedTransaction by remember { mutableStateOf<Transaction?>(null) }

    // Campos del diálogo de edición
    var editTitle by remember { mutableStateOf("") }
    var editAmount by remember { mutableStateOf("") }
    var editDate by remember { mutableStateOf<Date?>(null) }

    var showIncomes by remember { mutableStateOf(true) }
    var timeFilter by remember { mutableStateOf(TimeFilter.ALL) }
    var expandedCategory by remember { mutableStateOf<Category?>(null) }

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var selectedTransactionToDelete: Transaction? by remember { mutableStateOf(null) }

    var titleError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    var showAddDialog by remember { mutableStateOf(false) }
    var showIncomeDialog by remember { mutableStateOf(false) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var showFullFormDialog by remember { mutableStateOf(false) }


    Spacer(modifier = Modifier.height(8.dp))

    // FIX: Fondo global para la pantalla
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        // 🔝 Barra superior personalizada
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón de retroceso
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Volver a cuentas",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Título de la pantalla
            Text(
                text = "Cuentas",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onPrimary
                ),
                maxLines = 1
            )
        }

        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text("Nuevo Movimiento")
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Selecciona tipo de movimiento") },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("¿Qué tipo de movimiento deseas agregar?")
                        Button(
                            onClick = {
                                isIncome = true
                                selectedCategory = Category.getIncomeCategories().first()
                                showAddDialog = false
                                showFullFormDialog = true
                            }
                        ) {
                            Text("Agregar Ingreso")
                        }
                        Button(
                            onClick = {
                                isIncome = false
                                selectedCategory = Category.getExpenseCategories().first()
                                showAddDialog = false
                                showFullFormDialog = true
                            }
                        ) {
                            Text("Agregar Gasto")
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {}
            )
        }


        if (showFullFormDialog) {
            AlertDialog(
                onDismissRequest = { showFullFormDialog = false },
                title = { Text(if (isIncome) "Nuevo Ingreso" else "Nuevo Gasto") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                if (titleError && it.isNotBlank()) titleError = false
                            },
                            label = {
                                Text(
                                    "Descripción",
                                    color = if (titleError) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            },
                            isError = titleError,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (titleError) {
                            Text(
                                text = "La descripción es obligatoria",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        OutlinedTextField(
                            value = amount,
                            onValueChange = { newValue ->
                                val processedValue = newValue.replace(',', '.')
                                if (processedValue.isEmpty() ||
                                    (processedValue.matches(Regex("^\\d*\\.?\\d*\$")) &&
                                            processedValue.count { it == '.' } <= 1)
                                ) {
                                    amount = processedValue
                                    if (amountError && processedValue.toDoubleOrNull() ?: 0.0 > 0) {
                                        amountError = false
                                    }
                                }
                            },
                            label = {
                                Text(
                                    "Cantidad",
                                    color = if (amountError) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            },
                            isError = amountError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (amountError) {
                            Text(
                                text = "La cantidad debe ser mayor a 0",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Text("Categoría:", style = MaterialTheme.typography.labelMedium)
                        DropdownMenuCategory(
                            selectedCategory = selectedCategory,
                            categories = if (isIncome) Category.getIncomeCategories() else Category.getExpenseCategories(),
                            onCategorySelected = { selectedCategory = it }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newDate.ifBlank { dateFormat.format(selectedDate.time) },
                            onValueChange = { input ->
                                newDate = input
                                try {
                                    val parsed = dateFormat.parse(input)
                                    if (parsed != null) {
                                        selectedDate.time = parsed
                                        dateError = false
                                        futureDateError = parsed.after(Date())
                                    }
                                } catch (_: Exception) {
                                    dateError = true
                                }
                            },
                            label = { Text("Fecha (dd/MM/yyyy)") },
                            trailingIcon = {
                                IconButton(onClick = { showDatePickerDialog = true }) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                                }
                            },
                            isError = dateError || futureDateError,
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (dateError) {
                            Text(
                                "Fecha inválida",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                        if (futureDateError) {
                            Text(
                                "No se permiten fechas futuras",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val amt = amount.toDoubleOrNull() ?: 0.0
                        titleError = title.isBlank()
                        amountError = amt <= 0

                        if (!titleError && !amountError && !dateError && !futureDateError) {
                            val currentDate = Calendar.getInstance().time
                            if (selectedDate.time.after(currentDate)) {
                                futureDateError = true
                                return@Button
                            }

                            viewModel.addTransaction(
                                accountId = accountId,
                                title = title.trim(),
                                amount = amt,
                                isIncome = isIncome,
                                category = selectedCategory,
                                date = selectedDate.time
                            )

                            // Resetear estado
                            title = ""
                            amount = ""
                            titleError = false
                            amountError = false
                            val calendar = Calendar.getInstance()
                            selectedDate = calendar
                            newDate = formatDate(calendar.time)

                            showFullFormDialog = false
                        }
                    }) {
                        Text("Agregar Movimiento")
                    }
                },
                dismissButton = {
                    Button(onClick = { showFullFormDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
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

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(TimeFilter.values()) { filter ->
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

        Spacer(modifier = Modifier.height(16.dp))

        val transactionsToShow = if (showIncomes) {
            account.transactions.filter { it.isIncome }
        } else {
            account.transactions.filter { !it.isIncome }
        }.filter {
            when (timeFilter) {
                TimeFilter.Dia -> isSameDay(it.date, Date())
                TimeFilter.Semana -> isSameWeek(it.date, Date())
                TimeFilter.Mes -> isSameMonth(it.date, Date())
                TimeFilter.Anio -> isSameYear(it.date, Date())
                TimeFilter.ALL -> true
            }
        }

        val groupedTransactions = transactionsToShow.groupBy { it.category }.map { (category, transactions) ->
            val totalAmount = transactions.sumOf { it.amount }
            Pair(category, totalAmount)
        }

        if (groupedTransactions.isNotEmpty()) {
            Text(
                text = if (showIncomes) "Resumen de Ingresos" else "Resumen de Gastos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface, // FIX: Color de texto
                modifier = Modifier.padding(vertical = 8.dp)
            )
            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                items(groupedTransactions) { (category, totalAmount) ->
                    ExpandableCategoryItem(
                        category = category,
                        totalAmount = totalAmount,
                        transactions = transactionsToShow.filter { it.category == category },
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
            }

            if (showEditDialog && editedTransaction != null) {
                AlertDialog(
                    onDismissRequest = { showEditDialog = false },
                    title = { Text("Editar Transacción") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Campo: Descripción
                            OutlinedTextField(
                                value = title,
                                onValueChange = {
                                    title = it
                                    if (it.isNotBlank()) titleError = false
                                },
                                label = { Text("Descripción") },
                                isError = titleError,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (titleError) {
                                Text(
                                    text = "La descripción es obligatoria",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }

                            // Campo: Cantidad
                            OutlinedTextField(
                                value = amount,
                                onValueChange = { newValue ->
                                    val processedValue = newValue.replace(',', '.')
                                    if (processedValue.isEmpty() ||
                                        (processedValue.matches(Regex("^\\d*\\.?\\d*\$")) && processedValue.count { it == '.' } <= 1)) {
                                        amount = processedValue
                                        if (processedValue.toDoubleOrNull() ?: 0.0 > 0) amountError = false
                                    }
                                },
                                label = { Text("Cantidad") },
                                isError = amountError,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (amountError) {
                                Text(
                                    text = "La cantidad debe ser mayor que 0",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }

                            // Campo: Fecha con validación visual
                            OutlinedTextField(
                                value = editDate?.let { formatDate(it) } ?: "",
                                onValueChange = { input ->
                                    try {
                                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                        val parsedDate = dateFormat.parse(input)
                                        editDate = parsedDate
                                    } catch (e: Exception) {
                                        editDate = null  // Marca fecha como inválida
                                    }
                                },
                                label = { Text("Fecha (dd/MM/yyyy)") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = editDate == null, // Muestra error si la fecha es inválida
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (editDate == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = if (editDate == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                                )
                            )

                            // Mensaje de error opcional
                            if (editDate == null) {
                                Text(
                                    text = "Fecha inválida. Use el formato dd/MM/yyyy",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val transaction = editedTransaction
                                if (transaction != null && editDate != null && editTitle.isNotBlank()) {
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
                        Button(
                            onClick = {
                                showEditDialog = false
                            }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            if (showDeleteConfirmation && selectedTransactionToDelete != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmation = false },
                    title = { Text("Confirmar eliminación") },
                    text = { Text("¿Estás seguro de eliminar esta transacción?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                val transaction = selectedTransactionToDelete
                                if (transaction != null) {
                                    viewModel.deleteTransaction(transaction.id) // ❗ Aquí se borra de verdad
                                    showDeleteConfirmation = false
                                    selectedTransactionToDelete = null
                                }
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

        } else {
            Text(
                text = if (showIncomes) "No hay ingresos registrados." else "No hay gastos registrados.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface, // FIX: Color de texto
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

    }
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(date)
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

@Composable
fun ExpandableCategoryItem(
    category: Category,
    totalAmount: Double,
    transactions: List<Transaction>,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    onEditTransaction: (Transaction) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .clickable(onClick = onExpand)
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(category.color)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = category.iconRes),
                            contentDescription = category.displayName,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${if (totalAmount >= 0) "+" else ""}${formatCurrency(totalAmount)} €",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = if (isExpanded) "Contraer" else "Expandir"
                )
            }

            if (isExpanded) {
                Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)) {
                    transactions.forEach { transaction ->
                        TransactionDetailItem(
                            transaction = transaction,
                            onDelete = { onDeleteTransaction(transaction) },
                            onEdit = { onEditTransaction(transaction) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionDetailItem(transaction: Transaction,onDelete: () -> Unit,onEdit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface, // FIX: Color de texto
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${if (transaction.isIncome) "+" else ""}${formatCurrency(transaction.amount)} €",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface // FIX: Color de texto
            )
            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onEdit) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar"
                )
            }

        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Fecha: ${formatDate(transaction.date)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), // FIX: Color de texto
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

// Componente para mostrar el resumen de una categoría
@Composable
fun CategorySummaryItem(category: Category, totalAmount: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(color = category.color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        category.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${if (totalAmount >= 0) "+" else "-"}${formatCurrency(totalAmount)} €",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    viewModel: MainViewModel,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var selectedTransactionToDelete: Transaction? by remember { mutableStateOf(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(color = transaction.category.color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        transaction.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${transaction.category.displayName} - ${formatCurrency(transaction.amount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Fecha: ${formatDate(transaction.date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            IconButton(onClick = { showDeleteConfirmation = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun DropdownMenuCategory(
    selectedCategory: Category,
    categories: List<Category>,
    onCategorySelected: (Category) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedCategory.displayName,
            onValueChange = { },
            readOnly = true,
            label = { Text("Categoría", color = MaterialTheme.colorScheme.onSurface) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(color = category.color)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                category.displayName,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}