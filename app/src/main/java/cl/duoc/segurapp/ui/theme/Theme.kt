package cl.duoc.segurapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Navy = Color(0xFF0B3B5C)
val Fondo = Color(0xFFF5F7F9)
val Texto = Color(0xFF1B2630)
val TextoSec = Color(0xFF5B6B7A)
val Borde = Color(0xFFA9B6C2)
val HeaderSub = Color(0xFFC5D6E3)
val Ambar = Color(0xFFF2A900)
val Gris = Color(0xFFCBD5DD)
val ChipFondo = Color(0xFFE8EDF2)

@Composable
fun SegurAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Navy,
            onPrimary = Color.White,
            background = Fondo,
            surface = Color.White,
            onSurface = Texto,
            error = Color(0xFFB3261E)
        ),
        content = content
    )
}
