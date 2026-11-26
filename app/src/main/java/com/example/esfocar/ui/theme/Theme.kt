package com.example.esfocar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = VerdeCarabinerosClaro,
    onPrimary = Color(0xFF003920),
    primaryContainer = VerdeContenedorOscuro,
    onPrimaryContainer = VerdeContenedorClaro,
    secondary = VerdeOlivaClaro,
    onSecondary = Color(0xFF1D3529),
    tertiary = DoradoInstitucionalClaro,
    background = FondoOscuro,
    surface = FondoOscuro
)

private val LightColorScheme = lightColorScheme(
    primary = VerdeCarabinerosOscuro,
    onPrimary = Color.White,
    primaryContainer = VerdeContenedorClaro,
    onPrimaryContainer = VerdeCarabinerosOscuro,
    secondary = VerdeOliva,
    onSecondary = Color.White,
    tertiary = DoradoInstitucional,
    background = FondoClaro,
    surface = Color.White
)

@Composable
fun ESFOCARTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // En false para respetar siempre los colores institucionales
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}