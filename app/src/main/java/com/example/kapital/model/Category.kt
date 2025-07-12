package com.example.kapital.model

import androidx.compose.ui.graphics.Color
import com.example.kapital.R

enum class Category(
    val displayName: String,
    val isIncomeCategory: Boolean,
    val color: Color,
    val iconRes: Int
) {
    // Categorías de Ingresos
    SALARY("Salario", true, Color.Green, R.drawable.ic_salary),
    INVESTMENT("Inversión", true, Color(0xFF6C3483), R.drawable.ic_investment), // Morado oscuro
    GIFT_INCOME("Regalo", true, Color(0xFFFFD700), R.drawable.ic_gift),         // Dorado
    INTEREST("Interés", true, Color(0xFF1E90FF), R.drawable.ic_interest),       // Azul brillante
    BIZUM("Bizum", true, Color(0xFF00B894), R.drawable.ic_bizum),               // Verde turquesa
    TIP("Propina", true, Color(0xFFFECB5E), R.drawable.ic_tip),                 // Amarillo suave
    OTHER_INCOME("Otro (Ingreso)", true, Color.DarkGray, R.drawable.ic_other_income),

    // Categorías de Gastos
    FOOD("Comida", false, Color.Red, R.drawable.ic_food),
    HOUSING("Vivienda", false, Color(0xFFAC92EB), R.drawable.ic_housing),       // Lila
    TRANSPORT("Transporte", false, Color(0xFFFF9800), R.drawable.ic_transport), // Naranja
    SERVICES("Servicios", false, Color(0xFF2ECC71), R.drawable.ic_services),//,    // Verde esmeralda
    CLOTHING("Ropa y calzado", false, Color(0xFFE67E22), R.drawable.ic_clothing), // Naranja oscuro
    HEALTH("Salud", false, Color(0xFFE74C3C), R.drawable.ic_health),           // Rojo claro
    EDUCATION("Educación", false, Color(0xFF3498DB), R.drawable.ic_education);  // Azul cielo
//    LEISURE("Ocio", false, Color.Blue, R.drawable.ic_entertainment),
//    SAVINGS("Ahorro e inversión", false, Color(0xFF55EFc4), R.drawable.ic_savings), // Verde menta
//    PETS("Mascotas", false, Color(0xFFF1C40F), R.drawable.ic_pets),             // Amarillo
//    GIFTS("Regalos", false, Color(0xFFFD79A8), R.drawable.ic_gifts),            // Rosa pastel
//    OTHER_EXPENSE("Otro (Gasto)", false, Color.Gray, R.drawable.ic_other_expense);

    companion object {
        fun getIncomeCategories(): List<Category> = values().filter { it.isIncomeCategory }
        fun getExpenseCategories(): List<Category> = values().filter { !it.isIncomeCategory }
    }
}