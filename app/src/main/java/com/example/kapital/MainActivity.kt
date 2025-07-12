package com.example.kapital

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.kapital.ui.navigation.NavGraph
import com.example.kapital.ui.theme.KapitalTheme
import com.example.kapital.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KapitalTheme {
                val navController = rememberNavController()
                val viewModel: MainViewModel = viewModel()
                NavGraph(navController = navController, viewModel = viewModel, onBack = {
                    onBackPressedDispatcher.onBackPressed()
                })
            }
        }
    }
}
