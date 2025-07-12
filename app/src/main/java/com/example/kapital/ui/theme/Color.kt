package com.example.kapital.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Colores para el modo claro
val LightColors = lightColorScheme(
    primary = Color(0xFF0D47A1),
    onPrimary = Color.White,
    secondary = Color(0xFF03DAC5),
    onSecondary = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF424242),
    outline = Color(0xFF9E9E9E),
    error = Color(0xFFB00020),
    onError = Color.White,
    primaryContainer = Color(0xFFDCE9F9),
    onPrimaryContainer = Color(0xFF000000)
)

// Colores para el modo oscuro - Mejorados
val DarkColors = darkColorScheme(
    primary = Color(0xFF0D47A1),
    onPrimary = Color.White,
    secondary = Color(0xFF03DAC5),
    onSecondary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color.White, // Texto principal en blanco
    surface = Color(0xFF1F1F1F),
    onSurface = Color.White, // Texto en superficies en blanco
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color.White, // Texto en variantes de superficie en blanco
    outline = Color(0xFF737373),
    error = Color(0xFFFF6464),
    onError = Color.White,
    primaryContainer = Color(0xFF354259),
    onPrimaryContainer = Color(0xFFFFFFFF),
)