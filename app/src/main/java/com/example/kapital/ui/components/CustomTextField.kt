package com.example.kapital.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember { mutableStateOf(value) }

    Column(modifier = modifier) {
        // Etiqueta del campo de texto
        Text(
            text = label,
            style = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Campo de texto personalizado
        BasicTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                onValueChange(it)
            },
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface // Usa el color de texto para superficies
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary), // Color del cursor
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp) // Altura fija para simular un campo de texto estándar
                .background(MaterialTheme.colorScheme.surface) // Usa el color de superficie del tema
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline, // Usa 'outline' para bordes claros/oscuros
                    shape = MaterialTheme.shapes.small
                )
                .padding(12.dp) // Espaciado interno
        )

        // Línea inferior (opcional, si quieres un estilo "outlined")
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        )
    }
}