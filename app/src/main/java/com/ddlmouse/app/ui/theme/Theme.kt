package com.ddlmouse.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Color(0xFF3F6F5E),
    onPrimary = Color.White,
    secondary = Color(0xFFF47C62),
    onSecondary = Color(0xFF2B1B16),
    tertiary = Color(0xFFE6B84F),
    background = Color(0xFFFFFAF1),
    onBackground = Color(0xFF25312B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF25312B),
    surfaceVariant = Color(0xFFE7EFE3),
    onSurfaceVariant = Color(0xFF47564E),
    error = Color(0xFFC84E3A)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(8.dp)
)

@Composable
fun DDLMouseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        shapes = AppShapes,
        content = content
    )
}

