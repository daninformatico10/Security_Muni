package cl.duoc.segurapp.ui.casos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.segurapp.data.Caso
import cl.duoc.segurapp.data.Usuario
import cl.duoc.segurapp.ui.AppHeader
import cl.duoc.segurapp.ui.Etiqueta
import cl.duoc.segurapp.ui.FiltroChip
import cl.duoc.segurapp.ui.FormShape
import cl.duoc.segurapp.ui.colorEstado
import cl.duoc.segurapp.ui.colorPrioridad
import cl.duoc.segurapp.ui.formatoFecha
import cl.duoc.segurapp.ui.theme.Borde
import cl.duoc.segurapp.ui.theme.ChipFondo
import cl.duoc.segurapp.ui.theme.Fondo
import cl.duoc.segurapp.ui.theme.Texto
import cl.duoc.segurapp.ui.theme.TextoSec

@Composable
fun CasosScreen(usuario: Usuario?, vm: CasosViewModel, onDetalle: (Int) -> Unit) {
    var busqueda by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf("Todos") }

    LaunchedEffect(Unit) { vm.cargarCasos() }

    val visibles = vm.casos.filter { c ->
        (filtro == "Todos" || c.estado == filtro) &&
            (busqueda.isBlank() ||
                c.codigo.contains(busqueda, ignoreCase = true) ||
                c.categoria.contains(busqueda, ignoreCase = true) ||
                c.titulo.contains(busqueda, ignoreCase = true))
    }

    Column(Modifier.fillMaxSize().background(Fondo)) {
        AppHeader(
            "Casos",
            "Perfil: ${usuario?.rol?.replaceFirstChar { it.uppercase() } ?: ""} · ${vm.casos.size} casos de prueba"
        )
        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            placeholder = { Text("Buscar por ID o categoría") },
            singleLine = true,
            shape = FormShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                unfocusedBorderColor = Borde
            ),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf("Todos", "En revisión", "Derivado", "Cerrado")) { f ->
                FiltroChip(f, filtro == f) { filtro = f }
            }
        }

        vm.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
        }

        if (vm.cargando && vm.casos.isEmpty()) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(Modifier.padding(32.dp))
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(visibles, key = { it.id }) { caso -> CasoCard(caso) { onDetalle(caso.id) } }
                if (visibles.isEmpty() && vm.error == null && !vm.cargando) {
                    item { Text("No hay casos para mostrar.", color = TextoSec) }
                }
            }
        }
    }
}

@Composable
private fun CasoCard(caso: Caso, onClick: () -> Unit) {
    val (fondoPrio, colorPrio) = colorPrioridad(caso.prioridad)
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(caso.codigo, color = TextoSec, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(formatoFecha(caso.creadoEn), color = TextoSec, fontSize = 14.sp)
            }
            Text(
                caso.titulo, color = Texto, fontWeight = FontWeight.Bold, fontSize = 20.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text("${caso.sector} (referencial)", color = TextoSec, fontSize = 15.sp)
            Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Etiqueta("Prioridad ${caso.prioridad.lowercase()}", fondoPrio, colorPrio)
                Etiqueta(caso.estado, ChipFondo, colorEstado(caso.estado))
            }
        }
    }
}
