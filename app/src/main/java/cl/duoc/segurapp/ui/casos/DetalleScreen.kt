package cl.duoc.segurapp.ui.casos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.segurapp.data.Caso
import cl.duoc.segurapp.data.ESTADOS
import cl.duoc.segurapp.data.Usuario
import cl.duoc.segurapp.ui.AppHeader
import cl.duoc.segurapp.ui.BotonPrimario
import cl.duoc.segurapp.ui.BotonSecundario
import cl.duoc.segurapp.ui.FormShape
import cl.duoc.segurapp.ui.formatoFecha
import cl.duoc.segurapp.ui.theme.Ambar
import cl.duoc.segurapp.ui.theme.Borde
import cl.duoc.segurapp.ui.theme.Fondo
import cl.duoc.segurapp.ui.theme.Gris
import cl.duoc.segurapp.ui.theme.Navy
import cl.duoc.segurapp.ui.theme.Texto
import cl.duoc.segurapp.ui.theme.TextoSec

@Composable
fun DetalleScreen(id: Int, usuario: Usuario?, vm: CasosViewModel, onBack: () -> Unit) {
    var observacion by remember { mutableStateOf("") }

    LaunchedEffect(id) {
        vm.limpiarError()
        vm.cargarDetalle(id)
    }

    val caso = vm.detalle?.takeIf { it.id == id }
    val esPersonal = usuario?.rol != "residente"

    Column(Modifier.fillMaxSize().background(Fondo)) {
        AppHeader(caso?.codigo ?: "Caso", caso?.titulo, onBack)

        if (caso == null) {
            val error = vm.error
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            } else {
                CircularProgressIndicator(Modifier.padding(32.dp).align(Alignment.CenterHorizontally))
            }
            return@Column
        }

        Column(
            Modifier
                .weight(1f)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TarjetaBlanca { Text("Estado del caso", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Texto); Progreso(caso.estado) }

            TarjetaBlanca {
                Text(campo("Prioridad", caso.prioridad), fontSize = 17.sp, color = Texto)
                Text(campo("Sector", "${caso.sector.removePrefix("Sector ")} (referencial)"), fontSize = 17.sp, color = Texto)
                Text(campo("Responsable", caso.responsable), fontSize = 17.sp, color = Texto)
                Text(caso.descripcion, fontSize = 15.sp, color = TextoSec)
            }

            TarjetaBlanca {
                Text("Historial", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Texto)
                caso.historial.orEmpty().forEach {
                    Text("${formatoFecha(it.creadoEn)} · ${it.texto}", fontSize = 15.sp, color = TextoSec)
                }
            }

            Column {
                Text("Agregar observación", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Texto, modifier = Modifier.padding(bottom = 6.dp))
                OutlinedTextField(
                    value = observacion,
                    onValueChange = { observacion = it },
                    placeholder = { Text("Escribe un avance") },
                    shape = FormShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                        unfocusedBorderColor = Borde
                    ),
                    trailingIcon = {
                        IconButton(
                            enabled = observacion.isNotBlank(),
                            onClick = { vm.agregarObservacion(id, observacion) { observacion = "" } }
                        ) { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar observación", tint = Navy) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            vm.error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp) }

            if (esPersonal) {
                val indice = ESTADOS.indexOf(caso.estado)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BotonSecundario("Derivar", { vm.derivar(id) }, Modifier.weight(1f), enabled = indice < ESTADOS.indexOf("Derivado"))
                    BotonPrimario("Cambiar estado", { vm.avanzar(id) }, Modifier.weight(1.2f), enabled = indice < ESTADOS.lastIndex)
                }
            }
        }
    }
}

private fun campo(nombre: String, valor: String) = buildAnnotatedString {
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("$nombre: ") }
    append(valor)
}

@Composable
private fun TarjetaBlanca(contenido: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { contenido() }
    }
}

@Composable
private fun Progreso(estadoActual: String) {
    val actual = ESTADOS.indexOf(estadoActual)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        ESTADOS.forEachIndexed { i, nombre ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                val punto = Modifier.size(26.dp)
                when {
                    i < actual -> Box(punto.background(Navy, CircleShape))
                    i == actual -> Box(punto.background(Ambar, CircleShape).border(3.dp, Navy, CircleShape))
                    else -> Box(punto.background(Gris, CircleShape))
                }
                Text(
                    nombre, fontSize = 10.sp, maxLines = 1, softWrap = false, textAlign = TextAlign.Center,
                    fontWeight = if (i == actual) FontWeight.Bold else FontWeight.Normal,
                    color = if (i == actual) Navy else TextoSec,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}
