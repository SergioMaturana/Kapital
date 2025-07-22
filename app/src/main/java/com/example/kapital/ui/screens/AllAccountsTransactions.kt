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
    var selectedCategoryInfo by remember { mutableStateOf<Pair<Category, Double>?>(null) }

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
            }
        }
        if (groupedTransactionsForChart.isNotEmpty()) {
            item {
                Text(
                    text = "Resumen Global de Transacciones",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            item {
                PieChartView(
                    data = groupedTransactionsForChart.map { (category, totalAmount) ->
                        category to totalAmount
                    },
                    categories = Category.values().toList(), // Pasamos todas las categorías
                    onSegmentClick = { categoryName, amount ->
                        val category = Category.values().find { it.name == categoryName }
                        if (category != null) {
                            selectedCategoryInfo = Pair(category, amount)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Nueva sección para mostrar la lista de transacciones
            if (filteredTransactions.isNotEmpty()) {
                item {
                    Text(
                        text = "Lista de Transacciones",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
                    )
                }
                items(filteredTransactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        viewModel = viewModel,
                        onDelete = {
                            // Lógica para eliminar la transacción
                            viewModel.deleteTransaction(transaction.id)
                        }
                    )
                }

            } else {
                item {
                    Text(
                        text = "No hay transacciones registradas.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

        } else {
            item {
                Text(
                    text = "No hay transacciones globales registradas.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

    }

    // Mostrar el diálogo con la información del segmento seleccionado
    if (selectedCategoryInfo != null) {
        val (category, amount) = selectedCategoryInfo!!
        AlertDialog(
            onDismissRequest = { selectedCategoryInfo = null },
            title = { Text("Detalles de la Categoría") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Categoría: ${category.displayName}") // Usar displayName
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
