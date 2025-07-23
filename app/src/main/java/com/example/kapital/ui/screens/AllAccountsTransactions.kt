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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.kapital.model.Category
import com.example.kapital.model.Transaction
import com.example.kapital.ui.screens.formatCurrency
import com.example.kapital.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import kotlin.math.absoluteValue
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.utils.ColorTemplate

@Composable
fun AllAccountsTransactions(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val accounts = viewModel.accounts.collectAsState().value
    var timeFilter by remember { mutableStateOf(TimeFilter.ALL) }
    var expandedCategory: Category? by remember { mutableStateOf(null) }
    var isBalanceVisible by remember { mutableStateOf(true) }
    var selectedCategoryInfo by remember { mutableStateOf<Pair<Any?, Double>?>(null) }

    // Calcular el balance total de todas las cuentas
    val totalBalance = accounts.sumOf { account ->
        viewModel.getAccountBalance(account.id)
    }

    // Obtener TODAS las transacciones de TODAS las cuentas
    val allTransactions = remember(accounts) {
        accounts.flatMap { account -> account.transactions }
    }

    // Filtrar solo por rango de fecha
    val filteredTransactions = remember(allTransactions, timeFilter) {
        allTransactions.filter { transaction ->
            when (timeFilter) {
                TimeFilter.Dia -> isSameDay(transaction.date, Date())
                TimeFilter.Semana -> isSameWeek(transaction.date, Date())
                TimeFilter.Mes -> isSameMonth(transaction.date, Date())
                TimeFilter.Anio -> isSameYear(transaction.date, Date())
                TimeFilter.ALL -> true
            }
        }
    }

    // Agrupar transacciones por categoría y calcular montos totales para el gráfico
    val groupedTransactionsForChart = remember(filteredTransactions) {
        filteredTransactions
            .groupBy { it.category }
            .map { (category, transactions) ->
                Triple(
                    category.name,
                    transactions.sumOf { it.amount.absoluteValue }, // Monto absoluto para el gráfico
                    category.color // Incluir el color de la categoría
                )
            }
            .sortedByDescending { it.second } // Ordenar por monto total descendente
    }

    var chartType by remember { mutableStateOf(ChartType.CATEGORIES) }

    val totalIncome = remember(filteredTransactions) {
        filteredTransactions.filter { it.amount > 0 }.sumOf { it.amount }
    }

    val totalExpenses = remember(filteredTransactions) {
        filteredTransactions.filter { it.amount < 0 }.sumOf { it.amount.absoluteValue }
    }

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

                // Botones para cambiar el tipo de gráfico (NUEVO)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { chartType = ChartType.CATEGORIES },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (chartType == ChartType.CATEGORIES) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (chartType == ChartType.CATEGORIES) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("Por Categorías")
                    }
                    Button(
                        onClick = { chartType = ChartType.INCOME_EXPENSES },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (chartType == ChartType.INCOME_EXPENSES) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (chartType == ChartType.INCOME_EXPENSES) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("Ingresos vs Gastos")
                    }
                }

            }
        }

        if (chartType == ChartType.CATEGORIES && groupedTransactionsForChart.isNotEmpty()) {
            val categoryData = groupedTransactionsForChart.map { Triple(it.first, it.second, it.third) }
            val categories = Category.values().toList()

            item {
                PieChartView(
                    data = categoryData.map { it.first to it.second },
                    categories = categories,
                    onSegmentClick = { label, amount ->
                        val category = categories.find { it.name == label }
                        selectedCategoryInfo = if (category != null) Pair(category, amount) else null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Mostrar transacciones agrupadas por categoría
            groupedTransactionsForChart.forEach { (categoryName, _, _) ->
                val category = categories.find { it.name == categoryName }
                val transactionsForCategory = filteredTransactions.filter { it.category == category }

                if (transactionsForCategory.isNotEmpty()) {
                    item {
                        Text(
                            text = category?.displayName ?: categoryName,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    items(transactionsForCategory) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            viewModel = viewModel,
                            onDelete = {
                                viewModel.deleteTransaction(transaction.id)
                            }
                        )
                    }
                }
            }
        } else if (chartType == ChartType.INCOME_EXPENSES) {

            val incomeTransactions = filteredTransactions.filter { it.amount > 0 }
            val expenseTransactions = filteredTransactions.filter { it.amount < 0 }

            // Resumen global de transacciones
            item {
                Text(
                    text = "Resumen Global de Transacciones",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            // Gráfico de ingresos vs gastos
            item {
                val totalIncome = remember(filteredTransactions) {
                    filteredTransactions.filter { it.amount > 0 }.sumOf { it.amount }
                }
                val totalExpenses = remember(filteredTransactions) {
                    filteredTransactions.filter { it.amount < 0 }.sumOf { it.amount.absoluteValue }
                }
                IncomeVsExpensesChart(
                    income = totalIncome,
                    expenses = totalExpenses,
                    onSegmentClick = { label, amount ->
                        selectedCategoryInfo = when (label) {
                            "Ingresos" -> Pair("Ingresos", amount)
                            "Gastos" -> Pair("Gastos", amount)
                            else -> {
                                val category = Category.values().find { it.name == label }
                                if (category != null) Pair(category, amount) else null
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Mostrar ingresos
            if (incomeTransactions.isNotEmpty()) {
                item {
                    Text(
                        text = "Ingresos",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
                    )
                }
                items(incomeTransactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        viewModel = viewModel,
                        onDelete = {
                            viewModel.deleteTransaction(transaction.id)
                        }
                    )
                }
            } else {
                item {
                    Text(
                        text = "No hay ingresos registrados.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Mostrar gastos
            if (expenseTransactions.isNotEmpty()) {
                item {
                    Text(
                        text = "Gastos",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
                    )
                }
                items(expenseTransactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        viewModel = viewModel,
                        onDelete = {
                            viewModel.deleteTransaction(transaction.id)
                        }
                    )
                }
            } else {
                item {
                    Text(
                        text = "No hay gastos registrados.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

    }

    // Mostrar el diálogo con la información del segmento seleccionado
    if (selectedCategoryInfo != null) {
        val (labelOrCategory, amount) = selectedCategoryInfo!!
        AlertDialog(
            onDismissRequest = { selectedCategoryInfo = null },
            title = { Text("Detalles") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (labelOrCategory is Category) {
                        Text(text = "Categoría: ${labelOrCategory.displayName}")
                    } else {
                        Text(text = "Etiqueta: $labelOrCategory")
                    }
                    Text(text = "Cantidad: ${formatCurrency(amount)}")
                }
            },
            confirmButton = {
                Button(onClick = { selectedCategoryInfo = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun PieChartView(
    data: List<Pair<String, Double>>,
    categories: List<Category>, // Agregamos las categorías para obtener sus colores
    onSegmentClick: (String, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                isDrawHoleEnabled = true
                setEntryLabelColor(Color.BLACK)
                setUsePercentValues(false) // Desactivar porcentajes
                legend.isEnabled = false // Desactivar leyenda
                animateY(1000)
                setDrawEntryLabels(false) // Desactivar etiquetas de texto dentro del gráfico
                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        if (e is PieEntry) {
                            val category = e.label
                            val amount = e.value.toDouble()
                            onSegmentClick(category, amount) // Llamar al callback
                        }
                    }
                    override fun onNothingSelected() {
                        // No hacer nada si no se selecciona nada
                    }
                })
            }
        },
        update = { chart ->
            val entries = data.map { (categoryName, amount) ->
                PieEntry(amount.toFloat(), categoryName)
            }

            // Obtener los colores de las categorías correspondientes
            val colors = data.mapNotNull { (categoryName, _) ->
                categories.find { it.name == categoryName }?.color?.toArgb()
            }

            val dataSet = PieDataSet(entries, "").apply {
                this.colors = colors // Asignar los colores correctos
                valueTextSize = 0f // Ocultar cantidades
                sliceSpace = 3f
                selectionShift = 5f
            }

            val pieData = PieData(dataSet)
            chart.data = pieData
            chart.invalidate()
        },
        modifier = modifier
    )
}

@Composable
fun IncomeVsExpensesChart(
    income: Double,
    expenses: Double,
    onSegmentClick: (String, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                isDrawHoleEnabled = true
                setEntryLabelColor(Color.BLACK)
                setUsePercentValues(false)
                legend.isEnabled = false
                animateY(1000)
                setDrawEntryLabels(false)
                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        if (e is PieEntry) {
                            val label = e.label
                            val amount = e.value.toDouble()
                            onSegmentClick(label, amount)
                        }
                    }

                    override fun onNothingSelected() {}
                })
            }
        },
        update = { chart ->
            val entries = listOf(
                PieEntry(income.toFloat(), "Ingresos"),
                PieEntry(expenses.toFloat(), "Gastos")
            )
            val colors = listOf(
                Color.GREEN,
                Color.RED
            )

            val dataSet = PieDataSet(entries, "").apply {
                this.colors = colors
                valueTextSize = 0f
                sliceSpace = 3f
                selectionShift = 5f
            }
            val pieData = PieData(dataSet)
            chart.data = pieData
            chart.invalidate()
        },
        modifier = modifier
    )
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

enum class ChartType {
    CATEGORIES, // Gráfico actual por categorías
    INCOME_EXPENSES // Gráfico de ingresos vs. gastos
}
