package cl.duoc.segurapp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.segurapp.ui.theme.Borde
import cl.duoc.segurapp.ui.theme.ChipFondo
import cl.duoc.segurapp.ui.theme.HeaderSub
import cl.duoc.segurapp.ui.theme.Navy
import cl.duoc.segurapp.ui.theme.Texto
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

val FormShape = RoundedCornerShape(14.dp)

@Composable
fun AppHeader(titulo: String, subtitulo: String? = null, onBack: (() -> Unit)? = null) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Navy)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
            }
            Text(titulo, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
        if (subtitulo != null) {
            Text(
                subtitulo, color = HeaderSub, fontSize = 14.sp,
                modifier = Modifier.padding(start = if (onBack != null) 48.dp else 0.dp)
            )
        }
    }
}

@Composable
fun EtiquetaCampo(texto: String) {
    Text(
        texto, color = Texto, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun camposColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = Navy,
    unfocusedBorderColor = Borde
)

@Composable
fun CampoTexto(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    clave: Boolean = false,
    teclado: KeyboardType = KeyboardType.Text,
    minLineas: Int = 1
) {
    var visible by remember { mutableStateOf(false) }
    Column(modifier) {
        EtiquetaCampo(etiqueta)
        OutlinedTextField(
            value = valor,
            onValueChange = onCambio,
            placeholder = if (placeholder.isNotEmpty()) ({ Text(placeholder) }) else null,
            singleLine = minLineas == 1,
            minLines = minLineas,
            shape = FormShape,
            colors = camposColors(),
            visualTransformation = if (clave && !visible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (clave) KeyboardType.Password else teclado),
            trailingIcon = if (clave) {
                {
                    IconButton(onClick = { visible = !visible }) {
                        Icon(
                            if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = "Mostrar u ocultar contraseña"
                        )
                    }
                }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Selector(
    etiqueta: String,
    opciones: List<String>,
    indice: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var abierto by remember { mutableStateOf(false) }
    Column(modifier) {
        EtiquetaCampo(etiqueta)
        ExposedDropdownMenuBox(expanded = abierto, onExpandedChange = { abierto = it }) {
            OutlinedTextField(
                value = opciones.getOrElse(indice) { "" },
                onValueChange = {},
                readOnly = true,
                shape = FormShape,
                colors = camposColors(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = abierto) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
                opciones.forEachIndexed { i, o ->
                    DropdownMenuItem(text = { Text(o) }, onClick = { onSelect(i); abierto = false })
                }
            }
        }
    }
}

@Composable
fun BotonPrimario(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Navy),
        modifier = modifier.height(52.dp)
    ) { Text(texto, fontSize = 17.sp, fontWeight = FontWeight.Bold) }
}

@Composable
fun BotonSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icono: (@Composable () -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        border = BorderStroke(1.5.dp, if (enabled) Navy else Borde),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Navy),
        modifier = modifier.height(52.dp)
    ) {
        if (icono != null) {
            icono()
            androidx.compose.foundation.layout.Spacer(Modifier.padding(horizontal = 4.dp))
        }
        Text(texto, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

/** Botón tipo "pastilla" para elegir una opción (perfil, prioridad). */
@Composable
fun OpcionPastilla(texto: String, seleccionada: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (seleccionada) {
        BotonPrimario(texto, onClick, modifier)
    } else {
        BotonSecundario(texto, onClick, modifier)
    }
}

@Composable
fun Etiqueta(texto: String, fondo: Color, color: Color) {
    Text(
        texto, color = color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .background(fondo, CircleShape)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    )
}

@Composable
fun FiltroChip(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Text(
        texto,
        color = if (seleccionado) Color.White else Texto,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .background(if (seleccionado) Navy else ChipFondo, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    )
}

fun colorPrioridad(p: String): Pair<Color, Color> = when (p) {
    "Alta" -> Color(0xFFFDE2E2) to Color(0xFF9B1C1C)
    "Media" -> Color(0xFFFFEFC9) to Color(0xFF7A4A00)
    else -> Color(0xFFDDF1E3) to Color(0xFF1E5B33)
}

fun colorEstado(e: String): Color = when (e) {
    "Derivado" -> Color(0xFF5B35A8)
    "En revisión" -> Color(0xFF7A4A00)
    "Cerrado" -> Color(0xFF1E5B33)
    "Seguimiento" -> Color(0xFF0B5E8C)
    else -> Color(0xFF3B4A57)
}

/** "2026-10-14 09:30:00" -> "14 oct · 09:30" */
fun formatoFecha(s: String): String = try {
    LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        .format(DateTimeFormatter.ofPattern("d MMM · HH:mm", Locale("es", "CL")))
        .replace(".", "")
} catch (_: Exception) {
    s
}
