package com.example.kapital.model

data class Account(
    val id: Int,
    val name: String,
    val transactions: List<Transaction> = mutableListOf()
)