package com.johnny.tier1bankdemo.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color

// Chase Bank Digital Identity Standards
private val LightColors = lightColorScheme(
    primary = Color(0xFF117ACA),          // Chase Blue
    onPrimary = Color(0xFFFFFFFF),        // White text on blue buttons
    primaryContainer = Color(0xFFE6F0F9), // Light blue tint
    background = Color(0xFFFFFFFF),       // Strictly White background
    surface = Color(0xFFFFFFFF),          // Strictly White surface
    onBackground = Color(0xFF211E1E),     // Chase Bank Black
    onSurface = Color(0xFF211E1E),
    onSurfaceVariant = Color(0xFF5F6368), // Mid-grey for subtext
    error = Color(0xFFD92D20)             // Error Red
)

@Composable
fun Tier1BankTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = {
            // Ensure a safe, white background for the entire application
            androidx.compose.material3.Surface(
                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                color = androidx.compose.material3.MaterialTheme.colorScheme.background
            ) {
                content()
            }
        }
    )
}
