package com.example.kapital.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.kapital.model.Account
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "kapital_prefs")

class DataStoreManager(private val context: Context) {

    private val gson = Gson()
    private val ACCOUNTS_KEY = stringPreferencesKey("accounts")

    fun getAccounts(): Flow<List<Account>> = context.dataStore.data.map { preferences ->
        val json = preferences[ACCOUNTS_KEY] ?: "[]"
        val type = object : TypeToken<List<Account>>() {}.type
        gson.fromJson(json, type)
    }

    suspend fun saveAccounts(accounts: List<Account>) {
        val json = gson.toJson(accounts)
        context.dataStore.edit { prefs ->
            prefs[ACCOUNTS_KEY] = json
        }
    }
}