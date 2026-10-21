package cl.duoc.segurapp.ui.casos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.segurapp.data.NuevoCasoRequest
import cl.duoc.segurapp.ui.AppHeader
import cl.duoc.segurapp.ui.BotonPrimario
import cl.duoc.segurapp.ui.BotonSecundario
import cl.duoc.segurapp.ui.CampoTexto
import cl.duoc.segurapp.ui.EtiquetaCampo
import cl.duoc.segurapp.ui.OpcionPastilla
import cl.duoc.segurapp.ui.Selector
import cl.duoc.segurapp.ui.theme.Borde
import cl.duoc.segurapp.ui.theme.ChipFondo
import cl.duoc.segurapp.ui.theme.Fondo
import cl.duoc.segurapp.ui.theme.Texto
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val PRIORIDADES = listOf("Baja", "Media", "Alta")

@Composable
fun NuevoCasoScreen(vm: CasosViewModel, onGuardado: () -> Unit) {
    var categoria by rememberSaveable { mutableIntStateOf(0) }
    var sector by rememberSaveable { mutableIntStateOf(0) }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var prioridad by rememberSaveable { mutableIntStateOf(1) }
    var evidencia by rememberSaveable { mutableStateOf(false) }
    val ahora = remember { LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm")) }

    LaunchedEffect(Unit) {
        vm.limpiarError()
    }

    val cat = vm.catalogos
    Column(Modifier.fillMaxSize().background(Fondo)) {
        AppHeader("Nuevo caso", "Registro de situación (datos ficticios)")
        Column(
            Modifier
                .weight(1f)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Selector("Categoría", cat.categorias.map { it.nombre }, categoria, { categoria = it })
            Selector("Sector referencial", cat.sectores.map { it.nombre }, sector, { sector = it })
            CampoTexto(
                "Descripción breve", descripcion, { descripcion = it },
                placeholder = "Luminarias apagadas en tramo de calle (ejemplo).", minLineas = 3
            )

            Column {
                EtiquetaCampo("Prioridad")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PRIORIDADES.forEachIndexed { i, p ->
                        OpcionPastilla(p, prioridad == i, { prioridad = i }, Modifier.weight(1f))
                    }
                }
            }

            Column {
                EtiquetaCampo("Fecha y hora simuladas")
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(ChipFondo, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) { Text(ahora, color = Texto, fontSize = 17.sp) }
            }

            BotonSecundario(
                if (evidencia) "Evidencia simulada adjunta" else "Adjuntar evidencia simulada",
                { evidencia = !evidencia },
                Modifier.fillMaxWidth(),
                icono = { Icon(if (evidencia) Icons.Outlined.CheckCircle else Icons.Outlined.AttachFile, null) }
            )

            vm.error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp) }

            if (vm.cargando) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            } else {
                BotonPrimario(
                    "Guardar caso",
                    {
                        vm.crearCaso(
                            NuevoCasoRequest(
                                cat.categorias[categoria].id, cat.sectores[sector].id,
                                descripcion, PRIORIDADES[prioridad], evidencia
                            )
                        ) {
                            descripcion = ""; evidencia = false; prioridad = 1
                            onGuardado()
                        }
                    },
                    Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
