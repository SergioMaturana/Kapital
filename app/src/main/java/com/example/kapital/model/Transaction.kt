package com.example.kapital.model

import java.util.*

data class Transaction(
    val id: Int,
    val title: String,
    val amount: Double,
    val isIncome: Boolean,
    val category: Category,
    val date: Date,
    val accountId: Int
)