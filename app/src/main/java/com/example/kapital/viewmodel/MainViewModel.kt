package com.example.kapital.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kapital.data.DataStoreManager
import com.example.kapital.model.Account
import com.example.kapital.model.Category
import com.example.kapital.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStoreManager = DataStoreManager(application.applicationContext)

    private var nextAccountId = 0
    private var nextTransactionId = 0

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    init {
        viewModelScope.launch {
            dataStoreManager.getAccounts().collect { loadedAccounts ->
                _accounts.value = loadedAccounts
                nextAccountId = (loadedAccounts.maxOfOrNull { it.id } ?: 0) + 1
                nextTransactionId =
                    ((loadedAccounts.flatMap { it.transactions }.maxOfOrNull { it.id } ?: 0) + 1).toInt()
            }
        }
    }

    fun addAccount(name: String) {
        val newAccount = Account(id = nextAccountId++, name = name)
        val updatedAccounts = _accounts.value + newAccount
        updateAccounts(updatedAccounts)
    }

    fun deleteAccount(accountId: Int) {
        val updatedAccounts = _accounts.value.filter { it.id != accountId }
        updateAccounts(updatedAccounts)
    }

    fun addTransaction(
        accountId: Int,
        title: String,
        amount: Double,
        isIncome: Boolean,
        category: Category,
        date: Date
    ) {
        val adjustedAmount = if (isIncome) amount else -amount

        val updatedAccounts = accounts.value.map { account ->
            if (account.id == accountId) {
                account.copy(
                    transactions = account.transactions + Transaction(
                        id = generateTransactionId().toInt(),
                        title = title,
                        amount = adjustedAmount,
                        isIncome = isIncome,
                        category = category,
                        date = date,
                        accountId = accountId
                    )
                )
            } else account
        }

        updateAccounts(updatedAccounts)
    }


    fun updateTransaction(updated: Transaction) {
        _accounts.value = accounts.value.map { account ->
            if (account.id == updated.accountId) {
                account.copy(
                    transactions = account.transactions.map { t ->
                        if (t.id == updated.id) updated else t
                    }
                )
            } else account
        }
        updateAccounts(_accounts.value)
    }

    fun deleteTransaction(transactionId: Int) {
        _accounts.update { current ->
            current.map { account ->
                account.copy(
                    transactions = account.transactions.filter { it.id != transactionId }
                )
            }
        }
        updateAccounts(_accounts.value)
    }

    private fun generateTransactionId(): Long {
        // Implementa lógica para generar un ID único
        return System.currentTimeMillis()
    }

    fun getAccountBalance(accountId: Int): Double {
        val account = _accounts.value.find { it.id == accountId } ?: return 0.0
        return account.transactions.sumOf { it.amount }
    }

    private fun updateAccounts(newAccounts: List<Account>) {
        _accounts.value = newAccounts
        viewModelScope.launch {
            dataStoreManager.saveAccounts(newAccounts)
        }
    }

    fun updateAccount(updated: Account) {
        _accounts.update { current ->
            current.map { account ->
                if (account.id == updated.id) updated else account
            }
        }
    }

    fun formatCurrency(amount: Double): String {
        val formatter = DecimalFormat("###,###,##0.00", DecimalFormatSymbols.getInstance(Locale.GERMANY))
        return "${formatter.format(amount).replace(",", "X").replace(".", ",").replace("X", ".")} €"
    }
}
